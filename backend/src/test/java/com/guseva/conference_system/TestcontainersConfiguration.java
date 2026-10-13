package com.guseva.conference_system;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.postgresql.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

/** Временная PostgreSQL для тестов. Поднимается в Docker и удаляется после тестов. */
@TestConfiguration(proxyBeanMethods = false)
public class TestcontainersConfiguration {

    // @ServiceConnection сообщает Spring: подключайся к этой базе вместо настроек
    // из application.properties
    @Bean
    @ServiceConnection
    PostgreSQLContainer postgresContainer() {
        return new PostgreSQLContainer(DockerImageName.parse("postgres:16"));
    }
}