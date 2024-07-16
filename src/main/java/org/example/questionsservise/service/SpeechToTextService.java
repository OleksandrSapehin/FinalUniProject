package org.example.questionsservise.service;

import java.io.IOException;

public interface SpeechToTextService {
    String transcribeAudio(String audioFilePath) throws IOException, InterruptedException;
}
