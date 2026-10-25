package com.gdgoc.studyhub.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gdgoc.studyhub.domain.user.entity.User;
import com.gdgoc.studyhub.domain.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void 회원가입에_성공하면_201과_회원정보를_반환하고_비밀번호는_포함하지_않는다() throws Exception {
        // CSRF 토큰 없이 호출해도 통과해야 한다 (SecurityConfig 에서 signup 은 CSRF 제외)
        signup("tester@studyhub.com", "password1234", "테스터")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").isNumber())
                .andExpect(jsonPath("$.data.email").value("tester@studyhub.com"))
                .andExpect(jsonPath("$.data.name").value("테스터"))
                .andExpect(jsonPath("$.data.systemRole").value("USER"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void 비밀번호는_원문이_아닌_해시로_저장된다() throws Exception {
        signup("tester@studyhub.com", "password1234", "테스터");

        User user = userRepository.findByEmail("tester@studyhub.com").orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("password1234").startsWith("{bcrypt}");
        assertThat(passwordEncoder.matches("password1234", user.getPassword())).isTrue();
    }

    @Test
    void 이미_가입된_이메일이면_409를_반환한다() throws Exception {
        signup("tester@studyhub.com", "password1234", "테스터");

        signup("tester@studyhub.com", "otherpass5678", "다른사람")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value("USER_409"));
    }

    @Test
    void 대소문자만_다른_이메일도_중복으로_처리한다() throws Exception {
        signup("tester@studyhub.com", "password1234", "테스터");

        signup("Tester@StudyHub.com", "password1234", "테스터")
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("USER_409"));
    }

    @Test
    void 이메일_형식이_아니면_400을_반환한다() throws Exception {
        signup("not-an-email", "password1234", "테스터")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"))
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("email"));
    }

    @Test
    void 비밀번호가_8자_미만이면_400을_반환한다() throws Exception {
        signup("tester@studyhub.com", "short", "테스터")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("password"));
    }

    @Test
    void 비밀번호에_한글이나_공백이_있으면_400을_반환한다() throws Exception {
        signup("tester@studyhub.com", "비밀번호 1234", "테스터")
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.fieldErrors[0].field").value("password"));
    }

    private ResultActions signup(String email, String password, String name) throws Exception {
        String body = """
                {"email":"%s","password":"%s","name":"%s"}
                """.formatted(email, password, name);
        return mockMvc.perform(post("/api/auth/signup")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }
}
