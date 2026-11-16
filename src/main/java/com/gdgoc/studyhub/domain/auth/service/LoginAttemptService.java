package com.gdgoc.studyhub.domain.auth.service;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 로그인 무차별 대입(비밀번호 반복 시도) 방어.
 * 같은 이메일로 window 안에 maxFailures 번 실패하면 lockDuration 동안 로그인을 막는다.
 * <p>
 * IP 가 아니라 이메일 기준인 이유: 프론트가 Vercel 프록시를 거치므로 서버에서 보이는 IP 는 Vercel 서버 IP 다.
 * 가입되지 않은 이메일도 똑같이 세므로, 잠김 여부로 가입 여부를 알아낼 수 없다.
 * <p>
 * 서버 메모리에 저장하므로 재시작하면 초기화되고, 서버를 여러 대로 늘리면 Redis 등 공용 저장소로 옮겨야 한다.
 */
@Service
public class LoginAttemptService {

    /** 메모리 무한 증가 방지: 기록이 이 수를 넘으면 만료된 기록을 정리한다. */
    private static final int CLEANUP_THRESHOLD = 10_000;

    private final int maxFailures;
    private final Duration window;
    private final Duration lockDuration;
    private final Clock clock;
    private final Map<String, Attempt> attempts = new ConcurrentHashMap<>();

    @Autowired
    public LoginAttemptService(@Value("${auth.login-limit.max-failures:5}") int maxFailures,
                               @Value("${auth.login-limit.window:15m}") Duration window,
                               @Value("${auth.login-limit.lock-duration:15m}") Duration lockDuration) {
        this(maxFailures, window, lockDuration, Clock.systemUTC());
    }

    LoginAttemptService(int maxFailures, Duration window, Duration lockDuration, Clock clock) {
        this.maxFailures = maxFailures;
        this.window = window;
        this.lockDuration = lockDuration;
        this.clock = clock;
    }

    /** 지금 이 이메일로 로그인 시도를 막아야 하는지. */
    public boolean isBlocked(String email) {
        Attempt attempt = attempts.get(email);
        return attempt != null && attempt.lockedUntil != null && clock.instant().isBefore(attempt.lockedUntil);
    }

    /** 로그인 실패 1회 기록. maxFailures 에 도달하면 잠근다. */
    public void recordFailure(String email) {
        Instant now = clock.instant();
        attempts.compute(email, (key, prev) -> {
            Attempt attempt = (prev == null || prev.isExpired(now, window)) ? new Attempt(now) : prev;
            attempt.failures++;
            if (attempt.failures >= maxFailures) {
                attempt.lockedUntil = now.plus(lockDuration);
            }
            return attempt;
        });
        if (attempts.size() > CLEANUP_THRESHOLD) {
            attempts.entrySet().removeIf(e -> e.getValue().isExpired(now, window));
        }
    }

    /** 로그인 성공 시 실패 기록을 지운다. */
    public void reset(String email) {
        attempts.remove(email);
    }

    private static final class Attempt {
        private final Instant windowStart;
        private int failures;
        private Instant lockedUntil;

        private Attempt(Instant windowStart) {
            this.windowStart = windowStart;
        }

        /** 잠금도 풀렸고 집계 구간도 지났으면 새로 센다. */
        private boolean isExpired(Instant now, Duration window) {
            boolean lockOver = lockedUntil == null || !now.isBefore(lockedUntil);
            return lockOver && !now.isBefore(windowStart.plus(window));
        }
    }
}
