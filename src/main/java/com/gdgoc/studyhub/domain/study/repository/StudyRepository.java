package com.gdgoc.studyhub.domain.study.repository;

import com.gdgoc.studyhub.domain.study.entity.Study;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyRepository extends JpaRepository<Study, Long> {

    Optional<Study> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Study s where s.id = :studyId")
    Optional<Study> lockById(@Param("studyId") Long studyId);
}
