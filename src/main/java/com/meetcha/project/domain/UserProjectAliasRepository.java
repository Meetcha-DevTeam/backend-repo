package com.meetcha.project.domain;

import com.meetcha.project.dto.GetProjectsDto;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface UserProjectAliasRepository extends Repository<UserProjectAliasEntity, UUID> {
    //user_project_aliases 테이블 조회용 Repository
    //특정 사용자가 참여 중인 프로젝트 목록 조회
    //별칭 존재하면 그걸 우선 사용
    //프로젝트 생성일 기준 최신순 정렬
    //JPQL 사용
    @Query("""
    SELECT new com.meetcha.project.dto.GetProjectsDto(
        p.projectId,
        COALESCE(a.customName, p.name)
    )
    FROM UserProjectAliasEntity a
    JOIN a.project p
    WHERE a.user.userId = :userId
    ORDER BY p.createdAt DESC
""")

    List<GetProjectsDto> findProjectsByUserId(@Param("userId") UUID userId);
}
