package com.gdgoc.studyhub.domain.auth.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;

class LoginAttemptServiceTest {

    private static final String EMAIL = "tester@studyhub.com";

    private final MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
    private final LoginAttemptService service =
            new LoginAttemptService(5, Duration.ofMinutes(15), Duration.ofMinutes(15), clock);

    @Test
    void 실패가_한도_미만이면_막지_않는다() {
        failTimes(4);
        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void 실패가_한도에_도달하면_막는다() {
        failTimes(5);
        assertThat(service.isBlocked(EMAIL)).isTrue();
    }

    @Test
    void 잠금_시간이_지나면_다시_허용한다() {
        failTimes(5);
        clock.advance(Duration.ofMinutes(15));
        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void 집계_구간이_지나면_실패_횟수를_새로_센다() {
        failTimes(4);
        clock.advance(Duration.ofMinutes(16));
        failTimes(1);
        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void 성공하면_실패_기록을_지운다() {
        failTimes(4);
        service.reset(EMAIL);
        failTimes(1);
        assertThat(service.isBlocked(EMAIL)).isFalse();
    }

    @Test
    void 다른_이메일에는_영향을_주지_않는다() {
        failTimes(5);
        assertThat(service.isBlocked("other@studyhub.com")).isFalse();
    }

    private void failTimes(int n) {
        for (int i = 0; i < n; i++) {
            service.recordFailure(EMAIL);
        }
    }

    private static final class MutableClock extends Clock {
        private Instant now;

        private MutableClock(Instant now) {
            this.now = now;
        }

        void advance(Duration d) {
            now = now.plus(d);
        }

        @Override
        public ZoneOffset getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(java.time.ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return now;
        }
    }
}
