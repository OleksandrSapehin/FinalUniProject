package org.example.questionsservise.service.impl;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.api.gax.rpc.ApiException;
import com.google.cloud.speech.v1.*;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import lombok.RequiredArgsConstructor;
import org.example.questionsservise.service.QuestionsService;
import org.example.questionsservise.service.SpeechToTextService;
import org.example.questionsservise.service.VertexAIService;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class SpeechToTextServiceImpl implements SpeechToTextService {

    private static final String BUCKET_NAME = "bucket_for_service";
    private static final String FFMPEG_PATH = "D:\\Java\\ffmpeg-master-latest-win64-gpl\\bin\\ffmpeg.exe";
    private static final String CHUNK_DIR = "chunks/";
    private final SpeechSettings speechSettings;
    private final Storage storage;
    private final VertexAIService vertexAIService;
    private final QuestionsService questionsService;
    
    @Override
    public String transcribeAudio(String audioFilePath) throws IOException, InterruptedException {
        File chunkDir = new File(CHUNK_DIR);
        if (!chunkDir.exists() && !chunkDir.mkdirs()) {
            throw new IOException("Failed to create chunk directory: " + CHUNK_DIR);
        }

        List<String> chunkPaths = splitAudioFile(audioFilePath);

        try (ExecutorService executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors())) {
            List<CompletableFuture<String>> futures = chunkPaths.stream()
                    .map(chunkPath -> CompletableFuture.supplyAsync(() -> {
                        try {
                            String gcsUri = uploadFileToGCS(storage, chunkPath);
                            return transcribeChunk(gcsUri);
                        } catch (IOException | InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }, executor))
                    .toList();

            StringBuilder combinedTranscript = new StringBuilder();
            for (CompletableFuture<String> future : futures) {
                try {
                    combinedTranscript.append(future.get()).append("\n");
                } catch (ExecutionException e) {
                    throw new RuntimeException("Error during transcription: " + e.getMessage(), e);
                }
            }
            cleanUpChunksFromGCS(chunkPaths);

            List<String> questions = vertexAIService.extractQuestions(combinedTranscript.toString());
            questions.forEach(System.out::println);
            questionsService.saveQuestions(questions);

            return "Transcription and question saving completed successfully";

        }
    }

    private List<String> splitAudioFile(String inputFilePath) throws IOException, InterruptedException {
        List<String> chunkPaths = new ArrayList<>();
        String chunkFileNamePattern = CHUNK_DIR + "chunk_%03d.mp3";

        File chunkDir = new File(CHUNK_DIR);
        if (!chunkDir.exists() && !chunkDir.mkdirs()) {
            throw new IOException("Failed to create chunk directory: " + CHUNK_DIR);
        }

        String command = FFMPEG_PATH + " -i \"" + inputFilePath + "\" -f segment -segment_time " + 60 + " -c copy " + chunkFileNamePattern;
        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        processBuilder.redirectErrorStream(true);

        Process process = processBuilder.start();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        }

        int exitCode = process.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("Error splitting audio file. Exit code: " + exitCode);
        }

        for (File file : Objects.requireNonNull(chunkDir.listFiles())) {
            if (file.getName().startsWith("chunk_") && file.getName().endsWith(".mp3")) {
                chunkPaths.add(file.getAbsolutePath());
            }
        }
        return chunkPaths;
    }

    private String transcribeChunk(String gcsUri) throws IOException, InterruptedException {
        try (SpeechClient speechClient = SpeechClient.create(speechSettings)) {
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(RecognitionConfig.AudioEncoding.MP3)
                    .setSampleRateHertz(16000)
                    .setLanguageCode("en-US")
                    .build();
            RecognitionAudio audio = RecognitionAudio.newBuilder().setUri(gcsUri).build();

            LongRunningRecognizeRequest request = LongRunningRecognizeRequest.newBuilder()
                    .setConfig(config)
                    .setAudio(audio)
                    .build();

            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> future =
                    speechClient.longRunningRecognizeAsync(request);

            try {
                LongRunningRecognizeResponse response = future.get(60, TimeUnit.MINUTES);
                List<SpeechRecognitionResult> results = response.getResultsList();

                StringBuilder transcript = new StringBuilder();
                for (SpeechRecognitionResult result : results) {
                    SpeechRecognitionAlternative alternative = result.getAlternativesList().getFirst();
                    transcript.append(alternative.getTranscript());
                    transcript.append("\n");
                }
                return transcript.toString();
            } catch (ExecutionException | TimeoutException e) {
                throw new RuntimeException("Error during transcription: " + e.getMessage(), e);
            }

        } catch (ApiException e) {
            throw new RuntimeException("API error during transcription: " + e.getMessage(), e);
        }
    }

    private String uploadFileToGCS(Storage storage, String filePath) throws IOException {
        Path path = Paths.get(filePath);
        String fileName = path.getFileName().toString();
        BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
        BlobInfo blobInfo = BlobInfo.newBuilder(blobId).build();
        storage.create(blobInfo, Files.readAllBytes(path));
        return "gs://" + BUCKET_NAME + "/" + fileName;
    }

    private void cleanUpChunksFromGCS(List<String> chunkPaths) {
        for (String chunkPath : chunkPaths) {
            Path path = Paths.get(chunkPath);
            String fileName = path.getFileName().toString();
            BlobId blobId = BlobId.of(BUCKET_NAME, fileName);
            storage.delete(blobId);
        }
    }
}
