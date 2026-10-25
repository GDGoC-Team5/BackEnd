package com.gdgoc.studyhub.domain.studytag.service;

import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.service.StudyAuthorizationService;
import com.gdgoc.studyhub.domain.studytag.dto.StudyTagResponse;
import com.gdgoc.studyhub.domain.studytag.entity.StudyTag;
import com.gdgoc.studyhub.domain.studytag.exception.StudyTagErrorCode;
import com.gdgoc.studyhub.domain.studytag.repository.StudyTagRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class StudyTagService {

    private final StudyTagRepository studyTagRepository;
    private final StudyAuthorizationService studyAuth;

    /** MANAGER 만. 같은 스터디 안에서 같은 이름은 409. */
    @Transactional
    public StudyTagResponse create(Long studyId, Long userId, String name) {
        StudyMember me = studyAuth.requireManager(studyId, userId);
        String tagName = name.trim();
        if (studyTagRepository.existsByStudyIdAndName(studyId, tagName)) {
            throw new BusinessException(StudyTagErrorCode.DUPLICATE_TAG);
        }
        StudyTag tag = StudyTag.create(me.getStudy(), tagName);
        try {
            studyTagRepository.saveAndFlush(tag);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(StudyTagErrorCode.DUPLICATE_TAG);
        }
        return StudyTagResponse.from(tag);
    }

    /** 멤버만 */
    public List<StudyTagResponse> getTags(Long studyId, Long userId) {
        studyAuth.requireMember(studyId, userId);
        return studyTagRepository.findAllByStudyIdOrderByIdAsc(studyId).stream()
                .map(StudyTagResponse::from)
                .toList();
    }

    /** MANAGER 만. 다른 스터디의 태그 id 를 넣어도 이 스터디 것이 아니면 404 다. */
    @Transactional
    public void delete(Long studyId, Long userId, Long tagId) {
        studyAuth.requireManager(studyId, userId);
        StudyTag tag = studyTagRepository.findByIdAndStudyId(tagId, studyId)
                .orElseThrow(() -> new BusinessException(StudyTagErrorCode.TAG_NOT_FOUND));
        studyTagRepository.delete(tag);
    }
}
