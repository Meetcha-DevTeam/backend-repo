package com.meetcha.project.domain;

import com.meetcha.auth.domain.UserEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "projects",
        // 유저별 같은 이름 금지 (user_id + name) 복합 유니크
        uniqueConstraints = @UniqueConstraint(
                name = "uk_projects_user_name",
                columnNames = {"user_id", "name"}
        )
)
public class ProjectEntity {
    @Id
    @JdbcType(BinaryJdbcType.class)
    @Column(name = "project_id", nullable = false)
    private UUID projectId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
}
