package store.slackjudge.batch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@EnablePostgresTest
@EnableAutoConfiguration(exclude = {
        MongoAutoConfiguration.class,
        MongoDataAutoConfiguration.class
})
@ActiveProfiles("test")
public class TestContainer {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void test(){

    }
}
