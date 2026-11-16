package com.gdgoc.studyhub.domain.material.service;

import com.gdgoc.studyhub.domain.material.dto.MaterialCreateRequest;
import com.gdgoc.studyhub.domain.material.dto.MaterialResponse;
import com.gdgoc.studyhub.domain.material.entity.Material;
import com.gdgoc.studyhub.domain.material.exception.MaterialErrorCode;
import com.gdgoc.studyhub.domain.material.repository.MaterialRepository;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.studymember.service.StudyAuthorizationService;
import com.gdgoc.studyhub.domain.week.entity.Week;
import com.gdgoc.studyhub.domain.week.exception.WeekErrorCode;
import com.gdgoc.studyhub.domain.week.repository.WeekRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final WeekRepository weekRepository;
    private final StudyAuthorizationService studyAuth;

    @Transactional
    public MaterialResponse create(Long weekId, Long userId, MaterialCreateRequest request) {
        Week week = findWeek(weekId);
        StudyMember manager = studyAuth.requireManager(week.getStudy().getId(), userId);
        Material material = Material.create(week, manager, request.title(), request.url());
        materialRepository.saveAndFlush(material);
        return MaterialResponse.from(material);
    }

    public List<MaterialResponse> getAll(Long weekId, Long userId) {
        Week week = findWeek(weekId);
        studyAuth.requireMember(week.getStudy().getId(), userId);
        return materialRepository.findAllByWeekIdOrderByCreatedAtDescIdDesc(weekId).stream()
                .map(MaterialResponse::from)
                .toList();
    }

    public MaterialResponse get(Long materialId, Long userId) {
        Material material = findMaterial(materialId);
        studyAuth.requireMember(material.getWeek().getStudy().getId(), userId);
        return MaterialResponse.from(material);
    }

    @Transactional
    public void delete(Long materialId, Long userId) {
        Material material = findMaterial(materialId);
        studyAuth.requireManager(material.getWeek().getStudy().getId(), userId);
        materialRepository.delete(material);
    }

    private Week findWeek(Long weekId) {
        return weekRepository.findById(weekId)
                .orElseThrow(() -> new BusinessException(WeekErrorCode.WEEK_NOT_FOUND));
    }

    private Material findMaterial(Long materialId) {
        return materialRepository.findById(materialId)
                .orElseThrow(() -> new BusinessException(MaterialErrorCode.MATERIAL_NOT_FOUND));
    }
}
