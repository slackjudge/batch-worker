package store.slackjudge.batch;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@EnablePostgresTest
public class TestContainer {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void test(){
        String sql= """
                INSERT INTO users (slack_id, baekjoon_id, user_name, boj_tier, team_name)
                VALUES ('tkv00','tkv00','김도연',12,'백엔드');
                """;
        jdbcTemplate.update(sql);
    }
}
