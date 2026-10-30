package com.vriddhi.content.repository;

import com.vriddhi.content.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepository extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNameIgnoreCase(String name);
}
