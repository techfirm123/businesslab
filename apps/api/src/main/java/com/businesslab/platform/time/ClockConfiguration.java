
package com.businesslab.platform.time;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

/**
 * Central system clock configuration.
 *
 * Application services depend on Clock rather than calling
 * Instant.now() directly, keeping time-dependent operations testable.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock businessLabClock() {
        return Clock.systemUTC();
    }
}
