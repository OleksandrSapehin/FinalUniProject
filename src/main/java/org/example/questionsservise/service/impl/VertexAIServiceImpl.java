package org.example.questionsservise.service.impl;

import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.questionsservise.service.VertexAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.StringReader;
import java.util.*;


@Service
@RequiredArgsConstructor
public class VertexAIServiceImpl implements VertexAIService {

    private final GenerativeModel generativeModel;
    private static final Logger log = LoggerFactory.getLogger(VertexAIServiceImpl.class);

    @Override
    public List<String> extractQuestions(String text) throws IOException {
        String prompt = "Write in your response only extracted interviewer's questions, comma separated from the following text: " + text;
        GenerateContentResponse response = generativeModel.generateContent(prompt);
        String responseText = response.getCandidatesList().getFirst().getContent().toString();
        log.info("AI response text: {}", responseText);

        List<String> questions = extractQuestionsFromText(responseText);

        log.info("Extracted questions: {}", questions);
        return questions;
    }

    private List<String> extractQuestionsFromText(String text) {
        List<String> questions = new ArrayList<>();

        text = text.replaceAll("role: \"model\"", "")
                .replaceAll("parts \\{", "")
                .replaceAll("\\}", "")
                .replaceAll("text: \"", "")
                .replaceAll("\"$", "")
                .trim();

        String[] splitText = text.split("[,\\n]");

        for (String part : splitText) {
            part = part.trim();
            if (!part.isEmpty()) {
                questions.add(part);
            }
        }
        if (!questions.isEmpty()) {
            int lastIndex = questions.size() - 1;
            String lastQuestion = questions.get(lastIndex);
            if (lastQuestion.length() > 3) {
                questions.set(lastIndex, lastQuestion.substring(0, lastQuestion.length() - 3).trim());
            }
        }
        log.info("Extracted questions from text: {}", questions);
        return questions;
    }
}
