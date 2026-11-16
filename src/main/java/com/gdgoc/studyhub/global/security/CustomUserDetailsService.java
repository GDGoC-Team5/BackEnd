package com.gdgoc.studyhub.global.security;

import com.gdgoc.studyhub.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 로그인 시 DaoAuthenticationProvider 가 호출한다. "이메일로 회원을 찾아 UserDetails 로 변환"하는 역할만 하고,
 * 비밀번호 비교는 Provider 가 PasswordEncoder 로 직접 한다.
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 이 예외는 Provider 가 BadCredentialsException 으로 바꿔서, 이메일이 없는지 비밀번호가 틀렸는지 구분할 수 없게 된다.
        return userRepository.findByEmail(email)
                .map(CustomUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("user not found"));
    }
}
