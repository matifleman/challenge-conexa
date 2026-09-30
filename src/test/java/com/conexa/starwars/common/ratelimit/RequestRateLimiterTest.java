package com.conexa.starwars.common.ratelimit;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import com.conexa.starwars.common.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class RequestRateLimiterTest {

    private static final Duration WINDOW = Duration.ofMinutes(1);

    // Simulated clock in nanoseconds, advanced by the tests instead of waiting
    private final AtomicLong now = new AtomicLong();
    private final RequestRateLimiter limiter = new RequestRateLimiter(new RateLimitProperties(3, WINDOW), now::get);

    @Test
    void allowsRequestsUpToTheLimit() {
        assertThatCode(() -> requestTimes("luke", 3)).doesNotThrowAnyException();
    }

    @Test
    void rejectsRequestsOverTheLimitWithTheTimeLeftInTheWindow() {
        requestTimes("luke", 3);
        advance(Duration.ofSeconds(20));

        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> limiter.checkAllowed("luke"))
                .satisfies(ex -> assertThat(ex.getRetryAfter()).isEqualTo(Duration.ofSeconds(40)));
    }

    @Test
    void laterRequestsDoNotExtendTheWindow() {
        requestTimes("luke", 3);
        advance(Duration.ofSeconds(59));
        assertThatExceptionOfType(TooManyRequestsException.class).isThrownBy(() -> limiter.checkAllowed("luke"));

        advance(Duration.ofSeconds(1));

        assertThatCode(() -> limiter.checkAllowed("luke")).doesNotThrowAnyException();
    }

    @Test
    void countsEachUserSeparately() {
        requestTimes("luke", 3);

        assertThatCode(() -> limiter.checkAllowed("leia")).doesNotThrowAnyException();
    }

    @Test
    void countsUsernamesIgnoringCase() {
        requestTimes("LUKE", 3);

        assertThatExceptionOfType(TooManyRequestsException.class).isThrownBy(() -> limiter.checkAllowed("luke"));
    }

    private void requestTimes(String username, int times) {
        for (int i = 0; i < times; i++) {
            limiter.checkAllowed(username);
        }
    }

    private void advance(Duration duration) {
        now.addAndGet(duration.toNanos());
    }
}
