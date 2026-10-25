package com.gdgoc.studyhub.global.config;

import com.gdgoc.studyhub.global.security.CsrfCookieFilter;
import com.gdgoc.studyhub.global.security.RestAccessDeniedHandler;
import com.gdgoc.studyhub.global.security.RestAuthenticationEntryPoint;
import com.gdgoc.studyhub.global.security.RestLogoutSuccessHandler;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * 세션 기반 인증의 기본 보안 설정. 로그인(JSON) 처리는 AuthController 에서 직접 수행하므로
 * formLogin / httpBasic 은 사용하지 않는다.
 */
@Configuration
// @PreAuthorize 사용 가능하게 한다. 스터디별 권한 체크 예: @PreAuthorize("@studyAuth.isManager(#studyId, authentication)")
@EnableMethodSecurity
public class SecurityConfig {

    private static final String[] PUBLIC_ENDPOINTS = {
            "/api/auth/signup",
            "/api/auth/login",
            "/swagger-ui.html",
            "/swagger-ui/**",
            "/v3/api-docs/**"
    };

    private static final List<String> ALLOWED_ORIGINS = List.of(
            "https://studyhub.com",
            "https://dev.studyhub.com",
            "http://localhost:3000"
    );

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   RestAuthenticationEntryPoint authenticationEntryPoint,
                                                   RestAccessDeniedHandler accessDeniedHandler,
                                                   RestLogoutSuccessHandler logoutSuccessHandler) throws Exception {
        http
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // SPA 가 쿠키(XSRF-TOKEN)를 읽어 X-XSRF-TOKEN 헤더로 보내는 방식.
                // 인증 전 API(회원가입·로그인)는 토큰을 받을 수 있는 선행 요청이 없으므로 제외한다.
                .csrf(csrf -> csrf
                        .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                        .csrfTokenRequestHandler(new CsrfTokenRequestAttributeHandler())
                        .ignoringRequestMatchers(
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/auth/signup"),
                                PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/auth/login")))
                .addFilterAfter(new CsrfCookieFilter(), BasicAuthenticationFilter.class)
                // 로그인 컨트롤러가 저장한 SecurityContext 를 다음 요청부터 세션에서 꺼내 오도록 같은 저장소를 쓴다.
                .securityContext(context -> context.securityContextRepository(securityContextRepository()))
                // LogoutFilter 가 처리: 세션 무효화 + SecurityContext 비우기 + CSRF 토큰 삭제 + JSESSIONID 쿠키 삭제.
                // POST 이고 CSRF 검사 대상이라, 다른 사이트가 몰래 로그아웃시키는 요청은 막힌다.
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .deleteCookies("JSESSIONID")
                        .logoutSuccessHandler(logoutSuccessHandler))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler));
        return http.build();
    }

    /**
     * 로그인 API 에서 이메일·비밀번호를 검증할 때 쓰는 AuthenticationManager.
     * DaoAuthenticationProvider 가 UserDetailsService 로 회원을 찾고 PasswordEncoder 로 비밀번호를 비교한다.
     */
    @Bean
    public AuthenticationManager authenticationManager(UserDetailsService userDetailsService,
                                                       PasswordEncoder passwordEncoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder);
        return new ProviderManager(provider);
    }

    /** SecurityContext 를 요청 속성 + HTTP 세션에 저장한다 (시큐리티 기본값과 동일한 구성). */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository());
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(ALLOWED_ORIGINS);
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Content-Type", "X-XSRF-TOKEN"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
