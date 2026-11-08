package com.gdgoc.studyhub.domain.material.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.gdgoc.studyhub.domain.material.entity.Material;
import com.gdgoc.studyhub.domain.material.repository.MaterialRepository;
import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
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
class MaterialControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private EntityManager entityManager;
    @Autowired private MaterialRepository materialRepository;

    private Week week;
    private User manager;
    private User secondManager;
    private User member;
    private User outsider;
    private StudyMember managerMember;
    private StudyMember memberMembership;

    @BeforeEach
    void setUp() {
        Study study = persist(Study.create("세션 자료 테스트", "", "MATERIAL"));
        manager = createUser("material-manager@example.com");
        secondManager = createUser("material-manager2@example.com");
        member = createUser("material-member@example.com");
        outsider = createUser("material-outside@example.com");
        managerMember = persist(StudyMember.manager(study, manager));
        persist(StudyMember.manager(study, secondManager));
        memberMembership = persist(StudyMember.member(study, member));
        // 자료는 과제 제출 방식과 관계없이 공유할 수 있다.
        week = persist(Week.create(study, 1, "첫 주차", "과제", null, null,
                LocalDateTime.of(2026, 11, 20, 23, 59), AssignmentType.FILE));
        entityManager.flush();
    }

    @Test
    void 등록부터_목록_상세_삭제까지_4개_API가_연결된다() throws Exception {
        String body = create("{\"title\":\"  첫 세션 자료  \",\"url\":\" https://example.com/slides \"}")
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.weekId").value(week.getId()))
                .andExpect(jsonPath("$.data.memberId").value(managerMember.getId()))
                .andExpect(jsonPath("$.data.userId").value(manager.getId()))
                .andExpect(jsonPath("$.data.title").value("첫 세션 자료"))
                .andExpect(jsonPath("$.data.url").value("https://example.com/slides"))
                .andExpect(jsonPath("$.data.createdAt").isNotEmpty())
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.data.id")).longValue();
        entityManager.clear();
        assertThat(materialRepository.findById(id).orElseThrow().getTitle()).isEqualTo("첫 세션 자료");

        mockMvc.perform(get(weekPath()).with(user(details(member))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(1));
        mockMvc.perform(get(itemPath(id)).with(user(details(member))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.url").value("https://example.com/slides"));
        mockMvc.perform(get(itemPath(id)).with(user(details(manager))))
                .andExpect(status().isOk());
        mockMvc.perform(delete(itemPath(id)).with(user(details(secondManager))).with(csrf()))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").doesNotExist());
        entityManager.flush();
        entityManager.clear();
        assertThat(materialRepository.existsById(id)).isFalse();
        mockMvc.perform(get(itemPath(id)).with(user(details(member))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("MATERIAL_404"));
    }

    @Test
    void 멤버는_조회만_가능하고_등록과_삭제는_운영진만_가능하다() throws Exception {
        Material material = saveMaterial(week, "자료");
        mockMvc.perform(post(weekPath()).with(user(details(member))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("STUDY_403_MANAGER"));
        mockMvc.perform(delete(itemPath(material.getId())).with(user(details(member))).with(csrf()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("STUDY_403_MANAGER"));
        assertThat(materialRepository.existsById(material.getId())).isTrue();
    }

    @Test
    void 다른_스터디_운영진은_이_스터디_자료에_접근할_수_없다() throws Exception {
        Study otherStudy = persist(Study.create("다른 스터디", "", "OTHER123"));
        persist(StudyMember.manager(otherStudy, outsider));
        Material material = saveMaterial(week, "비공개 자료");
        mockMvc.perform(post(weekPath()).with(user(details(outsider))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isForbidden()).andExpect(jsonPath("$.error.code").value("STUDY_403_MEMBER"));
        mockMvc.perform(get(weekPath()).with(user(details(outsider))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(itemPath(material.getId())).with(user(details(outsider))))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(itemPath(material.getId())).with(user(details(outsider))).with(csrf()))
                .andExpect(status().isForbidden());
    }

    @Test
    void 탈퇴한_멤버와_운영진은_기존_자료에도_접근할_수_없다() throws Exception {
        Material material = saveMaterial(week, "자료");
        memberMembership.leave();
        managerMember.leave();
        entityManager.flush();
        mockMvc.perform(get(itemPath(material.getId())).with(user(details(member))))
                .andExpect(status().isForbidden());
        mockMvc.perform(get(weekPath()).with(user(details(member))))
                .andExpect(status().isForbidden());
        create(validBody()).andExpect(status().isForbidden());
        mockMvc.perform(delete(itemPath(material.getId())).with(user(details(manager))).with(csrf()))
                .andExpect(status().isForbidden());
        // 기존 등록자가 탈퇴해도 다른 활동 멤버는 자료를 조회할 수 있다.
        mockMvc.perform(get(itemPath(material.getId())).with(user(details(secondManager))))
                .andExpect(status().isOk());
    }

    @Test
    void 로그인과_CSRF_보호가_4개_API에_적용된다() throws Exception {
        Material material = saveMaterial(week, "자료");
        mockMvc.perform(get(weekPath())).andExpect(status().isUnauthorized());
        mockMvc.perform(get(itemPath(material.getId()))).andExpect(status().isUnauthorized());
        mockMvc.perform(post(weekPath()).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(delete(itemPath(material.getId())).with(csrf())).andExpect(status().isUnauthorized());
        mockMvc.perform(post(weekPath()).with(user(details(manager)))
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isForbidden());
        mockMvc.perform(delete(itemPath(material.getId())).with(user(details(manager))))
                .andExpect(status().isForbidden());
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "not-a-url", "ftp://example.com/file", "https://", "https://example.com/a b"})
    void 잘못된_링크는_400이다(String url) throws Exception {
        create("{\"title\":\"자료\",\"url\":\"" + url + "\"}").andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("COMMON_400"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"title\":\"자료\"}", "{\"url\":\"https://example.com\"}",
            "{\"title\":\"   \",\"url\":\"https://example.com\"}",
            "{\"title\":null,\"url\":null}"})
    void 필수_입력_누락이나_공백은_400이다(String body) throws Exception {
        create(body).andExpect(status().isBadRequest());
    }

    @Test
    void 제목과_링크_최대길이를_검증한다() throws Exception {
        create("{\"title\":\"" + "가".repeat(101) + "\",\"url\":\"https://example.com\"}")
                .andExpect(status().isBadRequest());
        create("{\"title\":\"자료\",\"url\":\"https://example.com/" + "a".repeat(2048) + "\"}")
                .andExpect(status().isBadRequest());
        String boundaryUrl = "https://example.com/" + "a".repeat(2048 - "https://example.com/".length());
        create("{\"title\":\"" + "가".repeat(100) + "\",\"url\":\"" + boundaryUrl + "\"}")
                .andExpect(status().isCreated());
    }

    @Test
    void 빈_목록과_없는_주차와_없는_자료를_구분한다() throws Exception {
        mockMvc.perform(get(weekPath()).with(user(details(member))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data").isEmpty());
        String missingWeek = "/api/v1/weeks/" + Long.MAX_VALUE + "/materials";
        mockMvc.perform(get(missingWeek).with(user(details(member))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("WEEK_404"));
        mockMvc.perform(post(missingWeek).with(user(details(manager))).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("WEEK_404"));
        mockMvc.perform(get(itemPath(Long.MAX_VALUE)).with(user(details(member))))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("MATERIAL_404"));
        mockMvc.perform(delete(itemPath(Long.MAX_VALUE)).with(user(details(manager))).with(csrf()))
                .andExpect(status().isNotFound()).andExpect(jsonPath("$.error.code").value("MATERIAL_404"));
    }

    @Test
    void 목록은_해당_주차_자료만_최신순으로_반환한다() throws Exception {
        Material first = saveMaterial(week, "첫 자료");
        Material second = saveMaterial(week, "다음 자료");
        Week otherWeek = persist(Week.create(week.getStudy(), 2, "다른 주차", "과제", null, null,
                week.getDeadline(), AssignmentType.LINK));
        saveMaterial(otherWeek, "다른 주차 자료");
        LocalDateTime sameTime = LocalDateTime.of(2026, 11, 1, 12, 0);
        entityManager.createQuery("update Material m set m.createdAt = :time where m.week.id = :weekId")
                .setParameter("time", sameTime).setParameter("weekId", week.getId()).executeUpdate();
        entityManager.clear();
        mockMvc.perform(get(weekPath()).with(user(details(member))))
                .andExpect(status().isOk()).andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].id").value(second.getId()))
                .andExpect(jsonPath("$.data[1].id").value(first.getId()));
    }

    @Test
    void 자료가_있는_주차는_409이고_자료를_삭제하면_주차도_삭제된다() throws Exception {
        Material material = saveMaterial(week, "자료");
        mockMvc.perform(delete("/api/weeks/" + week.getId()).with(user(details(manager))).with(csrf()))
                .andExpect(status().isConflict()).andExpect(jsonPath("$.error.code").value("WEEK_409_MATERIALS"));
        mockMvc.perform(delete(itemPath(material.getId())).with(user(details(manager))).with(csrf()))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/weeks/" + week.getId()).with(user(details(manager))).with(csrf()))
                .andExpect(status().isOk());
    }

    @Test
    void 기존_api_접두사도_4개_기능을_지원한다() throws Exception {
        String body = mockMvc.perform(post("/api/weeks/" + week.getId() + "/materials")
                        .with(user(details(manager))).with(csrf()).contentType(MediaType.APPLICATION_JSON).content(validBody()))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.data.id")).longValue();
        mockMvc.perform(get("/api/weeks/" + week.getId() + "/materials").with(user(details(member))))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/materials/" + id).with(user(details(member))))
                .andExpect(status().isOk());
        mockMvc.perform(delete("/api/materials/" + id).with(user(details(manager))).with(csrf()))
                .andExpect(status().isOk());
    }

    private ResultActions create(String body) throws Exception {
        return mockMvc.perform(post(weekPath()).with(user(details(manager))).with(csrf())
                .contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private String validBody() { return "{\"title\":\"세션 자료\",\"url\":\"https://example.com/slides\"}"; }
    private String weekPath() { return "/api/v1/weeks/" + week.getId() + "/materials"; }
    private String itemPath(long id) { return "/api/v1/materials/" + id; }
    private CustomUserDetails details(User actor) { return new CustomUserDetails(actor); }
    private User createUser(String email) { return persist(User.create(email, "{bcrypt}test-only-hash", "테스터")); }
    private Material saveMaterial(Week targetWeek, String title) {
        return materialRepository.saveAndFlush(Material.create(targetWeek, managerMember, title, "https://example.com/slides"));
    }
    private <T> T persist(T entity) { entityManager.persist(entity); return entity; }
}
