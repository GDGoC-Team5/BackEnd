package com.gdgoc.studyhub.global.config;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 미인증_요청은_401과_공통_에러_JSON을_반환한다() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("COMMON_401"));
    }

    @Test
    void 공개_엔드포인트는_인증_없이_접근할_수_있다() throws Exception {
        // 컨트롤러가 아직 없으므로 401/403 이 아니라 404 가 나오면 보안 필터를 통과한 것
        mockMvc.perform(post("/api/auth/login")).andExpect(status().isNotFound());
        mockMvc.perform(get("/v3/api-docs")).andExpect(status().isOk());
    }

    @Test
    void CSRF_토큰_없는_변경_요청은_403을_반환한다() throws Exception {
        mockMvc.perform(post("/api/studies").with(user("tester")))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("COMMON_403"));
    }

    @Test
    void CSRF_토큰이_있으면_보안_필터를_통과한다() throws Exception {
        mockMvc.perform(post("/api/studies").with(user("tester")).with(csrf()))
                .andExpect(status().isNotFound());
    }

    @Test
    void 응답에_XSRF_TOKEN_쿠키가_내려가고_JS에서_읽을_수_있다() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(cookie().exists("XSRF-TOKEN"))
                .andExpect(cookie().httpOnly("XSRF-TOKEN", false));
    }

    @Test
    void 허용된_Origin의_CORS_요청은_credentials를_허용한다() throws Exception {
        mockMvc.perform(options("/api/users/me")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"))
                .andExpect(header().string("Access-Control-Allow-Credentials", "true"));
    }

    @Test
    void 허용되지_않은_Origin의_CORS_요청은_거부된다() throws Exception {
        mockMvc.perform(options("/api/users/me")
                        .header("Origin", "https://evil.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }

    @Test
    void 비밀번호는_해시되어_저장되고_검증된다() {
        String encoded = passwordEncoder.encode("password1234");

        assertThat(encoded).isNotEqualTo("password1234");
        assertThat(passwordEncoder.matches("password1234", encoded)).isTrue();
    }
}
