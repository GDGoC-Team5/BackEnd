package com.gdgoc.studyhub.domain.study.entity;

import com.gdgoc.studyhub.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "study")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Study extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(length = 500)
    private String description;

    /** 초대 코드. 이 값을 아는 사람은 누구나 MEMBER 로 가입할 수 있다. */
    @Column(name = "invite_code", nullable = false, unique = true, length = 8)
    private String inviteCode;

    private Study(String name, String description, String inviteCode) {
        this.name = name;
        this.description = description;
        this.inviteCode = inviteCode;
    }

    public static Study create(String name, String description, String inviteCode) {
        return new Study(name, description, inviteCode);
    }

    /** null 인 값은 바꾸지 않는다 (PATCH). */
    public void update(String name, String description) {
        if (name != null) {
            this.name = name;
        }
        if (description != null) {
            this.description = description;
        }
    }
}
