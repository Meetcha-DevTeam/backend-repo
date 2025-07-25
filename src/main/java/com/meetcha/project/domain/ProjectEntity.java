package com.meetcha.project.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "projects")
public class ProjectEntity {
    @Id
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    private String name;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
