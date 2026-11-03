package com.vriddhi.content.repository;

import com.vriddhi.content.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    Optional<Category> findByCodeIgnoreCase(String code);
}
