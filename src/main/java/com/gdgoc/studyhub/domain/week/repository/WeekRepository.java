package com.gdgoc.studyhub.domain.week.repository;

import com.gdgoc.studyhub.domain.week.entity.Week;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WeekRepository extends JpaRepository<Week, Long> {

    boolean existsByStudyIdAndWeekNo(Long studyId, int weekNo);

    List<Week> findAllByStudyIdOrderByWeekNoAsc(Long studyId);
}