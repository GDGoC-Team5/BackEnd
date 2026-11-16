package com.gdgoc.studyhub.domain.studymember.service;

import com.gdgoc.studyhub.domain.studymember.entity.MemberStatus;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.exception.StudyMemberErrorCode;
import com.gdgoc.studyhub.domain.studymember.repository.StudyMemberRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 스터디별 권한 체크 모듈. "이 사용자가 이 스터디의 MEMBER/MANAGER 인가"를 확인한다. ACTIVE 상태만 인정한다.
 * <p>
 * 권한이 스터디마다 달라서 SecurityConfig 의 URL 규칙(authenticated 등)으로는 표현할 수 없다.
 * URL 규칙은 "로그인했는가"까지만 보고, 스터디 권한은 이 모듈로 확인한다.
 *
 * <h3>사용법 1 — 서비스에서 직접 호출 (권장)</h3>
 * <pre>
 * StudyMember me = studyAuth.requireManager(studyId, userId);   // 아니면 403
 * </pre>
 *
 * <h3>사용법 2 — 컨트롤러에 @PreAuthorize</h3>
 * <pre>
 * &#64;PreAuthorize("@studyAuth.isManager(#studyId, authentication)")
 * &#64;DeleteMapping("/api/studies/{studyId}/weeks/{weekId}")
 * public ApiResponse&lt;Void&gt; delete(@PathVariable Long studyId, ...) { ... }
 * </pre>
 * {@code @studyAuth} 는 이 빈의 이름, {@code #studyId} 는 메서드 파라미터, {@code authentication} 은 현재 로그인 정보다.
 * false 면 시큐리티가 AccessDeniedException 을 던지고 공통 403(COMMON_403)으로 응답한다.
 */
@Component("studyAuth")
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyAuthorizationService {

    private final StudyMemberRepository studyMemberRepository;

    public boolean isMember(Long studyId, Long userId) {
        return findActiveMember(studyId, userId).isPresent();
    }

    public boolean isManager(Long studyId, Long userId) {
        return findActiveMember(studyId, userId).filter(StudyMember::isManager).isPresent();
    }

    /** @PreAuthorize 에서 쓰는 버전. 로그인 정보에서 userId 를 꺼내 확인한다. */
    public boolean isMember(Long studyId, Authentication authentication) {
        Long userId = userIdOf(authentication);
        return userId != null && isMember(studyId, userId);
    }

    /** @PreAuthorize 에서 쓰는 버전. 로그인 정보에서 userId 를 꺼내 확인한다. */
    public boolean isManager(Long studyId, Authentication authentication) {
        Long userId = userIdOf(authentication);
        return userId != null && isManager(studyId, userId);
    }

    /**
     * ACTIVE 멤버가 아니면 403. 통과하면 해당 멤버 정보를 돌려준다 (getStudy() 로 스터디도 바로 쓸 수 있다).
     * 스터디가 아예 없어도 404 가 아니라 403 이다. 멤버가 아닌 사람에게 스터디 존재 여부를 알려주지 않기 위해서다.
     */
    public StudyMember requireMember(Long studyId, Long userId) {
        return findActiveMember(studyId, userId)
                .orElseThrow(() -> new BusinessException(StudyMemberErrorCode.NOT_STUDY_MEMBER));
    }

    /** ACTIVE 멤버가 아니면 403(STUDY_403_MEMBER), 멤버지만 MANAGER 가 아니면 403(STUDY_403_MANAGER). */
    public StudyMember requireManager(Long studyId, Long userId) {
        StudyMember member = requireMember(studyId, userId);
        if (!member.isManager()) {
            throw new BusinessException(StudyMemberErrorCode.NOT_STUDY_MANAGER);
        }
        return member;
    }

    private Optional<StudyMember> findActiveMember(Long studyId, Long userId) {
        return studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, userId, MemberStatus.ACTIVE);
    }

    private Long userIdOf(Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof CustomUserDetails principal) {
            return principal.getUserId();
        }
        return null;
    }
}
