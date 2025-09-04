package org.example.bookingrent;

import io.dapr.client.DaprClient;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.test.context.ActiveProfiles;

@TestConfiguration
@ActiveProfiles("test")
class MockConfig {
    @Bean
    @Primary
    public DaprClient daprClient() {
        return Mockito.mock(DaprClient.class, Mockito.RETURNS_DEEP_STUBS);
    }
}
