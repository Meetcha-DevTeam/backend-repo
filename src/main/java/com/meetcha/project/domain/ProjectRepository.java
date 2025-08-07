package com.meetcha.project.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ProjectRepository extends JpaRepository<ProjectEntity, UUID> {
    Optional<ProjectEntity> findByName(String name);
}
