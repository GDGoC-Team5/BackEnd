package com.gdgoc.studyhub.domain.submission.repository;

import com.gdgoc.studyhub.domain.submission.entity.Submission;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

    boolean existsByWeekIdAndMemberId(Long weekId, Long memberId);

    boolean existsByWeekId(Long weekId);

    @Override
    @EntityGraph(attributePaths = {"week.study", "member.user"})
    Optional<Submission> findById(Long id);

    @EntityGraph(attributePaths = {"week", "member.user"})
    Optional<Submission> findByWeekIdAndMemberId(Long weekId, Long memberId);

    @EntityGraph(attributePaths = {"week", "member.user"})
    List<Submission> findAllByWeekIdOrderByCreatedAtDescIdDesc(Long weekId);
    @Modifying
    @Query("delete from Submission item where item.week.id in (select w.id from Week w where w.study.id = :studyId)")
    void deleteAllByStudyId(@Param("studyId") Long studyId);
}
