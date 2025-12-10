package store.slackjudge.batch.infra.mongo.service.detector;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.solvedac.dto.ProblemInfoResponse;
import store.slackjudge.batch.infra.solvedac.dto.ProblemSearchResponse;
import store.slackjudge.batch.repository.ProblemJdbcRepository;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/* [ 사용자가 푼 문제 감지 전략 ]
 * 변경 감지 전략 구현체
 */
@Component
@RequiredArgsConstructor
public class ProblemChangeDetector implements SnapshotDetectStrategy<ProblemSearchResponse> {
    private final ProblemJdbcRepository repository;

    @Override
    public boolean detect(DetectionContext<ProblemSearchResponse> context) {
        return context.previous().getSolvedCount() != context.current().count();
    }

    @Override
    public void update(DetectionContext<ProblemSearchResponse> context) {
        //스냅샷에 존재하는 유저의 모든 문제 번호
        Set<Integer> previousProblemIds = context.previous().getSolvedProblemIds();

        //새로 조회한 유저의 모든 문제 번호
        Set<Integer> currentProblemIds =
                context.current()
                        .items()
                        .stream()
                        .map(ProblemInfoResponse::problemId)
                        .collect(Collectors.toSet());

        //새로 푼 문제 업데이트
        currentProblemIds.stream()
                //사용자가 이전에 푼 문제 set에 포함되지 않은 경우 -> 새로 푼 문제
                .filter(p -> !previousProblemIds.contains(p))
                .forEach(pid -> {
                    repository.updateProblemSolved(context.snapshotAt(), context.userId(), pid);
                });
    }
}
