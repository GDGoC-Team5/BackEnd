package com.gdgoc.studyhub.domain.auth.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import org.springframework.mock.web.MockHttpSession;
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

    @Test
    void 로그인하면_세션으로_내_정보를_조회할_수_있다() throws Exception {
        saveUser("tester@studyhub.com", "password1234");

        MockHttpSession session = (MockHttpSession) login("tester@studyhub.com", "password1234")
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("tester@studyhub.com"))
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value("tester@studyhub.com"))
                .andExpect(jsonPath("$.data.password").doesNotExist());
    }

    @Test
    void 로그인에_성공하면_세션_ID가_바뀐다() throws Exception {
        saveUser("tester@studyhub.com", "password1234");
        MockHttpSession session = new MockHttpSession();
        String before = session.getId();

        String after = mockMvc.perform(post("/api/auth/login").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody("tester@studyhub.com", "password1234")))
                .andExpect(status().isOk())
                .andReturn().getRequest().getSession(false).getId();

        assertThat(after).isNotEqualTo(before);
    }

    @Test
    void 비밀번호가_틀리거나_없는_이메일이면_같은_401을_반환한다() throws Exception {
        saveUser("tester@studyhub.com", "password1234");

        login("tester@studyhub.com", "wrong-password")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));
        login("nobody@studyhub.com", "password1234")
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error.code").value("AUTH_401"));
    }

    @Test
    void 로그아웃하면_같은_세션으로_내_정보를_조회할_수_없다() throws Exception {
        saveUser("tester@studyhub.com", "password1234");
        MockHttpSession session = (MockHttpSession) login("tester@studyhub.com", "password1234")
                .andReturn().getRequest().getSession(false);

        mockMvc.perform(post("/api/auth/logout").session(session).with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        mockMvc.perform(get("/api/users/me").session(session))
                .andExpect(status().isUnauthorized());
    }

    private void saveUser(String email, String password) {
        userRepository.save(User.create(email, passwordEncoder.encode(password), "테스터"));
    }

    private ResultActions login(String email, String password) throws Exception {
        return mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody(email, password)));
    }

    private String loginBody(String email, String password) {
        return """
                {"email":"%s","password":"%s"}
                """.formatted(email, password);
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
