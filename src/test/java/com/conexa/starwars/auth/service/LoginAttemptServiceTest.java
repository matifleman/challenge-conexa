package com.conexa.starwars.auth.service;

import java.time.Duration;
import java.util.concurrent.atomic.AtomicLong;

import com.conexa.starwars.auth.config.LoginAttemptProperties;
import com.conexa.starwars.common.exception.TooManyRequestsException;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LoginAttemptServiceTest {

    private static final Duration BLOCK_DURATION = Duration.ofMinutes(15);

    // Simulated clock in nanoseconds, advanced by the tests instead of waiting
    private final AtomicLong now = new AtomicLong();
    private final LoginAttemptService service =
            new LoginAttemptService(new LoginAttemptProperties(5, BLOCK_DURATION), now::get);

    @Test
    void allowsLoginBelowTheLimit() {
        failTimes("luke", 4);

        assertThatCode(() -> service.checkNotBlocked("luke")).doesNotThrowAnyException();
    }

    @Test
    void blocksUsernameOnceTheLimitIsReached() {
        failTimes("luke", 5);

        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> service.checkNotBlocked("luke"))
                .satisfies(ex -> assertThat(ex.getRetryAfter()).isEqualTo(BLOCK_DURATION));
    }

    @Test
    void successfulLoginResetsTheCount() {
        failTimes("luke", 4);
        service.loginSucceeded("luke");
        failTimes("luke", 4);

        assertThatCode(() -> service.checkNotBlocked("luke")).doesNotThrowAnyException();
    }

    @Test
    void countsUsernamesIgnoringCase() {
        failTimes("LUKE", 5);

        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> service.checkNotBlocked("luke"));
    }

    @Test
    void blockDoesNotAffectOtherUsernames() {
        failTimes("luke", 5);

        assertThatCode(() -> service.checkNotBlocked("leia")).doesNotThrowAnyException();
    }

    @Test
    void blockLiftsOnceTheDurationHasPassed() {
        failTimes("luke", 5);

        now.addAndGet(BLOCK_DURATION.minusSeconds(1).toNanos());
        assertThatExceptionOfType(TooManyRequestsException.class)
                .isThrownBy(() -> service.checkNotBlocked("luke"));

        now.addAndGet(Duration.ofSeconds(1).toNanos());
        assertThatCode(() -> service.checkNotBlocked("luke")).doesNotThrowAnyException();
    }

    private void failTimes(String username, int times) {
        for (int i = 0; i < times; i++) {
            service.loginFailed(username);
        }
    }
}
