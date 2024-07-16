package org.example.questionsservise.сonfig;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.language.v1.LanguageServiceClient;
import com.google.cloud.language.v1.LanguageServiceSettings;
import com.google.cloud.speech.v1.SpeechSettings;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.EndpointServiceClient;
import com.google.cloud.vertexai.api.EndpointServiceSettings;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Collections;

@Configuration
public class GoogleCloudConfig {

    private static final String CREDENTIALS_PATH = "D:\\Java\\speech_key.json";

    @Bean
    public GoogleCredentials googleCredentials() throws IOException {
      return GoogleCredentials.fromStream(new FileInputStream(CREDENTIALS_PATH))
              .createScoped(Collections.singletonList("https://www.googleapis.com/auth/cloud-platform"));
    }

    @Bean
    public SpeechSettings speechSettings(GoogleCredentials googleCredentials) throws IOException {
        return SpeechSettings.newBuilder()
                .setCredentialsProvider(() -> googleCredentials)
                .build();
    }

    @Bean
    public Storage storage(GoogleCredentials googleCredentials){
        return StorageOptions.newBuilder()
                .setCredentials(googleCredentials)
                .build()
                .getService();
    }

    @Bean
    public PredictionServiceClient predictionServiceClient(GoogleCredentials googleCredentials) throws IOException {
        PredictionServiceSettings settings = PredictionServiceSettings.newBuilder()
                .setCredentialsProvider(() -> googleCredentials)
                .build();
        return PredictionServiceClient.create(settings);
    }
}
