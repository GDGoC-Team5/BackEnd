package com.gdgoc.studyhub.domain.user.repository;

import com.gdgoc.studyhub.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    /** 로그인 시 UserDetailsService 에서 사용 */
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
