package com.meetcha.project.domain;

import com.meetcha.auth.domain.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcType;
import org.hibernate.type.descriptor.jdbc.BinaryJdbcType;

import java.util.UUID;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "user_project_aliases")
public class UserProjectAliasEntity {
    //프로젝트 별칭 엔티티
    @Id
    @JdbcType(BinaryJdbcType.class)
    @Column(name = "alias_id", nullable = false)
    private UUID aliasId;

    @Column(name = "custom_name")
    private String customName;

    @ManyToOne
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

}