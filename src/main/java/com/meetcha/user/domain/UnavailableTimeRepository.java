package com.meetcha.user.domain;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface UnavailableTimeRepository extends JpaRepository<UnavailableTime, Long> {
    List<UnavailableTime> findAllByUserId(UUID userId);
}
