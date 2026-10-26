package com.gdgoc.studyhub.domain.study.repository;

import com.gdgoc.studyhub.domain.study.entity.Study;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyRepository extends JpaRepository<Study, Long> {

    Optional<Study> findByInviteCode(String inviteCode);

    boolean existsByInviteCode(String inviteCode);
}
