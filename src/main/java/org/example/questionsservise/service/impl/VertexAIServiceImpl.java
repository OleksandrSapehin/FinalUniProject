package org.example.questionsservise.service.impl;

import com.google.cloud.vertexai.api.*;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import lombok.RequiredArgsConstructor;
import org.example.questionsservise.service.VertexAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.*;


@Service
@RequiredArgsConstructor
public class VertexAIServiceImpl implements VertexAIService {

    private final GenerativeModel generativeModel;
    private static final Logger log = LoggerFactory.getLogger(VertexAIServiceImpl.class);

    @Override
    public List<String> extractQuestions(String text) throws IOException {
        String prompt = "Extract and list only the interviewer's questions from the following text, separating each question with a comma. Ensure that each question is complete and correctly formatted: " + text;
        GenerateContentResponse response = generativeModel.generateContent(prompt);
        String responseText = response.getCandidatesList().getFirst().getContent().toString().replace("\\'", "'").trim();
        log.info("AI response text: {}", responseText);

        List<String> questions = extractQuestionsFromText(responseText);

        log.info("Extracted questions: {}", questions);
        return questions;
    }

    private List<String> extractQuestionsFromText(String text) {
        List<String> questions = new ArrayList<>();

        String[] questionStarters = {"what", "how", "why", "when", "where", "who", "which", "can", "is", "are", "do", "does", "did", "could", "would", "should", "will"};

        String[] splitText = text.split("[,\\n]");

        for (String part : splitText) {
            part = part.trim();
            if (isQuestion(part, questionStarters)) {
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

    private boolean isQuestion(String text, String[] starters) {
        text = text.toLowerCase();
        for (String starter : starters) {
            if (text.startsWith(starter) || text.endsWith("?")) {
                return true;
            }
        }
        return false;
    }
}
