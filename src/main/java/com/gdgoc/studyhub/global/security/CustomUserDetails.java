package com.gdgoc.studyhub.global.security;

import com.gdgoc.studyhub.domain.user.entity.User;
import java.util.Collection;
import java.util.List;
import lombok.Getter;
import org.springframework.security.core.CredentialsContainer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

/**
 * 로그인한 사용자 정보. 세션의 SecurityContext 에 저장되고, 컨트롤러에서는
 * {@code @AuthenticationPrincipal CustomUserDetails principal} 로 꺼내 쓴다.
 * <p>
 * 세션에 직렬화되므로 엔티티(User) 대신 필요한 값만 복사해 둔다.
 * CredentialsContainer 를 구현해 두면 인증 성공 직후 AuthenticationManager 가
 * eraseCredentials() 를 호출해 비밀번호 해시를 메모리·세션에서 지운다.
 */
@Getter
public class CustomUserDetails implements UserDetails, CredentialsContainer {

    private final Long userId;
    private final String email;
    private String password;
    private final List<GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.userId = user.getId();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + user.getSystemRole().name()));
    }

    /** 시큐리티가 "아이디"로 쓰는 값. 이 서비스에서는 이메일이다. */
    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public void eraseCredentials() {
        this.password = null;
    }
}
