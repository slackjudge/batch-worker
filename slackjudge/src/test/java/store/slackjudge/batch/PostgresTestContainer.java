package store.slackjudge.batch;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;

@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestContainer {
    private static final Logger log= LoggerFactory.getLogger(PostgresTestContainer.class);
    private static final String IMAGE_VERSION = "postgres:16";

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(IMAGE_VERSION)
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("pwd")
                    .withInitScript("init_schema.sql");

    static {
        POSTGRES.withLogConsumer(new Slf4jLogConsumer(log));
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideProps(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
    }
}
