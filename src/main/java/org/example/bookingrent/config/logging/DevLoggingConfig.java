package org.example.bookingrent.config.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("dev")
public class DevLoggingConfig {

    public DevLoggingConfig() {
        Logger rootLogger = (Logger) LoggerFactory.getLogger("org.example.bookingrent");

        rootLogger.setLevel(Level.TRACE);

        rootLogger.info("Development logging enabled: TRACE level for all packages");
    }
}
