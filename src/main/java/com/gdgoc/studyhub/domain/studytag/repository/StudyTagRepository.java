package com.gdgoc.studyhub.domain.studytag.repository;

import com.gdgoc.studyhub.domain.studytag.entity.StudyTag;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StudyTagRepository extends JpaRepository<StudyTag, Long> {

    List<StudyTag> findAllByStudyIdOrderByIdAsc(Long studyId);

    boolean existsByStudyIdAndName(Long studyId, String name);

    /** 다른 스터디의 태그 id 로 지우지 못하도록 studyId 도 함께 조건에 건다. */
    Optional<StudyTag> findByIdAndStudyId(Long id, Long studyId);
}
