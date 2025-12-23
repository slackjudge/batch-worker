package store.slackjudge.batch.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class ProblemJdbcRepository {
    private final JdbcTemplate jdbcTemplate;

    /*==========================
    *
    *ProblemJdbcRepository
    * 새로 문제를 풀이한 시간 + 풀이 유무 최신화 쿼리
    * index(user_id,solved_time) 인덱스 생성 + unique 처리
    * 1. 이미 (user_id,solved_time)의 데이터 존재 -> solved_time만 최신화
    * 2. 기존 값 존재하지 않음 -> INSERT
    * @parm batchTime:집계 기준일 / userId:users 테이블 PK / problemNumber:문제번호
    * @return void
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 8.
    *
    ==========================**/
    public void updateProblemSolved(LocalDateTime batchTime, Long userId, Integer problemNumber) {
        String sql = """
                INSERT INTO users_problem (user_id, problem_id, is_solved, solved_time)
                VALUES (?, ?, true, ?)
                ON CONFLICT (user_id, problem_id)
                DO UPDATE SET
                    solved_time = EXCLUDED.solved_time,
                    is_solved = EXCLUDED.is_solved
                """;

        jdbcTemplate.update(sql, userId, problemNumber, batchTime);
    }

    /*==========================
     *
     * ProblemJdbcRepository
     *
     * @parm snapshotAt 문제 풀이 스냅샷 기준 시각
     * @parm userId 문제를 푼 사용자 ID
     * @parm problemIds 사용자가 푼 문제 ID 목록
     * @return void
     * 사용자별 문제 풀이 정보를 중복 없이 일괄 삽입
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    public void batchInsertProblems(LocalDateTime snapshotAt, Long userId, List<Integer> problemIds) {
        String sql = """
                INSERT INTO users_problem (user_id, problem_id, is_solved, solved_time)
                VALUES (?, ?, true, ?)
                ON CONFLICT (user_id, problem_id) DO NOTHING
                """;

        jdbcTemplate.batchUpdate(sql, problemIds, problemIds.size(),
                (ps, problemId) -> {
                    ps.setLong(1, userId);
                    ps.setInt(2, problemId);
                    ps.setTimestamp(3, java.sql.Timestamp.valueOf(snapshotAt));
                });
    }
}
