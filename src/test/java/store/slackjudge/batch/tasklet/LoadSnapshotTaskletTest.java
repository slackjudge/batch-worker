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
import store.slackjudge.batch.common.CalculateSnapShotDate;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.repository.UserSolvedSnapShotRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadSnapshotTaskletTest {

    @Mock
    private UserSolvedSnapShotRepository repository;

    @Mock
    private CalculateSnapShotDate snapShotDate;

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
    private ExecutionContext stepContext;

    @Mock
    private ExecutionContext jobContext;

    private LoadSnapshotTasklet tasklet;

    private LocalDateTime batchTime = LocalDateTime.of(2024, 12, 15, 10, 0);

    @BeforeEach
    void setUp() {
        tasklet = new LoadSnapshotTasklet(
                repository,
                snapShotDate,
                logger,
                batchTime
        );

        // Mock 체인 설정
        StepContext stepContext = mock(StepContext.class);
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(jobContext);
    }

    @Test
    @DisplayName("유저 스냅샷을 조회하고 ExecutionContext에 저장")
    void shouldLoadSnapshotsAndSaveToContext() throws Exception {
        // given
        LocalDateTime snapshotAt = LocalDateTime.of(2024, 12, 15, 9, 0);
        when(snapShotDate.snapshotDate(batchTime)).thenReturn(snapshotAt);
        when(stepExecution.getExecutionContext()).thenReturn(stepContext);

        List<UserInfo> users = List.of(
                new UserInfo("user1", 1L, 1),
                new UserInfo("user2", 2L, 2)
        );
        when(jobContext.get("users")).thenReturn(users);

        UserSolvedSnapShotDocument doc1 = createMockDocument("user1", snapshotAt);
        UserSolvedSnapShotDocument doc2 = createMockDocument("user2", snapshotAt);

        when(repository.findByIdBojIdAndIdSnapShotAt("user1", snapshotAt))
                .thenReturn(Optional.of(doc1));
        when(repository.findByIdBojIdAndIdSnapShotAt("user2", snapshotAt))
                .thenReturn(Optional.of(doc2));

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        ArgumentCaptor captor =
                ArgumentCaptor.forClass(Map.class);
        verify(stepContext).put(eq("snapshots"), captor.capture());

        Map<String, UserSolvedSnapShotDocument> snapshots = (Map<String, UserSolvedSnapShotDocument>) captor.getValue();
        assertThat(snapshots).hasSize(2);
        assertThat(snapshots.get("user1")).isEqualTo(doc1);
        assertThat(snapshots.get("user2")).isEqualTo(doc2);
    }

    @Test
    @DisplayName("스냅샷이 없는 유저는 null로 처리")
    void shouldHandleMissingSnapshot() throws Exception {
        // given
        LocalDateTime snapshotAt = LocalDateTime.of(2024, 12, 15, 9, 0);
        when(snapShotDate.snapshotDate(batchTime)).thenReturn(snapshotAt);
        when(stepExecution.getExecutionContext()).thenReturn(stepContext);

        List<UserInfo> users = List.of(new UserInfo("user1", 1L, 1));
        when(jobContext.get("users")).thenReturn(users);

        when(repository.findByIdBojIdAndIdSnapShotAt("user1", snapshotAt))
                .thenReturn(Optional.empty());

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        ArgumentCaptor<Map<String, UserSolvedSnapShotDocument>> captor =
                ArgumentCaptor.forClass(Map.class);
        verify(stepContext).put(eq("snapshots"), captor.capture());

        Map<String, UserSolvedSnapShotDocument> snapshots = captor.getValue();
        assertThat(snapshots.get("user1")).isNull();
    }

    @Test
    @DisplayName("유저가 없으면 조기 종료")
    void shouldFinishEarlyWhenNoUsers() throws Exception {
        // given
        when(jobContext.get("users")).thenReturn(null);

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(repository, never()).findByIdBojIdAndIdSnapShotAt(any(), any());
        verify(logger).stepEnd(eq("LoadSnapshotTasklet"), eq("No users found"));
    }

    // Mock 문서 생성
    private UserSolvedSnapShotDocument createMockDocument(String bojId, LocalDateTime snapShotAt) {
        return new UserSolvedSnapShotDocument(/* ... */);
    }
}