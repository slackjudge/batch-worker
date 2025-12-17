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
import org.springframework.retry.RetryCallback;
import org.springframework.retry.support.RetryTemplate;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.infra.solvedac.client.SolvedAcUserInfoClient;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FetchSolvedAcUserInfoTaskletTest {

    @Mock private BatchLogger logger;
    @Mock private SolvedAcUserInfoClient userInfoClient;
    @Mock private RetryTemplate retryTemplate;

    @Mock private StepContribution contribution;
    @Mock private ChunkContext chunkContext;
    @Mock private StepContext stepContext;
    @Mock private StepExecution stepExecution;
    @Mock private JobExecution jobExecution;
    @Mock private ExecutionContext jobContext;
    @Mock private ExecutionContext stepExecutionContext;

    private FetchSolvedAcUserInfoTasklet tasklet;

    @BeforeEach
    void setUp() {
        when(chunkContext.getStepContext()).thenReturn(stepContext);
        when(stepContext.getStepExecution()).thenReturn(stepExecution);
        when(stepExecution.getJobExecution()).thenReturn(jobExecution);
        when(jobExecution.getExecutionContext()).thenReturn(jobContext);

        tasklet = new FetchSolvedAcUserInfoTasklet(
                logger,
                userInfoClient,
                retryTemplate
        );
    }

    @Test
    @DisplayName("모든 유저의 solved.ac 정보를 조회 -> ExecutionContext에 저장")
    void shouldFetchAllUserInfoAndSaveToContext() throws Exception {
        // given
        when(stepExecution.getExecutionContext()).thenReturn(stepExecutionContext);
        List<UserInfo> users = List.of(
                new UserInfo("boj1", 1L, 1),
                new UserInfo("boj2", 2L, 2)
        );
        when(jobContext.get("users")).thenReturn(users);

        UserInfoResponse response1 = createMockResponse("boj1", 1, 100, 1000);
        UserInfoResponse response2 = createMockResponse("boj2", 2, 200, 1500);

        when(retryTemplate.execute(any(RetryCallback.class))).thenAnswer(invocation ->
                invocation.getArgument(0, RetryCallback.class).doWithRetry(null)
        );

        when(userInfoClient.call("boj1", 0)).thenReturn(response1);
        when(userInfoClient.call("boj2", 0)).thenReturn(response2);

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        ArgumentCaptor<Map<String, UserInfoResponse>> captor = ArgumentCaptor.forClass(Map.class);
        verify(stepExecutionContext).put(eq("userSolvedInfo"), captor.capture());

        Map<String, UserInfoResponse> savedMap = captor.getValue();
        assertThat(savedMap).hasSize(2);
        assertThat(savedMap.get("boj1")).isEqualTo(response1);
        assertThat(savedMap.get("boj2")).isEqualTo(response2);

        verify(logger).stepStart("FetchSolvedAcUserInfoTasklet");
        verify(logger).stepEnd(eq("FetchSolvedAcUserInfoTasklet"), any(), any(), any(), any(), any(), any());
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
        verify(userInfoClient, never()).call(anyString(), anyInt());
        verify(logger).stepEnd("FetchSolvedAcUserInfoTasklet", "No users found");
    }

    @Test
    @DisplayName("빈 유저 리스트일 때 종료")
    void shouldFinishEarlyWhenEmptyUsers() throws Exception {
        // given
        when(jobContext.get("users")).thenReturn(Collections.emptyList());

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);
        verify(userInfoClient, never()).call(anyString(), anyInt());
        verify(logger).stepEnd("FetchSolvedAcUserInfoTasklet", "No users found");
    }

    @Test
    @DisplayName("API 호출 실패 시 해당 유저는 스킵 -> 다른 유저는 계속 처리")
    void shouldSkipFailedUserAndContinue() throws Exception {
        // given
        when(stepExecution.getExecutionContext()).thenReturn(stepExecutionContext);
        List<UserInfo> users = List.of(
                new UserInfo("boj1", 1L, 1),
                new UserInfo("boj2", 2L, 2),
                new UserInfo("boj3", 3L, 3)
        );
        when(jobContext.get("users")).thenReturn(users);

        UserInfoResponse response1 = createMockResponse("boj1", 1, 100, 1000);
        UserInfoResponse response3 = createMockResponse("boj3", 3, 300, 2000);

        when(retryTemplate.execute(any(RetryCallback.class))).thenAnswer(invocation ->
                invocation.getArgument(0, RetryCallback.class).doWithRetry(null)
        );

        when(userInfoClient.call("boj1", 0)).thenReturn(response1);
        when(userInfoClient.call("boj2", 0)).thenThrow(new RuntimeException("Network Error"));
        when(userInfoClient.call("boj3", 0)).thenReturn(response3);

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        ArgumentCaptor<Map<String, UserInfoResponse>> captor = ArgumentCaptor.forClass(Map.class);
        verify(stepExecutionContext).put(eq("userSolvedInfo"), captor.capture());

        Map<String, UserInfoResponse> savedMap = captor.getValue();
        assertThat(savedMap).hasSize(2);
        assertThat(savedMap.get("boj1")).isEqualTo(response1);
        assertThat(savedMap.get("boj3")).isEqualTo(response3);
        assertThat(savedMap).doesNotContainKey("boj2");

        verify(logger).userWarn(eq(2L), eq("boj2"), anyString());
    }

    @Test
    @DisplayName("모든 유저의 API 호출이 실패해도 정상 종료")
    void shouldFinishNormallyEvenWhenAllFailed() throws Exception {
        // given
        when(stepExecution.getExecutionContext()).thenReturn(stepExecutionContext);
        List<UserInfo> users = List.of(
                new UserInfo("boj1", 1L, 1)
        );
        when(jobContext.get("users")).thenReturn(users);

        when(retryTemplate.execute(any(RetryCallback.class))).thenAnswer(invocation ->
                invocation.getArgument(0, RetryCallback.class).doWithRetry(null)
        );

        when(userInfoClient.call("boj1", 0))
                .thenThrow(new RuntimeException("Network Error"));

        // when
        RepeatStatus status = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(status).isEqualTo(RepeatStatus.FINISHED);

        ArgumentCaptor<Map<String, UserInfoResponse>> captor = ArgumentCaptor.forClass(Map.class);
        verify(stepExecutionContext).put(eq("userSolvedInfo"), captor.capture());

        Map<String, UserInfoResponse> savedMap = captor.getValue();
        assertThat(savedMap).isEmpty();
    }

    private UserInfoResponse createMockResponse(String handle, int tier, int solvedCount, int rating) {
        return new UserInfoResponse(solvedCount, handle, tier, rating);
    }
}
