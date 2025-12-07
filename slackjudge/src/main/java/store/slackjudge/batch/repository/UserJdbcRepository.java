package store.slackjudge.batch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import store.slackjudge.batch.dto.UserInfo;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class UserJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    /*==========================
    *
    *UserJdbcRepository
    * 모든 유저들의 백준Id, PK, 백준 티어 조회
    * @return UserInfo record flat 형식의 유저 정보 리스트
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 7.
    *
    ==========================**/
    public List<UserInfo> findAllUserInfo() {
        String sql = """
                SELECT
                    u.baekjoon_id,
                    u.user_id,
                    u.boj_tier
                FROM
                    users AS u
                """;

        return jdbcTemplate.query(sql, ((rs, rowNum) ->
                new UserInfo(
                        rs.getString("baekjoon_id"),
                        rs.getLong("user_id"),
                        rs.getInt("boj_tier")
                )));
    }




}
