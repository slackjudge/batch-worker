package store.slackjudge.batch.repository;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import store.slackjudge.batch.EnablePostgresTest;
import store.slackjudge.batch.PostgresTestContainer;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@EnablePostgresTest
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(ProblemJdbcRepository.class)
class ProblemJdbcRepositoryTest {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ProblemJdbcRepository repository;

    @BeforeEach
    void clean() {
        jdbcTemplate.execute("TRUNCATE TABLE users_problem RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE users RESTART IDENTITY CASCADE");
        jdbcTemplate.execute("TRUNCATE TABLE problem RESTART IDENTITY CASCADE");
        //제약조건
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM pg_constraint 
                WHERE conrelid = 'users_problem'::regclass 
                AND conname = 'users_problem_unique'
                """, Integer.class);

        // 없으면 추가
        if (count != null && count.equals(0)) {
            jdbcTemplate.execute("""
                    ALTER TABLE users_problem
                    ADD CONSTRAINT users_problem_unique UNIQUE (user_id, problem_id)
                    """);
        }

        // 테스트용 사용자 데이터 삽입
        jdbcTemplate.update("""
                INSERT INTO users (user_id, slack_id, baekjoon_id, user_name, boj_tier, team_name)
                VALUES (12, 'slack', 'test', 'user', 1, 'Test Team')
                """);

        // 테스트용 문제 데이터 삽입
        jdbcTemplate.update("""
                INSERT INTO problem (problem_id, problem_title, problem_level, problem_url)
                VALUES (1000, 'A+B', 1, 'https://www.acmicpc.net/problem/1000')
                """);
        jdbcTemplate.update("""
                INSERT INTO problem (problem_id, problem_title, problem_level, problem_url)
                VALUES (1001, 'A+B+C', 1, 'https://www.acmicpc.net/problem/1001')
                """);
    }

    record UsersProblem(
            Long userId,
            Integer problemId,
            Boolean isSolved,
            LocalDateTime solvedTime
    ) {
    }

    @DisplayName("새로운 문제 풀이가 없으면 INSERT 수행")
    @Test
    void insertProblemSolved() {
        //given
        Long userId = 12L;
        Integer problemId = 1000;
        LocalDateTime batchTime = LocalDateTime.now();

        //when
        repository.updateProblemSolved(batchTime, userId, problemId);

        //then
        String sql = """
                SELECT *
                FROM users_problem
                WHERE user_id = ? AND problem_id = ?
                """;
        UsersProblem usersProblem = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) ->
                        new UsersProblem(
                                rs.getLong("user_id"),
                                rs.getInt("problem_id"),
                                rs.getBoolean("is_solved"),
                                rs.getTimestamp("solved_time").toLocalDateTime()
                        ),
                userId,
                problemId
        );

        assertNotNull(usersProblem);
        assertThat(usersProblem.isSolved).isTrue();
        assertThat(usersProblem.problemId).isEqualTo(problemId);
        assertThat(usersProblem.solvedTime).isEqualTo(batchTime);
        assertThat(usersProblem.userId).isEqualTo(userId);
    }

    @DisplayName("문제 풀이가 있으면 UPDATE 수행")
    @Test
    void updateProblemSolved() {
        //given
        Long userId = 12L;
        Integer problemId = 1001;
        LocalDateTime epochTime = LocalDateTime.of(1970, 1, 1, 0, 0, 0);
        LocalDateTime batchTime = LocalDateTime.now();

        jdbcTemplate.update("""
                        INSERT INTO users_problem (user_id, problem_id, is_solved, solved_time)
                        VALUES (?, ?, true, ?)
                """, 12, 1001, epochTime);
        //when
        repository.updateProblemSolved(batchTime, userId, problemId);

        //then
        String sql = """
                SELECT *
                FROM users_problem
                WHERE user_id = ? AND problem_id = ?
                """;
        UsersProblem usersProblem = jdbcTemplate.queryForObject(
                sql,
                (rs, rowNum) ->
                        new UsersProblem(
                                rs.getLong("user_id"),
                                rs.getInt("problem_id"),
                                rs.getBoolean("is_solved"),
                                rs.getTimestamp("solved_time").toLocalDateTime()
                        ),
                userId,
                problemId
        );

        assertNotNull(usersProblem);
        assertThat(usersProblem.isSolved).isTrue();
        assertThat(usersProblem.problemId).isEqualTo(problemId);
        assertThat(usersProblem.solvedTime).isEqualTo(batchTime);
        assertThat(usersProblem.userId).isEqualTo(userId);
    }

}