package org.example.bookingrent;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.springframework.context.annotation.Bean;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    public PostgreSQLContainer<?> postgreSQLContainer() {
        return new PostgreSQLContainer<>(DockerImageName.parse("postgres:16-alpine"))
                .withDatabaseName("testdb")
                .withUsername("test")
                .withPassword("test");
    }


    @Bean
    public GenericContainer<?> daprContainer() {
        GenericContainer<?> container = new GenericContainer<>(DockerImageName.parse("daprio/daprd:latest"))
                .withExposedPorts(3500, 50001)
                .withCommand(
                        "./daprd",
                        "--app-id", "booking-service",
                        "--dapr-http-port", "3500",
                        "--dapr-grpc-port", "50001"
                );
        container.start();
        System.setProperty("DAPR_HTTP_PORT", container.getMappedPort(3500).toString());
        System.setProperty("DAPR_GRPC_PORT", container.getMappedPort(50001).toString());
        return container;
    }
}
