package com.gdgoc.studyhub.domain.bestmember.entity;

import com.gdgoc.studyhub.domain.studymember.entity.StudyMember;
import com.gdgoc.studyhub.domain.week.entity.Week;
import com.gdgoc.studyhub.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
 * 주차별 우수 멤버. 한 주차에 한 명만 선정할 수 있다 (week_id UNIQUE).
 * 선정 시각은 createdAt, 마지막으로 바꾼 시각은 updatedAt 이다.
 */
@Getter
@Entity
@Table(name = "weekly_best_member",
        uniqueConstraints = @UniqueConstraint(name = "uk_weekly_best_member_week", columnNames = "week_id"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WeeklyBestMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "week_id")
    private Week week;

    /** 우수 멤버로 뽑힌 사람 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private StudyMember member;

    /** 뽑은 사람 (MANAGER) */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "selected_by")
    private StudyMember selectedBy;

    @Column(length = 500)
    private String reason;

    private WeeklyBestMember(Week week, StudyMember member, StudyMember selectedBy, String reason) {
        this.week = week;
        this.member = member;
        this.selectedBy = selectedBy;
        this.reason = reason;
    }

    public static WeeklyBestMember create(Week week, StudyMember member, StudyMember selectedBy, String reason) {
        return new WeeklyBestMember(week, member, selectedBy, reason);
    }

    /** null 인 값은 바꾸지 않는다 (PATCH). */
    public void change(StudyMember member, String reason) {
        if (member != null) {
            this.member = member;
        }
        if (reason != null) {
            this.reason = reason;
        }
    }
}