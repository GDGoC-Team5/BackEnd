package com.gdgoc.studyhub.domain.user.entity;

import com.gdgoc.studyhub.global.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 회원. password 에는 원문이 아니라 PasswordEncoder 로 해시한 값({bcrypt}...)만 저장한다.
 * user 는 MySQL·H2 예약어라 테이블명은 users 로 둔다.
 */
@Getter
@Entity
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 20)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "system_role", nullable = false, length = 20)
    private SystemRole systemRole;

    private User(String email, String encodedPassword, String name, SystemRole systemRole) {
        this.email = email;
        this.password = encodedPassword;
        this.name = name;
        this.systemRole = systemRole;
    }

    /** 일반 회원 생성. 비밀번호는 반드시 해시된 값을 넘긴다. */
    public static User create(String email, String encodedPassword, String name) {
        return new User(email, encodedPassword, name, SystemRole.USER);
    }
}
