package store.slackjudge.batch.tasklet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.retry.support.RetryTemplate;
import store.slackjudge.batch.common.CalculateSnapShotDate;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.infra.mongo.document.SnapShotId;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.dto.SaveSnapshot;
import store.slackjudge.batch.service.DetectionContext;
import store.slackjudge.batch.service.ProblemChangeDetector;
import store.slackjudge.batch.service.TierChangeDetector;
import store.slackjudge.batch.infra.solvedac.client.SolvedAcProblemInfoClient;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DetectAndUpdateUserTierAndProblemTaskletTest {

    @Mock
    private ProblemChangeDetector problemChangeDetector;
    @Mock
    private TierChangeDetector tierChangeDetector;
    @Mock
    private SolvedAcProblemInfoClient problemInfoClient;
    @Mock
    private CalculateSnapShotDate calculateSnapShotDate;
    @Mock
    private BatchLogger logger;
    @Mock
    private StepContribution stepContribution;

    private ChunkContext chunkContext;
    private StepExecution stepExecution;
    private JobExecution jobExecution;

    private ExecutionContext jobContext;

    private RetryTemplate retryTemplate = new RetryTemplate();

    private DetectAndUpdateUserTierAndProblemTasklet tasklet;

    private final LocalDateTime BATCH_TIME =
            LocalDateTime.of(2025, 12, 16, 10, 0);
    private final LocalDateTime SNAPSHOT_AT =
            LocalDateTime.of(2025, 12, 16, 9, 0);

    @BeforeEach
    void setUp() {
        tasklet = new DetectAndUpdateUserTierAndProblemTasklet(
                problemChangeDetector,
                tierChangeDetector,
                problemInfoClient,
                calculateSnapShotDate,
                logger,
                retryTemplate,
                BATCH_TIME
        );

        jobExecution = new JobExecution(1L);
        jobContext = jobExecution.getExecutionContext();
        stepExecution = new StepExecution("step", jobExecution);

        StepContext stepContext = new StepContext(stepExecution);
        chunkContext = new ChunkContext(stepContext);
    }

    @Test
    @DisplayName("사용자 목록이 비어있으면 즉시 종료")
    void execute_noUsers() throws Exception {
        jobContext.put("users", Collections.emptyList());

        RepeatStatus result = tasklet.execute(stepContribution, chunkContext);

        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
        verify(logger).stepEnd(
                eq("DetectAndUpdateUserTierAndProblemTasklet"),
                eq("No users found")
        );
        verifyNoInteractions(problemChangeDetector, tierChangeDetector, problemInfoClient);
    }

    @Test
    @DisplayName("신규 유저 처리 성공")
    void execute_newUser() throws Exception {
        //given
        when(calculateSnapShotDate.currentHour(BATCH_TIME))
                .thenReturn(SNAPSHOT_AT);

        UserInfo user = new UserInfo("boj1", 1L, 1);
        UserInfoResponse response =
                new UserInfoResponse(10, "boj1", 3, 100);

        jobContext.put("users", List.of(user));
        jobContext.put("snapshots", Collections.emptyMap());
        jobContext.put("userSolvedInfo", Map.of("boj1", response));

        when(problemInfoClient.fetchAllProblems("boj1"))
                .thenReturn(List.of(1, 2, 3));

        //when
        RepeatStatus result = tasklet.execute(stepContribution, chunkContext);

        //then
        assertThat(result).isEqualTo(RepeatStatus.FINISHED);

        verify(problemChangeDetector).saveNewProblem(
                eq(SNAPSHOT_AT),
                eq(1L),
                any()
        );
    }

    @Test
    @DisplayName("기존 유저 - 티어 변경")
    void execute_existingUser_tierChange() throws Exception {
        //given
        UserInfo user = new UserInfo("boj1", 1L, 5);
        UserInfoResponse response =
                new UserInfoResponse(5, "boj1", 4, 200);

        UserSolvedSnapShotDocument prev =
                UserSolvedSnapShotDocument.builder()
                        .id(SnapShotId.of("boj1", SNAPSHOT_AT))
                        .tier(3)
                        .solvedCount(5)
                        .solvedProblemIds(Set.of(1, 2))
                        .build();

        jobContext.put("users", List.of(user));
        jobContext.put("snapshots", Map.of("boj1", prev));
        jobContext.put("userSolvedInfo", Map.of("boj1", response));

        when(tierChangeDetector.detect(any())).thenReturn(true);

        //when
        RepeatStatus result = tasklet.execute(stepContribution, chunkContext);

        //then
        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
        assertThat(jobContext.getInt("UPDATED_USERS")).isEqualTo(1);

        verify(tierChangeDetector).update(any());
        verify(problemInfoClient, never()).fetchAllProblems(anyString());
    }

    @Test
    @DisplayName("기존 유저 - 문제 변경")
    void execute_existingUser_problemChange() throws Exception {
        //given
        UserInfo user = new UserInfo("boj1", 1L, 5);
        UserInfoResponse response =
                new UserInfoResponse(6, "boj1", 3, 200);

        UserSolvedSnapShotDocument prev =
                UserSolvedSnapShotDocument.builder()
                        .id(SnapShotId.of("boj1", SNAPSHOT_AT))
                        .tier(3)
                        .solvedCount(5)
                        .solvedProblemIds(Set.of(1, 2))
                        .build();

        jobContext.put("users", List.of(user));
        jobContext.put("snapshots", Map.of("boj1", prev));
        jobContext.put("userSolvedInfo", Map.of("boj1", response));

        when(problemInfoClient.fetchAllProblems("boj1"))
                .thenReturn(List.of(1, 2, 3));
        when(problemChangeDetector.detect(any())).thenReturn(true);

        //when
        RepeatStatus result = tasklet.execute(stepContribution, chunkContext);

        //then
        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
        assertThat(jobContext.getInt("UPDATED_USERS")).isEqualTo(1);

        verify(problemChangeDetector).update(any());
    }

    @Test
    @DisplayName("solved.ac 정보 없는 유저 스킵")
    void execute_skipUser() throws Exception {
        //given
        UserInfo user = new UserInfo("fail", 1L, 1);

        jobContext.put("users", List.of(user));
        jobContext.put("userSolvedInfo", Collections.emptyMap());

        //when
        RepeatStatus result = tasklet.execute(stepContribution, chunkContext);

        //then
        assertThat(result).isEqualTo(RepeatStatus.FINISHED);
        assertThat(jobContext.getInt("FAILED_USERS")).isEqualTo(1);

        verify(logger)
                .taskletWarn("Skipped user due to no solved.ac info: fail");
    }
}