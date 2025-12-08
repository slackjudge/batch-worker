
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Profile;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Testcontainers;



@Profile("test")
@Testcontainers
@TestConfiguration(proxyBeanMethods = false)
public class PostgresTestContainer {
    private static final Logger log= LoggerFactory.getLogger(PostgresTestContainer.class);
    private static final String IMAGE_VERSION = "postgres:16";
    private static final GenericContainer POSTGRES_CONTAINER;

    static {
        POSTGRES_CONTAINER=new GenericContainer<>(IMAGE_VERSION)
                .withExposedPorts(5555)
                .withEnv("POSTGRES_USER","test")
                .withEnv("POSTGRES_PASSWORD","pwd")
                .withEnv("POSTGRES_DB","test")
                .withEnv("POSTGRES_HOST_AUTH_METHOD","trust");

        POSTGRES_CONTAINER.withLogConsumer(new Slf4jLogConsumer(log));

        POSTGRES_CONTAINER.start();
    }

    @DynamicPropertySource
    public static void overrideProps(DynamicPropertyRegistry registry){
        registry
                .add("spring.datasource.url",()->String.format("jdbc:postgresql://localhost:%d",POSTGRES_CONTAINER.getFirstMappedPort()));
        registry
                .add("spring.datasource.username",()->"postgres");
        registry
                .add("spring.datasource.password",()->"pwd");
    }
}
