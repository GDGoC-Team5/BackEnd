package com.gdgoc.studyhub.domain.submission.service;

import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.service.StudyAuthorizationService;
import com.gdgoc.studyhub.domain.submission.dto.SubmissionResponse;
import com.gdgoc.studyhub.domain.submission.dto.SubmissionWriteRequest;
import com.gdgoc.studyhub.domain.submission.entity.Submission;
import com.gdgoc.studyhub.domain.submission.exception.SubmissionErrorCode;
import com.gdgoc.studyhub.domain.submission.repository.SubmissionRepository;
import com.gdgoc.studyhub.domain.week.entity.AssignmentType;
import com.gdgoc.studyhub.domain.week.entity.Week;
import com.gdgoc.studyhub.domain.week.exception.WeekErrorCode;
import com.gdgoc.studyhub.domain.week.repository.WeekRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubmissionService {

    private final SubmissionRepository submissionRepository;
    private final WeekRepository weekRepository;
    private final StudyAuthorizationService studyAuth;

    @Transactional
    public SubmissionResponse create(Long weekId, Long userId, SubmissionWriteRequest request) {
        Week week = findWeek(weekId);
        StudyMember member = studyAuth.requireMember(week.getStudy().getId(), userId);
        requireLinkAssignment(week);
        if (submissionRepository.existsByWeekIdAndMemberId(weekId, member.getId())) {
            throw new BusinessException(SubmissionErrorCode.ALREADY_SUBMITTED);
        }

        Submission submission = Submission.create(week, member, request.url());
        try {
            // 동시 제출은 DB의 (week_id, member_id) UNIQUE 제약으로도 차단한다.
            submissionRepository.saveAndFlush(submission);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(SubmissionErrorCode.ALREADY_SUBMITTED);
        }
        return SubmissionResponse.from(submission);
    }

    public List<SubmissionResponse> getAll(Long weekId, Long userId) {
        Week week = findWeek(weekId);
        studyAuth.requireManager(week.getStudy().getId(), userId);
        return submissionRepository.findAllByWeekIdOrderByCreatedAtDescIdDesc(weekId).stream()
                .map(SubmissionResponse::from)
                .toList();
    }

    public SubmissionResponse getMine(Long weekId, Long userId) {
        Week week = findWeek(weekId);
        StudyMember member = studyAuth.requireMember(week.getStudy().getId(), userId);
        Submission submission = submissionRepository.findByWeekIdAndMemberId(weekId, member.getId())
                .orElseThrow(() -> new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));
        return SubmissionResponse.from(submission);
    }

    public SubmissionResponse get(Long submissionId, Long userId) {
        Submission submission = findSubmission(submissionId);
        StudyMember actor = studyAuth.requireMember(submission.getWeek().getStudy().getId(), userId);
        if (!isOwner(submission, actor) && !actor.isManager()) {
            throw new BusinessException(SubmissionErrorCode.NOT_ALLOWED_TO_READ);
        }
        return SubmissionResponse.from(submission);
    }

    @Transactional
    public SubmissionResponse update(Long submissionId, Long userId, SubmissionWriteRequest request) {
        Submission submission = findSubmission(submissionId);
        requireOwner(submission, userId);
        requireLinkAssignment(submission.getWeek());
        submission.updateUrl(request.url());
        submissionRepository.flush();
        return SubmissionResponse.from(submission);
    }

    @Transactional
    public void delete(Long submissionId, Long userId) {
        Submission submission = findSubmission(submissionId);
        requireOwner(submission, userId);
        submissionRepository.delete(submission);
    }

    @Transactional
    public SubmissionResponse check(Long submissionId, Long userId, boolean checked) {
        Submission submission = findSubmission(submissionId);
        studyAuth.requireManager(submission.getWeek().getStudy().getId(), userId);
        submission.changeChecked(checked);
        submissionRepository.flush();
        return SubmissionResponse.from(submission);
    }

    private void requireOwner(Submission submission, Long userId) {
        StudyMember actor = studyAuth.requireMember(submission.getWeek().getStudy().getId(), userId);
        if (!isOwner(submission, actor)) {
            throw new BusinessException(SubmissionErrorCode.NOT_SUBMISSION_OWNER);
        }
    }

    private boolean isOwner(Submission submission, StudyMember actor) {
        return submission.getMember().getId().equals(actor.getId());
    }

    private void requireLinkAssignment(Week week) {
        if (week.getAssignmentType() != AssignmentType.LINK) {
            throw new BusinessException(SubmissionErrorCode.UNSUPPORTED_ASSIGNMENT_TYPE);
        }
    }

    private Week findWeek(Long weekId) {
        return weekRepository.findById(weekId)
                .orElseThrow(() -> new BusinessException(WeekErrorCode.WEEK_NOT_FOUND));
    }

    private Submission findSubmission(Long submissionId) {
        return submissionRepository.findById(submissionId)
                .orElseThrow(() -> new BusinessException(SubmissionErrorCode.SUBMISSION_NOT_FOUND));
    }
}
