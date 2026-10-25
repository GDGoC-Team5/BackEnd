package com.gdgoc.studyhub.domain.auth.service;

import com.gdgoc.studyhub.domain.auth.dto.SignupRequest;
import com.gdgoc.studyhub.domain.user.dto.UserResponse;
import com.gdgoc.studyhub.domain.user.entity.User;
import com.gdgoc.studyhub.domain.user.repository.UserRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import com.gdgoc.studyhub.global.exception.ErrorCode;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        // 대소문자만 다른 이메일로 중복 가입하지 못하도록 소문자로 통일해서 저장한다.
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.create(email, passwordEncoder.encode(request.password()), request.name().trim());
        try {
            // 동시 요청이 exists 체크를 함께 통과한 경우 DB UNIQUE 제약에서 걸린다.
            // flush 로 INSERT 를 즉시 실행해야 이 자리에서 예외를 잡아 409 로 바꿀 수 있다.
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(ErrorCode.DUPLICATE_EMAIL);
        }
        return UserResponse.from(user);
    }
}
