package org.example.questionsservise.сonfig;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.PredictionServiceClient;
import com.google.cloud.vertexai.api.PredictionServiceSettings;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import lombok.RequiredArgsConstructor;
import org.example.questionsservise.service.VertexAIService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class GeminiConfig {

    private static final String PROJECT_ID = "rising-city-423314-d3";
    private static final String LOCATION = "europe-central2";
    private static final String MODEL_NAME = "gemini-1.5-flash-001";

    @Bean
    public VertexAI vertexAI(GoogleCredentials googleCredentials) throws IOException {
        return new VertexAI.Builder()
                .setProjectId(PROJECT_ID)
                .setLocation(LOCATION)
                .setCredentials(googleCredentials)
                .build();
    }

    @Bean
    public GenerativeModel geminiProGenerativeModel(VertexAI vertexAI) {
        return new GenerativeModel(MODEL_NAME, vertexAI);
    }
}
