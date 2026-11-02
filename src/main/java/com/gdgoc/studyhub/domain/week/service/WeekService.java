package com.gdgoc.studyhub.domain.week.service;

import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.studymember.service.StudyAuthorizationService;
import com.gdgoc.studyhub.domain.week.dto.WeekCreateRequest;
import com.gdgoc.studyhub.domain.week.dto.WeekResponse;
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
public class WeekService {

    private final WeekRepository weekRepository;
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
}