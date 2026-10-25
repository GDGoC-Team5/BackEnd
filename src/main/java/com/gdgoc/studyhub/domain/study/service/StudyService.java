package com.gdgoc.studyhub.domain.study.service;

import com.gdgoc.studyhub.domain.study.dto.MyStudyResponse;
import com.gdgoc.studyhub.domain.study.dto.StudyCreateRequest;
import com.gdgoc.studyhub.domain.study.dto.StudyResponse;
import com.gdgoc.studyhub.domain.study.dto.StudyUpdateRequest;
import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.study.repository.StudyRepository;
import com.gdgoc.studyhub.domain.studymember.entity.MemberStatus;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.repository.StudyMemberRepository;
import com.gdgoc.studyhub.domain.studymember.service.StudyAuthorizationService;
import com.gdgoc.studyhub.domain.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyService {

    /** 초대 코드가 기존 코드와 겹칠 때 다시 뽑는 최대 횟수. 8자리라 실제로 겹칠 일은 거의 없다. */
    private static final int MAX_INVITE_CODE_ATTEMPTS = 5;

    private final StudyRepository studyRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final UserRepository userRepository;
    private final StudyAuthorizationService studyAuth;
    private final InviteCodeGenerator inviteCodeGenerator;

    /** 스터디 생성 + 생성자를 MANAGER 로 등록. 둘 중 하나라도 실패하면 둘 다 취소되도록 한 트랜잭션으로 묶는다. */
    @Transactional
    public StudyResponse create(Long userId, StudyCreateRequest request) {
        Study study = studyRepository.save(
                Study.create(request.name().trim(), request.description(), newInviteCode()));
        // getReferenceById: SELECT 없이 id 만 가진 프록시를 만든다. FK 만 필요할 때 쓴다.
        StudyMember manager = studyMemberRepository.save(
                StudyMember.manager(study, userRepository.getReferenceById(userId)));
        return StudyResponse.of(study, manager.getRole(), 1);
    }

    public List<MyStudyResponse> getMyStudies(Long userId) {
        return studyMemberRepository.findAllWithStudyByUserId(userId, MemberStatus.ACTIVE).stream()
                .map(MyStudyResponse::from)
                .toList();
    }

    public StudyResponse getStudy(Long studyId, Long userId) {
        StudyMember me = studyAuth.requireMember(studyId, userId);
        return toResponse(me);
    }

    @Transactional
    public StudyResponse update(Long studyId, Long userId, StudyUpdateRequest request) {
        StudyMember me = studyAuth.requireManager(studyId, userId);
        String name = request.name() == null ? null : request.name().trim();
        // 변경 감지(dirty checking): 엔티티 값만 바꾸면 트랜잭션이 끝날 때 UPDATE 가 나간다.
        me.getStudy().update(name, request.description());
        return toResponse(me);
    }

    private StudyResponse toResponse(StudyMember me) {
        long memberCount = studyMemberRepository.countByStudyIdAndStatus(me.getStudy().getId(), MemberStatus.ACTIVE);
        return StudyResponse.of(me.getStudy(), me.getRole(), memberCount);
    }

    /**
     * 겹치지 않는 초대 코드를 뽑는다. exists 확인과 저장 사이에 다른 요청이 같은 코드를 쓸 수도 있지만,
     * 그 경우는 DB UNIQUE 제약이 막는다 (확률이 극히 낮아 재시도까지는 하지 않는다).
     */
    private String newInviteCode() {
        for (int i = 0; i < MAX_INVITE_CODE_ATTEMPTS; i++) {
            String code = inviteCodeGenerator.generate();
            if (!studyRepository.existsByInviteCode(code)) {
                return code;
            }
        }
        throw new IllegalStateException("초대 코드 생성에 실패했습니다.");
    }
}
