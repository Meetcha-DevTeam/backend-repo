package com.meetcha.project.domain;

import com.meetcha.auth.domain.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Getter
@NoArgsConstructor
@Table(name = "user_project_aliases")
public class UserProjectAliasEntity {
    //프로젝트 별칭 엔티티
    @Id
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
