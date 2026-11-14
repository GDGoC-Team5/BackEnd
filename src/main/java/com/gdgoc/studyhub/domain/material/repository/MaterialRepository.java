package com.gdgoc.studyhub.domain.material.repository;

import com.gdgoc.studyhub.domain.material.entity.Material;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MaterialRepository extends JpaRepository<Material, Long> {

    boolean existsByWeekId(Long weekId);

    @Override
    @EntityGraph(attributePaths = {"week.study", "member.user"})
    Optional<Material> findById(Long id);

    @EntityGraph(attributePaths = {"week", "member.user"})
    List<Material> findAllByWeekIdOrderByCreatedAtDescIdDesc(Long weekId);
    @Modifying
    @Query("delete from Material item where item.week.id in (select w.id from Week w where w.study.id = :studyId)")
    void deleteAllByStudyId(@Param("studyId") Long studyId);
}
