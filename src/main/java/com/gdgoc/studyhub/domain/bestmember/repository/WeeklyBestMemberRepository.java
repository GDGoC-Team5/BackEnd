package com.gdgoc.studyhub.domain.bestmember.repository;

import com.gdgoc.studyhub.domain.bestmember.entity.WeeklyBestMember;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeeklyBestMemberRepository extends JpaRepository<WeeklyBestMember, Long> {

    Optional<WeeklyBestMember> findByWeekId(Long weekId);

    boolean existsByWeekId(Long weekId);

    /** 주차를 지우기 전에 그 주차의 우수 멤버 기록을 먼저 지운다 (FK 때문). */
    @Modifying
    @Query("delete from WeeklyBestMember b where b.week.id = :weekId")
    void deleteAllByWeekId(@Param("weekId") Long weekId);
}