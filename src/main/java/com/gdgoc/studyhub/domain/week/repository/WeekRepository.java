package com.gdgoc.studyhub.domain.week.repository;

import com.gdgoc.studyhub.domain.week.entity.Week;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WeekRepository extends JpaRepository<Week, Long> {

    boolean existsByStudyIdAndWeekNo(Long studyId, int weekNo);

    List<Week> findAllByStudyIdOrderByWeekNoAsc(Long studyId);
    @Modifying
    @Query("delete from Week item where item.study.id = :studyId")
    void deleteAllByStudyId(@Param("studyId") Long studyId);
}
