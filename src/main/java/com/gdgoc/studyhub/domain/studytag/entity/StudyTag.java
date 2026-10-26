package com.gdgoc.studyhub.domain.studytag.entity;

import com.gdgoc.studyhub.domain.study.entity.Study;
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
 * 스터디 속성(태그). 운영진 대시보드에서 스터디를 분류할 때 쓴다. 한 스터디 안에서 이름은 중복될 수 없다.
 */
@Getter
@Entity
@Table(name = "study_tag",
        uniqueConstraints = @UniqueConstraint(name = "uk_study_tag_study_name", columnNames = {"study_id", "name"}))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyTag extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "study_id")
    private Study study;

    @Column(nullable = false, length = 30)
    private String name;

    private StudyTag(Study study, String name) {
        this.study = study;
        this.name = name;
    }

    public static StudyTag create(Study study, String name) {
        return new StudyTag(study, name);
    }
}
