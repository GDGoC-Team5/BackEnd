package com.gdgoc.studyhub.domain.auth.service;

import com.gdgoc.studyhub.domain.auth.dto.LoginRequest;
import com.gdgoc.studyhub.domain.auth.dto.SignupRequest;
import com.gdgoc.studyhub.domain.auth.exception.AuthErrorCode;
import com.gdgoc.studyhub.domain.user.dto.UserResponse;
import com.gdgoc.studyhub.domain.user.entity.User;
import com.gdgoc.studyhub.domain.user.exception.UserErrorCode;
import com.gdgoc.studyhub.domain.user.repository.UserRepository;
import com.gdgoc.studyhub.global.exception.BusinessException;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    @Transactional
    public UserResponse signup(SignupRequest request) {
        String email = normalizeEmail(request.email());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }

        User user = User.create(email, passwordEncoder.encode(request.password()), request.name().trim());
        try {
            // 동시 요청이 exists 체크를 함께 통과한 경우 DB UNIQUE 제약에서 걸린다.
            // flush 로 INSERT 를 즉시 실행해야 이 자리에서 예외를 잡아 409 로 바꿀 수 있다.
            userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException e) {
            throw new BusinessException(UserErrorCode.DUPLICATE_EMAIL);
        }
        return UserResponse.from(user);
    }

    /**
     * 이메일·비밀번호를 검증하고 인증 결과(Authentication)를 돌려준다. 세션 저장은 컨트롤러에서 한다.
     * 이메일이 없든 비밀번호가 틀리든 같은 LOGIN_FAILED 로 응답해 가입 여부를 노출하지 않는다.
     */
    public Authentication authenticate(LoginRequest request) {
        UsernamePasswordAuthenticationToken token = UsernamePasswordAuthenticationToken.unauthenticated(
                normalizeEmail(request.email()), request.password());
        try {
            return authenticationManager.authenticate(token);
        } catch (AuthenticationException e) {
            throw new BusinessException(AuthErrorCode.LOGIN_FAILED);
        }
    }

    /** 대소문자만 다른 이메일을 같은 회원으로 보기 위해 가입·로그인 모두 소문자로 통일한다. */
    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
