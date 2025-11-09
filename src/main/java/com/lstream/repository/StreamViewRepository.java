package com.lstream.repository;

import com.lstream.model.Stream;
import com.lstream.model.StreamView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StreamViewRepository extends JpaRepository<StreamView, Long> {

    List<StreamView> findByStream(Stream stream);

    @Query("SELECT COUNT(DISTINCT sv.ipAddress) FROM StreamView sv WHERE sv.stream = :stream")
    Long countUniqueViewers(@Param("stream") Stream stream);

    @Query("SELECT AVG(sv.watchDurationSeconds) FROM StreamView sv WHERE sv.stream = :stream")
    Double getAverageWatchTime(@Param("stream") Stream stream);

    @Query("SELECT sv.country, COUNT(sv) as count FROM StreamView sv WHERE sv.stream = :stream GROUP BY sv.country ORDER BY count DESC")
    List<Object[]> getViewersByCountry(@Param("stream") Stream stream);

    @Query("SELECT COUNT(sv) FROM StreamView sv WHERE sv.stream.streamer.id = :streamerId AND sv.startedAt >= :since")
    Long countViewsSince(@Param("streamerId") Long streamerId, @Param("since") LocalDateTime since);
}
