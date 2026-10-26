package com.gdgoc.studyhub.domain.studymember.repository;

import com.gdgoc.studyhub.domain.studymember.entity.MemberStatus;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.entity.StudyRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface StudyMemberRepository extends JpaRepository<StudyMember, Long> {

    /** 상태와 상관없이 (study, user) 행을 찾는다. 재가입·탈퇴 처리에 쓴다. */
    Optional<StudyMember> findByStudyIdAndUserId(Long studyId, Long userId);

    /** 권한 체크용. ACTIVE 인 멤버만 찾는다. */
    Optional<StudyMember> findByStudyIdAndUserIdAndStatus(Long studyId, Long userId, MemberStatus status);

    long countByStudyIdAndStatus(Long studyId, MemberStatus status);

    long countByStudyIdAndRoleAndStatus(Long studyId, StudyRole role, MemberStatus status);

    /** 내가 속한 스터디 목록. study 를 함께 가져와 N+1 쿼리를 막는다. */
    @Query("""
            select m from StudyMember m join fetch m.study
            where m.user.id = :userId and m.status = :status
            order by m.id desc
            """)
    List<StudyMember> findAllWithStudyByUserId(@Param("userId") Long userId, @Param("status") MemberStatus status);

    /** 스터디 멤버 목록. user 를 함께 가져오고, MANAGER 가 먼저 오도록 정렬한다 (enum 이름순 MANAGER < MEMBER). */
    @Query("""
            select m from StudyMember m join fetch m.user
            where m.study.id = :studyId and m.status = :status
            order by m.role asc, m.id asc
            """)
    List<StudyMember> findAllWithUserByStudyId(@Param("studyId") Long studyId, @Param("status") MemberStatus status);
}
