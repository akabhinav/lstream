package com.lstream.repository;

import com.lstream.model.Stream;
import com.lstream.model.StreamAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StreamAnalyticsRepository extends JpaRepository<StreamAnalytics, Long> {

    List<StreamAnalytics> findByStreamOrderByTimestampDesc(Stream stream);

    @Query("SELECT sa FROM StreamAnalytics sa WHERE sa.stream = :stream AND sa.timestamp >= :since ORDER BY sa.timestamp ASC")
    List<StreamAnalytics> findByStreamSince(@Param("stream") Stream stream, @Param("since") LocalDateTime since);

    @Query("SELECT MAX(sa.currentViewers) FROM StreamAnalytics sa WHERE sa.stream = :stream")
    Long findPeakViewers(@Param("stream") Stream stream);

    @Query("SELECT AVG(sa.currentViewers) FROM StreamAnalytics sa WHERE sa.stream = :stream")
    Double findAverageViewers(@Param("stream") Stream stream);

    void deleteByStream(Stream stream);
}
