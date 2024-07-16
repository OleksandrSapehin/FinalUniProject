package org.example.questionsservise.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.questionsservise.service.AudioDownloadService;
import org.example.questionsservise.service.SpeechToTextService;
import org.example.questionsservise.utils.FileName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

@Service
@RequiredArgsConstructor
public class AudioDownloadServiceImpl implements AudioDownloadService {

    private static final Logger logger = LoggerFactory.getLogger(AudioDownloadService.class);
    private static final String FFMPEG_PATH = "D:\\Java\\ffmpeg-master-latest-win64-gpl\\bin";
    private static final String DOWNLOADS_DIR = "downloads/";

    private final SpeechToTextService speechToTextService;

    @Override
    public void downloadAudio(String videoId, String videoTitle) throws IOException, InterruptedException {
        String sanitizedTitle = FileName.sanitizeFileName(videoTitle);
        String filePath = DOWNLOADS_DIR + sanitizedTitle + ".mp3";

        String command = "yt-dlp -x --audio-format mp3 --ffmpeg-location " + FFMPEG_PATH + " -o \"" + filePath + "\" https://www.youtube.com/watch?v=" + videoId;

        ProcessBuilder processBuilder = new ProcessBuilder("cmd.exe", "/c", command);
        processBuilder.redirectErrorStream(true);

        try {
            Process process = processBuilder.start();
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                logger.info(line);
            }
            int exitCode = process.waitFor();
            if (exitCode == 0) {
                logger.info("Audio downloaded successfully for video ID: {}", videoId);

                String transcript = speechToTextService.transcribeAudio(filePath);
                System.out.println("Transcript for video ID " + videoId + ": " + transcript);

                File audioFile = new File(filePath);
                if (audioFile.exists() && !audioFile.delete()) {
                    logger.warn("Failed to delete audio file: {}", filePath);
                }
            } else {
                logger.error("Failed to download audio for video ID: {}. Exit code: {}", videoId, exitCode);
            }

        } catch (IOException | InterruptedException e) {
            logger.error("Error while downloading audio for video ID: " + videoId, e);
        }
    }

}
