package com.gdgoc.studyhub.domain.material.entity;

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
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/** 주차에 공유한 세션 자료 링크. 파일 자체는 저장하지 않는다. */
@Getter
@Entity
@Table(name = "material")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Material extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "week_id", nullable = false)
    private Week week;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private StudyMember member;

    @Column(nullable = false, length = 100)
    private String title;

    @Column(nullable = false, length = 2048)
    private String url;

    private Material(Week week, StudyMember member, String title, String url) {
        this.week = week;
        this.member = member;
        this.title = title;
        this.url = url;
    }

    public static Material create(Week week, StudyMember member, String title, String url) {
        return new Material(week, member, title, url);
    }
}
