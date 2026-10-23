package com.gdgoc.studyhub.global.exception;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gdgoc.studyhub.global.common.ApiResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

class GlobalExceptionHandlerTest {

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void 성공_응답은_success_true와_data를_반환한다() throws Exception {
        mockMvc.perform(get("/test/ok"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").value("ok"))
                .andExpect(jsonPath("$.error").doesNotExist());
    }

    @Test
    void BusinessException은_ErrorCode의_상태코드와_코드로_변환된다() throws Exception {
        mockMvc.perform(get("/test/business"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_403"))
                .andExpect(jsonPath("$.error.message").value("스터디 매니저만 가능합니다."));
    }

    @Test
    void Valid_검증_실패는_400과_필드별_사유를_반환한다() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_400"))
                .andExpect(jsonPath("$.error.fieldErrors", hasSize(1)))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("name"));
    }

    @Test
    void 잘못된_JSON은_400을_반환한다() throws Exception {
        mockMvc.perform(post("/test/valid")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{invalid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @Test
    void DataIntegrityViolationException은_409를_반환하고_SQL을_노출하지_않는다() throws Exception {
        mockMvc.perform(get("/test/duplicate"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_409"))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.DUPLICATE_RESOURCE.getMessage()));
    }

    @Test
    void AccessDeniedException은_500이_아닌_403을_반환한다() throws Exception {
        mockMvc.perform(get("/test/denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("COMMON_403"));
    }

    @Test
    void 처리되지_않은_예외는_500과_일반_메시지를_반환한다() throws Exception {
        mockMvc.perform(get("/test/unknown"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.error.code").value("COMMON_500"))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getMessage()));
    }

    record TestRequest(@NotBlank String name) {
    }

    @RestController
    static class TestController {

        @GetMapping("/test/ok")
        ApiResponse<String> ok() {
            return ApiResponse.ok("ok");
        }

        @GetMapping("/test/business")
        void business() {
            throw new BusinessException(ErrorCode.FORBIDDEN, "스터디 매니저만 가능합니다.");
        }

        @PostMapping("/test/valid")
        void valid(@Valid @RequestBody TestRequest request) {
        }

        @GetMapping("/test/duplicate")
        void duplicate() {
            throw new DataIntegrityViolationException("Duplicate entry 'a@a.com' for key 'users.email'");
        }

        @GetMapping("/test/denied")
        void denied() {
            throw new AccessDeniedException("denied");
        }

        @GetMapping("/test/unknown")
        void unknown() {
            throw new IllegalStateException("secret internal detail");
        }
    }
}
