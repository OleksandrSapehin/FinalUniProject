package org.example.questionsservise.service.impl;

import lombok.RequiredArgsConstructor;
import org.example.questionsservise.models.Question;
import org.example.questionsservise.repository.QuestionRepository;
import org.example.questionsservise.service.QuestionsService;
import org.example.questionsservise.service.VertexAIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.util.List;
import java.util.Map;


@Service
@RequiredArgsConstructor
public class QuestionsServiceImpl implements QuestionsService {

    private final QuestionRepository questionRepository;
    private static final Logger log = LoggerFactory.getLogger(QuestionsServiceImpl.class);

    @Override
    public void saveQuestions(List<String> questions) throws IOException {
        for (String qText : questions) {
            Question question = new Question();
            question.setQuestionText(qText);
            question.setCount(1);
            question.setChance(100);
            questionRepository.save(question);
        }
        log.info("Questions saved to the database: " + questions);
    }



}
