package org.example.questionsservise.service.impl;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.CaptionListResponse;
import com.google.api.services.youtube.model.SearchListResponse;
import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import org.example.questionsservise.models.Question;
import org.example.questionsservise.models.YouTubeVideo;
import org.example.questionsservise.repository.QuestionRepository;
import org.example.questionsservise.repository.YouTubeVideoRepository;
import org.example.questionsservise.service.AudioDownloadService;
import org.example.questionsservise.service.YouTubeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class YouTubeServiceImpl implements YouTubeService {

    private final YouTubeVideoRepository youTubeVideoRepository;
    private final YouTube youTube;
    private final AudioDownloadService audioDownloadService;
    private static final Logger logger = LoggerFactory.getLogger(YouTubeServiceImpl.class);


    @Override
    public List<SearchResult> searchVideosByKeyword(String keyword) {
        try {
            YouTube.Search.List search = youTube.search().list("snippet");
            search.setKey("");
            search.setQ(keyword);
            search.setType("video");
            search.setMaxResults(1L);
            search.setRelevanceLanguage("en");
            search.setFields("items(id/videoId,snippet/title)");

            SearchListResponse response = search.execute();
            List<SearchResult> items = response.getItems();
            logger.info("Search results: {}", items);
            return items;
        } catch (IOException e) {
            logger.error("Failed to search videos on YouTube", e);
            throw new RuntimeException("Failed to search videos on YouTube", e);
        }

    }

    @Override
    public void saveSearchResults(List<SearchResult> searchResults) {
        List<YouTubeVideo> videos = searchResults.stream()
                .map(result -> {
                    if (result.getId() != null && result.getId().getVideoId() != null && result.getSnippet() != null && result.getSnippet().getTitle() != null) {
                        YouTubeVideo video = new YouTubeVideo();
                        video.setLinkId(result.getId().getVideoId());
                        video.setTitle(result.getSnippet().getTitle());
                        logger.info("Mapped YouTube video: {}", video);
                        return video;
                    } else {
                        logger.error("Invalid search result: {}", result);
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        try {
            youTubeVideoRepository.saveAll(videos);
            for (YouTubeVideo video : videos) {
                try {
                    audioDownloadService.downloadAudio(video.getLinkId(), video.getTitle());
                } catch (Exception e) {
                    logger.error("Failed to download audio for video ID: {}", video.getLinkId(), e);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to save YouTube videos", e);
            throw new RuntimeException("Failed to save YouTube videos", e);
        }
    }
}
