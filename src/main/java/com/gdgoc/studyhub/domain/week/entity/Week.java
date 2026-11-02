package com.gdgoc.studyhub.domain.week.entity;

import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 스터디의 한 주차(회차). 같은 스터디 안에서 weekNo 는 중복될 수 없다.
 */
@Getter
@Entity
@Table(name = "week",
        uniqueConstraints = @UniqueConstraint(name = "uk_week_study_week_no", columnNames = {"study_id", "week_no"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Week extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;

    @Column(name = "week_no", nullable = false)
    private int weekNo;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(name = "assignment_description", length = 2000)
    private String assignmentDescription;

    /** 모임 일시 */
    @Column(name = "session_at")
    private LocalDateTime sessionAt;

    @Column(length = 100)
    private String location;

    /** 제출 마감 일시 */
    @Column(nullable = false)
    private LocalDateTime deadline;

    @Enumerated(EnumType.STRING)
    @Column(name = "assignment_type", nullable = false, length = 20)
    private AssignmentType assignmentType;

    private Week(Study study, int weekNo, String title, String assignmentDescription,
                 LocalDateTime sessionAt, String location, LocalDateTime deadline, AssignmentType assignmentType) {
        this.study = study;
        this.weekNo = weekNo;
        this.title = title;
        this.assignmentDescription = assignmentDescription;
        this.sessionAt = sessionAt;
        this.location = location;
        this.deadline = deadline;
        this.assignmentType = assignmentType;
    }

    public static Week create(Study study, int weekNo, String title, String assignmentDescription,
                              LocalDateTime sessionAt, String location, LocalDateTime deadline,
                              AssignmentType assignmentType) {
        return new Week(study, weekNo, title, assignmentDescription, sessionAt, location, deadline, assignmentType);
    }

    /** null 인 값은 바꾸지 않는다 (PATCH). 제출 방식(assignmentType)은 수정할 수 없다. */
    public void update(Integer weekNo, String title, String assignmentDescription,
                       LocalDateTime sessionAt, String location, LocalDateTime deadline) {
        if (weekNo != null) {
            this.weekNo = weekNo;
        }
        if (title != null) {
            this.title = title;
        }
        if (assignmentDescription != null) {
            this.assignmentDescription = assignmentDescription;
        }
        if (sessionAt != null) {
            this.sessionAt = sessionAt;
        }
        if (location != null) {
            this.location = location;
        }
        if (deadline != null) {
            this.deadline = deadline;
        }
    }
}