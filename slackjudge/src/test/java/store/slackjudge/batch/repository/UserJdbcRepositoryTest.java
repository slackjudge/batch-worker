package store.slackjudge.batch.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import store.slackjudge.batch.EnablePostgresTest;
import store.slackjudge.batch.dto.UserInfo;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
@ActiveProfiles("test")
@EnablePostgresTest
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(UserJdbcRepository.class)
class UserJdbcRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UserJdbcRepository repository;

    //더미 user 생성 및 저장
    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");

        String insert = """
                INSERT INTO users (slack_id, baekjoon_id, user_name, boj_tier, team_name,total_solved_count)
                VALUES (?, ?, ?, ?, ?,?)
                """;

        Object[][] dummyUsers = {
                {"slack1", "test1", "user1", 10, "teamA",100},
                {"slack2", "test2", "user2", 20, "teamA",200},
                {"slack3", "test3", "user3", 30, "teamB",300},
                {"slack4", "test4", "user4", 40, "teamB",400}
        };

        for (Object[] user : dummyUsers) {
            jdbcTemplate.update(insert, user);
        }
    }


    @Test
    @DisplayName("모든 유저를 조회하고 UserInfo 형식의 리스트 반환")
    void findAllUserInfo() {
        //given & when
        List<UserInfo> results=repository.findAllUserInfo();

        //then
        assertThat(results).isNotEmpty();
        assertThat(results.size()).isEqualTo(4);

        List<String> bojIds=new ArrayList<>();
        for (UserInfo info:results){
            bojIds.add(info.baekJoonId());
        }
        assertThat(bojIds).containsExactlyInAnyOrder("test1","test2","test3","test4");
    }

    @Test
    @DisplayName("유저의 새로운 티어를 업데이트")
    void updateUsersTier() {
        //given
        String bojId="test3"; //test3 유저 업데이트
        int newTier=42;
        int newSolvedCount=3333;

        //when
        repository.updateUsersTier(bojId,newTier,newSolvedCount);

        //then
        String tierSql="SELECT boj_tier FROM users WHERE baekjoon_id = ?";
        String countSql="SELECT total_solved_count FROM users WHERE baekjoon_id = ?";

        String fetchUsersTier=jdbcTemplate.queryForObject(tierSql,String.class,bojId);
        Integer solvedCount=jdbcTemplate.queryForObject(countSql,Integer.class,bojId);

        assertThat(fetchUsersTier).isEqualTo(String.valueOf(newTier));
        assertThat(solvedCount).isEqualTo(newSolvedCount);
    }
}