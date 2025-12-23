package store.slackjudge.batch.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.repository.ProblemJdbcRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/* [ 사용자가 푼 문제 감지 전략 ]
 * 변경 감지 전략 구현체
 */
@Component
@RequiredArgsConstructor
public class ProblemChangeDetector implements SnapshotDetectStrategy<List<Integer>> {
    private final ProblemJdbcRepository repository;

    /*==========================
    *
    *ProblemChangeDetector
    * solved.ac API 통신으로 최신 유저가 푼 문제 정보와 1시간 전 유저가 푼 문제 정보 비교 메서드
    * @parm context:변경감지 객체
    * @return true:변경 o / false:변경 x
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    @Override
    public boolean detect(DetectionContext<List<Integer>> context) {
        return context.previous().getSolvedCount() != context.current().size();
    }

    /*==========================
    *
    *ProblemChangeDetector
    * RDB에 유저의 새로 푼 문제 업데이트
    * @parm context:변경감지 객체
    * @return void
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    @Override
    public void update(DetectionContext<List<Integer>> context) {
        //스냅샷에 존재하는 유저의 모든 문제 번호
        Set<Integer> previousProblemIds = context.previous().getSolvedProblemIds();

        //새로 조회한 유저의 모든 문제 번호
        Set<Integer> currentProblemIds =
                new HashSet<>(context.current());

        //새로 푼 문제 업데이트
        currentProblemIds.stream()
                //사용자가 이전에 푼 문제 set에 포함되지 않은 경우 -> 새로 푼 문제
                .filter(p -> !previousProblemIds.contains(p))
                .forEach(pid -> {
                    repository.updateProblemSolved(context.snapshotAt(), context.userId(), pid);
                });
    }

    /*==========================
    *
    *ProblemChangeDetector
    * 신규 유저의 문제를 데이터베이스에 저장
    * @param snapshotAt: 스냅샷 시각
    * @param userId: 유저 ID
    * @param problemId: 문제 번호
    * @return void
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 13.
    *
    ==========================**/
    public void saveNewProblem(LocalDateTime snapshotAt, Long userId, List<Integer>problemIds) {
        repository.batchInsertProblems(snapshotAt, userId, problemIds);
    }
}
