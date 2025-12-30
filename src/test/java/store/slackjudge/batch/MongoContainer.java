package store.slackjudge.batch;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@ActiveProfiles("test")
public class MongoContainer {
    @Container
    static final MongoDBContainer mongo=new MongoDBContainer("mongo:6.0")
            .withReuse(true);

    @DynamicPropertySource
    static void mongoProperties(DynamicPropertyRegistry registry){
        registry.add(
                "spring.data.mongodb.uri",
                mongo::getReplicaSetUrl
        );
    }
}
