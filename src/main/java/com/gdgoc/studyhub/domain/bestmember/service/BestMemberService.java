package com.gdgoc.studyhub.domain.bestmember.service;

import com.gdgoc.studyhub.domain.bestmember.dto.BestMemberCreateRequest;
import com.gdgoc.studyhub.domain.bestmember.dto.BestMemberResponse;
import com.gdgoc.studyhub.domain.bestmember.dto.BestMemberUpdateRequest;
import com.gdgoc.studyhub.domain.bestmember.entity.WeeklyBestMember;
import com.gdgoc.studyhub.domain.bestmember.exception.BestMemberErrorCode;
import com.gdgoc.studyhub.domain.bestmember.repository.WeeklyBestMemberRepository;
import com.gdgoc.studyhub.domain.studymember.entity.MemberStatus;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.repository.StudyMemberRepository;
import com.gdgoc.studyhub.domain.studymember.service.StudyAuthorizationService;
import com.gdgoc.studyhub.domain.week.entity.Week;
import com.gdgoc.studyhub.domain.week.exception.WeekErrorCode;
import com.gdgoc.studyhub.domain.week.repository.WeekRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BestMemberService {

    private final WeeklyBestMemberRepository bestMemberRepository;
    private final WeekRepository weekRepository;
    private final StudyMemberRepository studyMemberRepository;
    private final StudyAuthorizationService studyAuth;

    /** 우수 멤버 조회. 로그인한 사람은 누구나 볼 수 있다. 아직 선정되지 않았으면 null. */
    public BestMemberResponse get(Long weekId) {
        findWeek(weekId);
        return bestMemberRepository.findByWeekId(weekId)
                .map(BestMemberResponse::from)
                .orElse(null);
    }

    /**
     * 우수 멤버 선정. MANAGER 만 가능하고, 주차에는 한 명만 뽑을 수 있다 (이미 있으면 409).
     * TODO: 제출(submission) 엔티티가 생기면 "해당 주차에 제출한 멤버만 선정 가능" 규칙을 추가한다.
     */
    @Transactional
    public BestMemberResponse select(Long weekId, Long actorUserId, BestMemberCreateRequest request) {
        Week week = findWeek(weekId);
        Long studyId = week.getStudy().getId();
        StudyMember manager = studyAuth.requireManager(studyId, actorUserId);
        StudyMember target = findTarget(studyId, request.userId());

        if (bestMemberRepository.existsByWeekId(weekId)) {
            throw new BusinessException(BestMemberErrorCode.ALREADY_SELECTED);
        }

        WeeklyBestMember best = WeeklyBestMember.create(week, target, manager, request.reason());
        try {
            // 동시에 두 번 선정하면 UNIQUE(week_id) 에서 걸린다. flush 로 바로 INSERT 해서 여기서 409 로 바꾼다.
            bestMemberRepository.saveAndFlush(best);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(BestMemberErrorCode.ALREADY_SELECTED);
        }
        return BestMemberResponse.from(best);
    }

    /** 우수 멤버 수정. MANAGER 만 가능하다. 보낸 필드만 바꾸고, 선정된 기록이 없으면 404. */
    @Transactional
    public BestMemberResponse update(Long weekId, Long actorUserId, BestMemberUpdateRequest request) {
        Week week = findWeek(weekId);
        Long studyId = week.getStudy().getId();
        studyAuth.requireManager(studyId, actorUserId);

        WeeklyBestMember best = bestMemberRepository.findByWeekId(weekId)
                .orElseThrow(() -> new BusinessException(BestMemberErrorCode.BEST_MEMBER_NOT_FOUND));

        StudyMember target = request.userId() == null ? null : findTarget(studyId, request.userId());
        best.change(target, request.reason());
        bestMemberRepository.saveAndFlush(best);
        return BestMemberResponse.from(best);
    }

    /** 우수 멤버 선정 취소. MANAGER 만 가능하고, 선정된 기록이 없으면 404. */
    @Transactional
    public void cancel(Long weekId, Long actorUserId) {
        Week week = findWeek(weekId);
        studyAuth.requireManager(week.getStudy().getId(), actorUserId);

        WeeklyBestMember best = bestMemberRepository.findByWeekId(weekId)
                .orElseThrow(() -> new BusinessException(BestMemberErrorCode.BEST_MEMBER_NOT_FOUND));
        bestMemberRepository.delete(best);
    }

    private Week findWeek(Long weekId) {
        return weekRepository.findById(weekId)
                .orElseThrow(() -> new BusinessException(WeekErrorCode.WEEK_NOT_FOUND));
    }

    /** 같은 스터디의 ACTIVE 멤버만 우수 멤버가 될 수 있다. */
    private StudyMember findTarget(Long studyId, Long userId) {
        return studyMemberRepository.findByStudyIdAndUserIdAndStatus(studyId, userId, MemberStatus.ACTIVE)
                .orElseThrow(() -> new BusinessException(BestMemberErrorCode.TARGET_NOT_STUDY_MEMBER));
    }
}