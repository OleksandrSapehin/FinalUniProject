package org.example.questionsservise.repository;

import org.example.questionsservise.models.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface QuestionRepository extends JpaRepository<Question,Long > {
    Question findByQuestionText(String questionText);
}
