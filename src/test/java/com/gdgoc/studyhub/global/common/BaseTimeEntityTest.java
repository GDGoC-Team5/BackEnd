package com.gdgoc.studyhub.global.common;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class BaseTimeEntityTest {

    @Autowired
    private AuditingTestRepository repository;

    @Test
    void 저장하면_created_at과_updated_at이_자동으로_채워진다() {
        AuditingTestEntity saved = repository.saveAndFlush(new AuditingTestEntity());

        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
    }
}
