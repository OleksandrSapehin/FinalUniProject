package org.example.questionsservise.service;

import com.google.api.services.youtube.model.Caption;
import com.google.api.services.youtube.model.SearchResult;
import org.example.questionsservise.models.YouTubeVideo;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;

import java.util.List;


public interface YouTubeService {

    List<SearchResult> searchVideosByKeyword(String keyword);

    void saveSearchResults(List<SearchResult> searchResults);
}
