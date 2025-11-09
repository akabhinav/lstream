package com.lstream.repository;

import com.lstream.model.Stream;
import com.lstream.model.StreamQuality;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface StreamQualityRepository extends JpaRepository<StreamQuality, Long> {

    List<StreamQuality> findByStream(Stream stream);

    List<StreamQuality> findByStreamAndAvailable(Stream stream, Boolean available);

    Optional<StreamQuality> findByStreamAndName(Stream stream, String name);

    void deleteByStream(Stream stream);
}
