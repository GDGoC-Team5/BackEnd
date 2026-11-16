package com.gdgoc.studyhub.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JPA Auditing 활성화.
 * 메인 클래스가 아니라 별도 설정 클래스에 두어, JPA 가 없는 슬라이스 테스트(@WebMvcTest 등)가 깨지지 않게 한다.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
