package org.example.questionsservise.controller;

import com.google.api.services.youtube.model.SearchResult;
import lombok.RequiredArgsConstructor;
import org.example.questionsservise.service.YouTubeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@RestController
@RequiredArgsConstructor
public class YouTubeController {

    private final YouTubeService youTubeService;

    @GetMapping("/api/search")
    public List<SearchResult> search(@RequestParam String keyword) {
       List<SearchResult> list = youTubeService.searchVideosByKeyword(keyword);
       youTubeService.saveSearchResults(list);
       return list;
    }
}
