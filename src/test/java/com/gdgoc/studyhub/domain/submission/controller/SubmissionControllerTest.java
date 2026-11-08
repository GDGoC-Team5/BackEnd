package com.gdgoc.studyhub.domain.submission.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.submission.entity.Submission;
import com.gdgoc.studyhub.domain.submission.repository.SubmissionRepository;
import com.gdgoc.studyhub.domain.user.entity.User;
import com.gdgoc.studyhub.domain.week.entity.AssignmentType;
import com.gdgoc.studyhub.domain.week.entity.Week;
import com.gdgoc.studyhub.global.security.CustomUserDetails;
import com.jayway.jsonpath.JsonPath;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class SubmissionControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private SubmissionRepository submissionRepository;

    private Study study;
    private Week week;
    private User manager;
    private User owner;
    private User peer;
    private User outsider;
    private StudyMember ownerMember;

    @BeforeEach
    void setUp() {
        study = persist(Study.create("제출 API 테스트", "", "SUBAPI01"));
        manager = createUser("manager-sub@example.com");
        owner = createUser("owner-sub@example.com");
        peer = createUser("peer-sub@example.com");
        outsider = createUser("outside-sub@example.com");
        persist(StudyMember.manager(study, manager));
        ownerMember = persist(StudyMember.member(study, owner));
        persist(StudyMember.member(study, peer));
        week = createWeek(1, AssignmentType.LINK);
        entityManager.flush();
    }

    @Test
    void 제출부터_조회_확인_수정_삭제까지_7개_API가_연결된다() throws Exception {
        String body = create("https://example.com/note").andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.checked").value(false))
                .andExpect(jsonPath("$.data.userId").value(owner.getId()))
                .andExpect(jsonPath("$.data.submittedAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.data.id")).longValue();

        mockMvc.perform(get(weekPath()).with(user(details(manager))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get(weekPath() + "/me").with(user(details(owner))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.id").value(id));
        mockMvc.perform(get(itemPath(id)).with(user(details(owner))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.url").value("https://example.com/note"));
        mockMvc.perform(get(itemPath(id)).with(user(details(manager))))
                .andExpect(status().isOk());

        check(id, true).andExpect(status().isOk()).andExpect(jsonPath("$.data.checked").value(true));
        mockMvc.perform(patch(itemPath(id)).with(user(details(owner))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://example.com/revised\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.checked").value(false))
                .andExpect(jsonPath("$.data.url").value("https://example.com/revised"));

        mockMvc.perform(delete(itemPath(id)).with(user(details(owner))).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true));
        assertThat(submissionRepository.existsById(id)).isFalse();
        mockMvc.perform(get(itemPath(id)).with(user(details(owner))))
                .andExpect(status().isNotFound());
    }

    @Test
    void 중복_제출은_409이고_삭제한_뒤에는_다시_제출할_수_있다() throws Exception {
        Submission first = saveSubmission();
        create("https://example.com/another").andExpect(status().isConflict())
                .andExpect(jsonPath("$.error.code").value("SUBMISSION_409"));
        mockMvc.perform(delete(itemPath(first.getId())).with(user(details(owner))).with(csrf()))
                .andExpect(status().isOk());
        create("https://example.com/again").andExpect(status().isCreated());
    }

    @Test
    void 일반_멤버는_전체_목록이나_타인의_제출물을_조회할_수_없다() throws Exception {
        Submission submission = saveSubmission();
        mockMvc.perform(get(weekPath()).with(user(details(owner))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(itemPath(submission.getId())).with(user(details(peer))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error.code").value("SUBMISSION_403_READ"));
    }

    @Test
    void 타인과_운영진은_제출자의_링크를_수정하거나_삭제할_수_없다() throws Exception {
        Submission submission = saveSubmission();
        for (User actor : new User[]{peer, manager}) {
            mockMvc.perform(patch(itemPath(submission.getId())).with(user(details(actor))).with(csrf())
                            .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://example.com/hijack\"}"))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.error.code").value("SUBMISSION_403_OWNER"));
            mockMvc.perform(delete(itemPath(submission.getId())).with(user(details(actor))).with(csrf()))
                    .andExpect(status().isForbidden());
        }
        assertThat(submissionRepository.findById(submission.getId()).orElseThrow().getUrl())
                .isEqualTo("https://example.com/note");
    }

    @Test
    void 운영진만_확인과_확인취소를_할_수_있고_같은_링크로_수정하면_확인이_유지된다() throws Exception {
        Submission submission = saveSubmission();
        mockMvc.perform(patch(itemPath(submission.getId()) + "/check").with(user(details(owner))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"checked\":true}"))
                .andExpect(status().isForbidden());
        check(submission.getId(), true).andExpect(status().isOk());
        mockMvc.perform(patch(itemPath(submission.getId())).with(user(details(owner))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://example.com/note\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.checked").value(true));
        check(submission.getId(), false).andExpect(status().isOk())
                .andExpect(jsonPath("$.data.checked").value(false));
    }

    @Test
    void 확인_상태를_생략하면_400이다() throws Exception {
        Submission submission = saveSubmission();
        mockMvc.perform(patch(itemPath(submission.getId()) + "/check").with(user(details(manager))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void 외부인과_탈퇴한_멤버는_제출하거나_조회할_수_없다() throws Exception {
        Submission submission = saveSubmission();
        mockMvc.perform(post(weekPath()).with(user(details(outsider))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"https://example.com/x\"}"))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(itemPath(submission.getId())).with(user(details(outsider))))
                .andExpect(status().isForbidden());
        ownerMember.leave();
        entityManager.flush();
        mockMvc.perform(get(weekPath() + "/me").with(user(details(owner))))
                .andExpect(status().isForbidden());
    }

    @Test
    void 로그인과_CSRF_보호가_적용된다() throws Exception {
        mockMvc.perform(get(weekPath() + "/me")).andExpect(status().isUnauthorized());
        mockMvc.perform(post(weekPath()).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/note\"}"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(post(weekPath()).with(user(details(owner))).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/note\"}"))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-a-url", "ftp://example.com/file", "https://", "https://example.com/a b"})
    void 잘못된_링크는_400이다(String url) throws Exception {
        create(url).andExpect(status().isBadRequest());
    }

    @Test
    void 링크_누락과_길이_초과는_400이다() throws Exception {
        mockMvc.perform(post(weekPath()).with(user(details(owner))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
        create("https://example.com/" + "a".repeat(2048)).andExpect(status().isBadRequest());
    }

    @ParameterizedTest
    @EnumSource(value = AssignmentType.class, names = {"FILE", "LINK_AND_FILE"})
    void 파일이_필요한_주차에는_링크만_제출할_수_없다(AssignmentType type) throws Exception {
        Week fileWeek = createWeek(2, type);
        mockMvc.perform(post("/api/v1/weeks/" + fileWeek.getId() + "/submissions")
                        .with(user(details(owner))).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/note\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("SUBMISSION_400_TYPE"));
    }

    @Test
    void 미제출과_없는_주차와_없는_제출물을_구분한다() throws Exception {
        mockMvc.perform(get(weekPath() + "/me").with(user(details(owner))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("SUBMISSION_404"));
        mockMvc.perform(get("/api/v1/weeks/" + Long.MAX_VALUE + "/submissions/me").with(user(details(owner))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("WEEK_404"));
        mockMvc.perform(get(itemPath(Long.MAX_VALUE)).with(user(details(owner))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("SUBMISSION_404"));
        mockMvc.perform(get(weekPath()).with(user(details(manager))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
    }

    @Test
    void 제출물이_있는_주차_삭제는_409이며_빈_주차는_삭제할_수_있다() throws Exception {
        saveSubmission();
        mockMvc.perform(delete("/api/weeks/" + week.getId()).with(user(details(manager))).with(csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.code").value("WEEK_409_SUBMISSIONS"));
        Week emptyWeek = createWeek(2, AssignmentType.LINK);
        mockMvc.perform(delete("/api/weeks/" + emptyWeek.getId()).with(user(details(manager))).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 기존_api_접두사로도_동일하게_제출할_수_있다() throws Exception {
        mockMvc.perform(post("/api/weeks/" + week.getId() + "/submissions")
                        .with(user(details(owner))).with(csrf()).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"https://example.com/note\"}"))
                .andExpect(status().isCreated());
    }

    private ResultActions create(String url) throws Exception {
        return mockMvc.perform(post(weekPath()).with(user(details(owner))).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"url\":\"" + url + "\"}"));
    }

    private ResultActions check(Long id, boolean checked) throws Exception {
        return mockMvc.perform(patch(itemPath(id) + "/check").with(user(details(manager))).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content("{\"checked\":" + checked + "}"));
    }

    private Submission saveSubmission() {
        return submissionRepository.saveAndFlush(Submission.create(week, ownerMember, "https://example.com/note"));
    }

    private String weekPath() { return "/api/v1/weeks/" + week.getId() + "/submissions"; }
    private String itemPath(long id) { return "/api/v1/submissions/" + id; }
    private CustomUserDetails details(User user) { return new CustomUserDetails(user); }

    private User createUser(String email) { return persist(User.create(email, "{bcrypt}test-only-hash", "테스터")); }

    private Week createWeek(int number, AssignmentType type) {
        return persist(Week.create(study, number, "주차 " + number, "과제", null, null,
                LocalDateTime.of(2026, 11, 20, 23, 59), type));
    }

    private <T> T persist(T entity) { entityManager.persist(entity); return entity; }
}
