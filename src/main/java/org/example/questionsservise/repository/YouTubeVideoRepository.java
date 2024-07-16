package org.example.questionsservise.repository;

import org.example.questionsservise.models.YouTubeVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface YouTubeVideoRepository extends JpaRepository<YouTubeVideo,Long > {

}
