package com.gdgoc.studyhub.domain.studymember.service;

import com.gdgoc.studyhub.domain.study.dto.StudyResponse;
import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.study.repository.StudyRepository;
import com.gdgoc.studyhub.domain.studymember.dto.StudyMemberResponse;
import com.gdgoc.studyhub.domain.studymember.entity.MemberStatus;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.exception.StudyMemberErrorCode;
import com.gdgoc.studyhub.domain.studymember.repository.StudyMemberRepository;
import com.gdgoc.studyhub.domain.user.repository.UserRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyMemberService {

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;
    private final StudyAuthorizationService studyAuth;

    /**
     * 초대 코드로 MEMBER 가입.
     * - 처음 가입: 새 행 INSERT
     * - 이미 ACTIVE: 409
     * - 탈퇴(LEFT)했던 멤버: 새 행이 아니라 기존 행을 ACTIVE 로 복구 ((study, user) UNIQUE 이기 때문)
     */
    @Transactional
    public StudyResponse join(Long userId, String inviteCode) {
        Study study = studyRepository.findByInviteCode(inviteCode.trim().toUpperCase(Locale.ROOT))
                .orElseThrow(() -> new BusinessException(StudyMemberErrorCode.INVALID_INVITE_CODE));

        Optional<StudyMember> existing = studyMemberRepository.findByStudyIdAndUserId(study.getId(), userId);
        StudyMember member;
        if (existing.isPresent()) {
            member = existing.get();
            if (member.isActive()) {
                throw new BusinessException(StudyMemberErrorCode.ALREADY_STUDY_MEMBER);
            }
            member.rejoin();
        } else {
            member = StudyMember.member(study, userRepository.getReferenceById(userId));
            try {
                // 같은 사람이 동시에 두 번 가입 요청하면 UNIQUE 제약에서 걸린다. flush 로 바로 INSERT 해서 여기서 409 로 바꾼다.
                studyMemberRepository.saveAndFlush(member);
            } catch (DataIntegrityViolationException e) {
                throw new BusinessException(StudyMemberErrorCode.ALREADY_STUDY_MEMBER);
            }
        }

        long memberCount = studyMemberRepository.countByStudyIdAndStatus(study.getId(), MemberStatus.ACTIVE);
        return StudyResponse.of(study, member.getRole(), memberCount);
    }

    /** 멤버 목록. 멤버만 볼 수 있다. ACTIVE 멤버만, MANAGER 를 먼저 보여준다. */
    public List<StudyMemberResponse> getMembers(Long studyId, Long userId) {
        studyAuth.requireMember(studyId, userId);
        return studyMemberRepository.findAllWithUserByStudyId(studyId, MemberStatus.ACTIVE).stream()
                .map(StudyMemberResponse::from)
                .toList();
    }

    /**
     * 탈퇴. 행은 지우지 않고 status 만 LEFT 로 바꾼다 (나중에 제출 기록 등이 이 멤버를 참조할 수 있다).
     * 정책: MANAGER(스터디 생성자)는 탈퇴할 수 없다. 스터디는 없어지지 않으므로 항상 관리자가 있어야 한다.
     */
    @Transactional
    public void leave(Long studyId, Long userId) {
        StudyMember me = studyAuth.requireMember(studyId, userId);
        if (me.isManager()) {
            throw new BusinessException(StudyMemberErrorCode.MANAGER_CANNOT_LEAVE);
        }
        me.leave();
    }
}
