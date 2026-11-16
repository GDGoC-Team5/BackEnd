package com.gdgoc.studyhub.domain.submission.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.user.entity.User;
import com.gdgoc.studyhub.domain.week.entity.AssignmentType;
import com.gdgoc.studyhub.domain.week.entity.Week;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceException;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class SubmissionPersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void 제출물을_저장하면_관계와_최초_상태와_제출_시각이_유지된다() {
        Study study = persist(Study.create("제출 테스트", "", "SUBTEST1"));
        StudyMember member = createMember(study, "submission-one@example.com");
        Week week = createWeek(study, 1);
        Submission submission = persist(Submission.create(week, member, "https://example.com/note"));
        entityManager.flush();
        Long submissionId = submission.getId();
        entityManager.clear();

        Submission found = entityManager.find(Submission.class, submissionId);
        assertThat(found.getWeek().getId()).isEqualTo(week.getId());
        assertThat(found.getMember().getId()).isEqualTo(member.getId());
        assertThat(found.getUrl()).isEqualTo("https://example.com/note");
        assertThat(found.isChecked()).isFalse();
        assertThat(found.getCreatedAt()).isNotNull();
        assertThat(found.getUpdatedAt()).isNotNull();
    }

    @Test
    void 같은_멤버가_같은_주차에_중복_제출하면_DB가_거부한다() {
        Study study = persist(Study.create("중복 테스트", "", "SUBTEST2"));
        StudyMember member = createMember(study, "submission-two@example.com");
        Week week = createWeek(study, 1);
        persist(Submission.create(week, member, "https://example.com/first"));
        entityManager.flush();

        assertThatThrownBy(() -> {
            persist(Submission.create(week, member, "https://example.com/second"));
            entityManager.flush();
        }).isInstanceOf(PersistenceException.class);
    }

    @Test
    void 주차나_멤버가_다르면_각각_제출할_수_있다() {
        Study study = persist(Study.create("독립 제출 테스트", "", "SUBTEST3"));
        StudyMember firstMember = createMember(study, "submission-three@example.com");
        StudyMember secondMember = createMember(study, "submission-four@example.com");
        Week firstWeek = createWeek(study, 1);
        Week secondWeek = createWeek(study, 2);

        Submission first = persist(Submission.create(firstWeek, firstMember, "https://example.com/a"));
        Submission nextWeek = persist(Submission.create(secondWeek, firstMember, "https://example.com/b"));
        Submission otherMember = persist(Submission.create(firstWeek, secondMember, "https://example.com/c"));
        entityManager.flush();

        assertThat(first.getId()).isNotEqualTo(nextWeek.getId()).isNotEqualTo(otherMember.getId());
    }

    private StudyMember createMember(Study study, String email) {
        User user = persist(User.create(email, "{bcrypt}test-only-hash", "테스터"));
        return persist(StudyMember.member(study, user));
    }

    private Week createWeek(Study study, int weekNo) {
        return persist(Week.create(study, weekNo, "주차 " + weekNo, "과제", null, null,
                LocalDateTime.of(2026, 11, 20, 23, 59), AssignmentType.LINK));
    }

    private <T> T persist(T entity) {
        entityManager.persist(entity);
        return entity;
    }
}
