package store.slackjudge.batch.infra.mongo.service.detector;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.solvedac.dto.ProblemInfoResponse;
import store.slackjudge.batch.infra.solvedac.dto.ProblemSearchResponse;
import store.slackjudge.batch.repository.ProblemJdbcRepository;
import store.slackjudge.batch.service.DetectionContext;
import store.slackjudge.batch.service.ProblemChangeDetector;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProblemChangeDetectorTest {
    @InjectMocks
    private ProblemChangeDetector detector;

    @Mock
    private ProblemJdbcRepository repository;

    @Test
    @DisplayName("context의 1시간 전 스냅샷의 유저의 문제 풀이 개수와 " +
                 "새로 조회한 유저의 문제 풀이 개수가 일치하지 않으면 true 반환"
    )
    void detect_Return_TRUE() {
        //given
        List<Integer> current = new ArrayList<>();
        UserSolvedSnapShotDocument previous = UserSolvedSnapShotDocument.builder()
                .solvedCount(13)
                .build();

        DetectionContext<List<Integer>> context = new DetectionContext<>(current, previous, LocalDateTime.now(), 1L, "");

        //when
        Boolean result = detector.detect(context);

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("context의 1시간 전 스냅샷의 유저의 문제 풀이 개수와 " +
                 "새로 조회한 유저의 문제 풀이 개수가 일치하면 false 반환"
    )
    void detect_Return_FALSE() {
        //given
        List<Integer> current =  List.of(1,2,3,4,5,6,7,8,9,10);
        UserSolvedSnapShotDocument previous = UserSolvedSnapShotDocument.builder()
                .solvedCount(10)
                .build();

        DetectionContext<List<Integer>> context = new DetectionContext<>(current, previous, LocalDateTime.now(), 1L, "");

        //when
        Boolean result = detector.detect(context);

        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("새로 푼 문제만 RDB에 update")
    void update_New_Solved() {
        //given
        Set<Integer> previousSolvedProblems = Set.of(12, 45, 37, 4); //이전에 푼 문제 번호
        List<Integer> currentSolvedProblem = List.of(
                12,
                45,
                37,
                4,
                23,
                11,
                5
        ); //새로 푼 문제 번호 -> 23,11,5

        UserSolvedSnapShotDocument previous = UserSolvedSnapShotDocument.builder()
                .solvedProblemIds(previousSolvedProblems).build();
        List<Integer> current = new ArrayList<>(currentSolvedProblem);
        DetectionContext<List<Integer>> context = new DetectionContext<>(current, previous, LocalDateTime.now(), 1L, "test");

        //when
        detector.update(context);

        //then
        verify(repository, times(3)).updateProblemSolved(any(), anyLong(), anyInt());
        verify(repository).updateProblemSolved(context.snapshotAt(), context.userId(), 23);
        verify(repository).updateProblemSolved(context.snapshotAt(), context.userId(), 11);
        verify(repository).updateProblemSolved(context.snapshotAt(), context.userId(), 5);
    }

    @Test
    @DisplayName("새로 푼 문제가 없으면 update 호출x")
    void update_Nothing() {
        //given
        Set<Integer> previousSolvedProblems = Set.of(12, 45, 37, 4); //이전에 푼 문제 번호
        List<Integer> currentSolvedProblem = List.of(12, 45, 37, 4);

        UserSolvedSnapShotDocument previous = UserSolvedSnapShotDocument.builder()
                .solvedProblemIds(previousSolvedProblems).build();
        List<Integer> current = new ArrayList<>(currentSolvedProblem);
        DetectionContext<List<Integer>> context = new DetectionContext<>(current, previous, LocalDateTime.now(), 1L, "test");

        //when
        detector.update(context);

        //then
         verify(repository, times(0)).updateProblemSolved(any(), anyLong(), anyInt());
    }
}
