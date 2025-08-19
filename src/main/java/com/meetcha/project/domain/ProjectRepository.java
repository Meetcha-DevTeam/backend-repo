package com.meetcha.project.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    boolean existsByUser_UserIdAndName(UUID userId, String name);
    List<ProjectEntity> findAllByUser_UserId(UUID userId);
}