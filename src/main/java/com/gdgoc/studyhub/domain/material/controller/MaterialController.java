package com.gdgoc.studyhub.domain.material.controller;

import com.gdgoc.studyhub.domain.material.dto.MaterialCreateRequest;
import com.gdgoc.studyhub.domain.material.dto.MaterialResponse;
import com.gdgoc.studyhub.domain.material.service.MaterialService;
import com.gdgoc.studyhub.global.common.ApiResponse;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping({"/api", "/api/v1"})
@RequiredArgsConstructor
@Tag(name = "Material", description = "주차별 세션 자료 링크")
public class MaterialController {

    private final MaterialService materialService;

    @PostMapping("/weeks/{weekId}/materials")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "세션 자료 등록", description = "해당 스터디의 운영진만 제목과 HTTP/HTTPS 링크를 등록할 수 있습니다.")
    public ApiResponse<MaterialResponse> create(@AuthenticationPrincipal CustomUserDetails principal,
                                                @PathVariable Long weekId,
                                                @Valid @RequestBody MaterialCreateRequest request) {
        return ApiResponse.ok(materialService.create(weekId, principal.getUserId(), request));
    }

    @GetMapping("/weeks/{weekId}/materials")
    @Operation(summary = "세션 자료 목록 조회", description = "해당 스터디의 멤버만 조회할 수 있습니다. 최신 등록순입니다.")
    public ApiResponse<List<MaterialResponse>> getAll(@AuthenticationPrincipal CustomUserDetails principal,
                                                     @PathVariable Long weekId) {
        return ApiResponse.ok(materialService.getAll(weekId, principal.getUserId()));
    }

    @GetMapping("/materials/{materialId}")
    @Operation(summary = "세션 자료 조회", description = "해당 스터디의 멤버에게 자료 제목과 링크를 JSON으로 반환합니다.")
    public ApiResponse<MaterialResponse> get(@AuthenticationPrincipal CustomUserDetails principal,
                                             @PathVariable Long materialId) {
        return ApiResponse.ok(materialService.get(materialId, principal.getUserId()));
    }

    @DeleteMapping("/materials/{materialId}")
    @Operation(summary = "세션 자료 삭제", description = "등록자에 관계없이 해당 스터디의 운영진만 삭제할 수 있습니다.")
    public ApiResponse<Void> delete(@AuthenticationPrincipal CustomUserDetails principal,
                                    @PathVariable Long materialId) {
        materialService.delete(materialId, principal.getUserId());
        return ApiResponse.ok();
    }
}
