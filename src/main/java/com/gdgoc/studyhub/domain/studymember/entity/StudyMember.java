package com.gdgoc.studyhub.domain.studymember.entity;

import com.gdgoc.studyhub.domain.study.entity.Study;
import com.gdgoc.studyhub.domain.user.entity.User;
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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원이 어떤 스터디에 어떤 역할로 속해 있는지. (study, user) 쌍은 하나만 존재하고,
 * 탈퇴 후 재가입하면 새 행을 만들지 않고 이 행의 status 를 다시 ACTIVE 로 바꾼다.
 */
@Getter
@Entity
@Table(name = "study_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_study_member_study_user", columnNames = {"study_id", "user_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudyRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status;

    private StudyMember(Study study, User user, StudyRole role) {
        this.study = study;
        this.user = user;
        this.role = role;
        this.status = MemberStatus.ACTIVE;
    }

    public static StudyMember manager(Study study, User user) {
        return new StudyMember(study, user, StudyRole.MANAGER);
    }

    public static StudyMember member(Study study, User user) {
        return new StudyMember(study, user, StudyRole.MEMBER);
    }

    public boolean isActive() {
        return status == MemberStatus.ACTIVE;
    }

    public boolean isManager() {
        return role == StudyRole.MANAGER;
    }

    public void leave() {
        this.status = MemberStatus.LEFT;
    }

    /** 탈퇴했던 멤버의 재가입. 예전 역할과 상관없이 MEMBER 로 돌아온다. */
    public void rejoin() {
        this.status = MemberStatus.ACTIVE;
        this.role = StudyRole.MEMBER;
    }
}
