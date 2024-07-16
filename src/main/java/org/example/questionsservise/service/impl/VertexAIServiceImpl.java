package org.example.questionsservise.service.impl;

import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.example.questionsservise.service.VertexAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


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

        String[] splitText = responseText.split(",");

        List<String> questions = new ArrayList<>();

        for (String part : splitText) {
            part = part.trim();
            if (!part.isEmpty()) {
                questions.add(part);
            }
        }

        return questions;
    }
}
