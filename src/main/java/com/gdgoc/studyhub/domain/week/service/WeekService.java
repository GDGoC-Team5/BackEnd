package com.gdgoc.studyhub.domain.week.service;

import com.gdgoc.studyhub.domain.bestmember.repository.WeeklyBestMemberRepository;
import com.gdgoc.studyhub.domain.material.repository.MaterialRepository;
import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.submission.repository.SubmissionRepository;
import com.gdgoc.studyhub.domain.studymember.service.StudyAuthorizationService;
import com.gdgoc.studyhub.domain.week.dto.WeekCreateRequest;
import com.gdgoc.studyhub.domain.week.dto.WeekResponse;
import com.gdgoc.studyhub.domain.week.dto.WeekSummaryResponse;
import com.gdgoc.studyhub.domain.week.dto.WeekUpdateRequest;
import com.gdgoc.studyhub.domain.week.entity.Week;
import com.gdgoc.studyhub.domain.week.exception.WeekErrorCode;
import com.gdgoc.studyhub.domain.week.repository.WeekRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import com.gdgoc.studyhub.global.exception.CommonErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WeekService {

    private final WeekRepository weekRepository;
    private final WeeklyBestMemberRepository bestMemberRepository;
    private final SubmissionRepository submissionRepository;
    private final MaterialRepository materialRepository;
    private final StudyAuthorizationService studyAuth;

    /** 주차 생성. MANAGER 만 가능하고, 같은 스터디에서 weekNo 가 겹치면 409. */
    @Transactional
    public WeekResponse create(Long studyId, Long userId, WeekCreateRequest request) {
        Study study = studyAuth.requireManager(studyId, userId).getStudy();

        if (weekRepository.existsByStudyIdAndWeekNo(studyId, request.weekNo())) {
            throw new BusinessException(WeekErrorCode.DUPLICATE_WEEK_NO);
        }

        Week week = Week.create(study, request.weekNo(), request.title().trim(), request.assignmentDescription(),
                request.sessionAt(), request.location(), request.deadline(), request.assignmentType());
        try {
            // 동시에 같은 번호로 두 번 요청하면 UNIQUE 제약에서 걸린다. flush 로 바로 INSERT 해서 여기서 409 로 바꾼다.
            weekRepository.saveAndFlush(week);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(WeekErrorCode.DUPLICATE_WEEK_NO);
        }
        return WeekResponse.from(week);
    }

    /** 주차 목록. 스터디 멤버만 볼 수 있다. 주차 번호 오름차순. 주차가 없으면 빈 배열. */
    public List<WeekSummaryResponse> getWeeks(Long studyId, Long userId) {
        studyAuth.requireMember(studyId, userId);
        return weekRepository.findAllByStudyIdOrderByWeekNoAsc(studyId).stream()
                .map(WeekSummaryResponse::from)
                .toList();
    }

    /** 주차 상세. 주차가 속한 스터디의 멤버만 볼 수 있다. */
    public WeekResponse getWeek(Long weekId, Long userId) {
        Week week = findWeek(weekId);
        studyAuth.requireMember(week.getStudy().getId(), userId);
        return WeekResponse.from(week);
    }

    /** 주차 수정. MANAGER 만 가능하다. 보낸 필드만 바꾸고, weekNo 를 바꿀 때 다른 주차와 겹치면 409. */
    @Transactional
    public WeekResponse update(Long weekId, Long userId, WeekUpdateRequest request) {
        Week week = findWeek(weekId);
        Long studyId = week.getStudy().getId();
        studyAuth.requireManager(studyId, userId);

        String title = request.title() == null ? null : request.title().trim();
        if (title != null && title.isEmpty()) {
            throw new BusinessException(CommonErrorCode.INVALID_INPUT, "제목은 비워둘 수 없습니다.");
        }
        if (request.weekNo() != null && request.weekNo() != week.getWeekNo()
                && weekRepository.existsByStudyIdAndWeekNo(studyId, request.weekNo())) {
            throw new BusinessException(WeekErrorCode.DUPLICATE_WEEK_NO);
        }

        week.update(request.weekNo(), title, request.assignmentDescription(),
                request.sessionAt(), request.location(), request.deadline());
        try {
            weekRepository.saveAndFlush(week);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(WeekErrorCode.DUPLICATE_WEEK_NO);
        }
        return WeekResponse.from(week);
    }

    /**
     * 주차 삭제. MANAGER 만 가능하다. 그 주차의 우수 멤버 기록도 함께 지운다.
     * 제출물이나 세션 자료가 있으면 먼저 삭제해야 한다.
     */
    @Transactional
    public void delete(Long weekId, Long userId) {
        Week week = findWeek(weekId);
        studyAuth.requireManager(week.getStudy().getId(), userId);
        if (submissionRepository.existsByWeekId(weekId)) {
            throw new BusinessException(WeekErrorCode.HAS_SUBMISSIONS);
        }
        if (materialRepository.existsByWeekId(weekId)) {
            throw new BusinessException(WeekErrorCode.HAS_MATERIALS);
        }
        bestMemberRepository.deleteAllByWeekId(weekId);
        weekRepository.delete(week);
    }

    private Week findWeek(Long weekId) {
        return weekRepository.findById(weekId)
                .orElseThrow(() -> new BusinessException(WeekErrorCode.WEEK_NOT_FOUND));
    }
}
