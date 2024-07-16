package org.example.questionsservise.service;

import java.io.IOException;

public interface AudioDownloadService {

   void downloadAudio(String videoId, String videoTitle) throws IOException, InterruptedException;
}
