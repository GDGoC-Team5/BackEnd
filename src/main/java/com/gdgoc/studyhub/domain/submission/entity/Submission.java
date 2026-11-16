package com.gdgoc.studyhub.domain.submission.entity;

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
 * 링크로 제출한 과제 한 건. 같은 멤버는 한 주차에 한 건만 제출한다.
 * 최초 제출 시각은 createdAt, 마지막 수정 시각은 updatedAt 이다.
 */
@Getter
@Entity
@Table(name = "submission",
        uniqueConstraints = @UniqueConstraint(name = "uk_submission_week_member", columnNames = {"week_id", "member_id"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Submission extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "week_id")
    private Week week;

    /** 제출자는 특정 스터디의 멤버다. 회원 정보는 member.getUser() 로 연결된다. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id")
    private StudyMember member;

    @Column(nullable = false, length = 2048)
    private String url;

    /** 운영진이 제출을 확인했는지 여부. 최초 제출 시에는 미확인이다. */
    @Column(nullable = false)
    private boolean checked;

    private Submission(Week week, StudyMember member, String url) {
        this.week = week;
        this.member = member;
        this.url = url;
        this.checked = false;
    }

    public static Submission create(Week week, StudyMember member, String url) {
        return new Submission(week, member, url);
    }

    /** 제출 내용이 바뀌면 운영진이 다시 확인해야 한다. */
    public void updateUrl(String url) {
        if (!this.url.equals(url)) {
            this.url = url;
            this.checked = false;
        }
    }

    public void changeChecked(boolean checked) {
        this.checked = checked;
    }
}
