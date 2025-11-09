package com.lstream.repository;

import com.lstream.model.Stream;
import com.lstream.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamRepository extends JpaRepository<Stream, Long> {

    Optional<Stream> findByStreamKey(String streamKey);

    List<Stream> findByStreamer(User streamer);

    List<Stream> findByStatus(Stream.StreamStatus status);

    @Query("SELECT s FROM Stream s WHERE s.status = :status AND s.privacy = 'PUBLIC' ORDER BY s.currentViewers DESC")
    Page<Stream> findLiveStreams(@Param("status") Stream.StreamStatus status, Pageable pageable);

    @Query("SELECT s FROM Stream s WHERE s.status = 'LIVE' AND s.privacy = 'PUBLIC' AND s.category = :category ORDER BY s.currentViewers DESC")
    Page<Stream> findLiveStreamsByCategory(@Param("category") String category, Pageable pageable);

    @Query("SELECT s FROM Stream s WHERE s.streamer = :streamer ORDER BY s.createdAt DESC")
    Page<Stream> findByStreamerOrderByCreatedAtDesc(@Param("streamer") User streamer, Pageable pageable);

    @Query("SELECT s FROM Stream s WHERE s.status = 'LIVE' AND s.privacy = 'PUBLIC' ORDER BY s.currentViewers DESC")
    List<Stream> findTrendingStreams(Pageable pageable);

    @Query("SELECT DISTINCT s.category FROM Stream s WHERE s.category IS NOT NULL")
    List<String> findAllCategories();
}
