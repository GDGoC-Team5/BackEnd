package com.gdgoc.studyhub.global.common;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

/** BaseTimeEntity 검증용 테스트 엔티티 */
@Entity
class AuditingTestEntity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
}
