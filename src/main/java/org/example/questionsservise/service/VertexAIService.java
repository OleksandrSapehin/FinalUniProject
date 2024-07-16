package org.example.questionsservise.service;


import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface VertexAIService {
    List<String> extractQuestions(String text) throws IOException;
}
