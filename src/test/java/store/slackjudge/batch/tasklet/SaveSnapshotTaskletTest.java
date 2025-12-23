package store.slackjudge.batch.tasklet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.dto.SaveSnapshot;
import store.slackjudge.batch.infra.mongo.service.UserSnapShotService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SaveSnapshotTaskletTest {

    @Mock
    private UserSnapShotService service;

    @Mock
    private BatchLogger logger;

    @Mock
    private StepContribution contribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private StepExecution stepExecution;

    @Mock
    private JobExecution jobExecution;

    @Mock
    private ExecutionContext jobContext;

    private SaveSnapshotTasklet tasklet;

    @BeforeEach
    void setUp() {
        tasklet = new SaveSnapshotTasklet(service, logger);

        // Mock 체인 설정
        StepContext mockStepContext = mock(StepContext.class);
        when(chunkContext.getStepContext()).thenReturn(mockStepContext);
        when(mockStepContext.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(jobContext);
    }

    @Test
    @DisplayName("모든 스냅샷을 저장")
    void shouldSaveAllSnapshots() {
        // given
        LocalDateTime snapshotAt = LocalDateTime.of(2024, 12, 15, 10, 0);

        SaveSnapshot snapshot1 = new SaveSnapshot(
            "user1",
            snapshotAt,
            Set.of(1000, 1001),
            2,
            5,
            1L,
            1200
        );

        SaveSnapshot snapshot2 = new SaveSnapshot(
            "user2",
            snapshotAt,
            Set.of(2000, 2001, 2002),
            3,
            4,
            2L,
            1500
        );

        Map<String, SaveSnapshot> currentSnapshots = new HashMap<>();
        currentSnapshots.put("user1", snapshot1);
        currentSnapshots.put("user2", snapshot2);

        when(jobContext.get("currentSnapshot")).thenReturn(currentSnapshots);

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        // 각 스냅샷이 저장되었는지 검증
        verify(service).saveSnapshot(snapshot1);
        verify(service).saveSnapshot(snapshot2);
        verify(service, times(2)).saveSnapshot(any(SaveSnapshot.class));

        // Logger 호출 검증
        verify(logger).stepStart("SaveSnapshotTasklet");
        verify(logger).stepEnd(eq("SaveSnapshotTasklet"), any(), any());
    }

    @Test
    @DisplayName("스냅샷이 null이면 조기 종료")
    void shouldFinishEarlyWhenSnapshotsIsNull() {
        // given
        when(jobContext.get("currentSnapshot")).thenReturn(null);

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(service, never()).saveSnapshot(any());
        verify(logger).stepEnd("SaveSnapshotTasklet", "No current snapshots");
    }

    @Test
    @DisplayName("스냅샷이 비어있으면 조기 종료")
    void shouldFinishEarlyWhenSnapshotsIsEmpty() {
        // given
        when(jobContext.get("currentSnapshot")).thenReturn(Collections.emptyMap());

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(service, never()).saveSnapshot(any());
        verify(logger).stepEnd("SaveSnapshotTasklet", "No current snapshots");
    }

    @Test
    @DisplayName("단일 스냅샷도 정상적으로 저장")
    void shouldSaveSingleSnapshot() {
        // given
        LocalDateTime snapshotAt = LocalDateTime.of(2024, 12, 15, 10, 0);

        SaveSnapshot snapshot = new SaveSnapshot(
            "user1",
            snapshotAt,
            Set.of(1000, 1001),
            2,
            5,
            1L,
            1200
        );

        Map<String, SaveSnapshot> currentSnapshots = Map.of("user1", snapshot);
        when(jobContext.get("currentSnapshot")).thenReturn(currentSnapshots);

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(service).saveSnapshot(snapshot);
        verify(service, times(1)).saveSnapshot(any(SaveSnapshot.class));
    }

    @Test
    @DisplayName("빈 문제 세트를 가진 스냅샷도 저장")
    void shouldSaveSnapshotWithEmptyProblemSet() {
        // given
        LocalDateTime snapshotAt = LocalDateTime.of(2024, 12, 15, 10, 0);

        SaveSnapshot snapshot = new SaveSnapshot(
            "newUser",
            snapshotAt,
            Collections.emptySet(),
            0,
            5,
            1L,
            0
        );

        Map<String, SaveSnapshot> currentSnapshots = Map.of("newUser", snapshot);
        when(jobContext.get("currentSnapshot")).thenReturn(currentSnapshots);

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(service).saveSnapshot(snapshot);
    }
}