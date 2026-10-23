package com.gdgoc.studyhub.global.common;

import org.springframework.data.jpa.repository.JpaRepository;

interface AuditingTestRepository extends JpaRepository<AuditingTestEntity, Long> {
}
