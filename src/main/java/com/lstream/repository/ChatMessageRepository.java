package com.lstream.repository;

import com.lstream.model.ChatMessage;
import com.lstream.model.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    Page<ChatMessage> findByStreamOrderByTimestampDesc(Stream stream, Pageable pageable);

    @Query("SELECT COUNT(c) FROM ChatMessage c WHERE c.stream = :stream AND c.timestamp > :since")
    Long countRecentMessages(@Param("stream") Stream stream, @Param("since") LocalDateTime since);

    @Query("SELECT c FROM ChatMessage c WHERE c.stream = :stream AND c.deleted = false ORDER BY c.timestamp DESC")
    List<ChatMessage> findRecentMessages(@Param("stream") Stream stream, Pageable pageable);

    void deleteByStream(Stream stream);
}
