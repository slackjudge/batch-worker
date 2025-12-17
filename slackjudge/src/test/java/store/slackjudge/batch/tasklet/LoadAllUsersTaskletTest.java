package store.slackjudge.batch.tasklet;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.repository.UserJdbcRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.scope.context.StepContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import store.slackjudge.batch.dto.UserInfo;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LoadAllUsersTaskletTest {

    @Mock
    private BatchLogger logger;

    @Mock
    private UserJdbcRepository repository;

    @Mock
    private StepContribution contribution;

    private LoadAllUsersTasklet tasklet;

    private StepExecution stepExecution;
    private ChunkContext chunkContext;

    @BeforeEach
    void setUp() {
        tasklet = new LoadAllUsersTasklet(repository, logger);

        stepExecution = new StepExecution("LoadAllUsersStep", null);
        StepContext stepContext = new StepContext(stepExecution);
        chunkContext = new ChunkContext(stepContext);
    }

    @Test
    @DisplayName("모든 유저 정보를 조회하고 ExecutionContext에 저장")
    void execute_success() {
        // given
        List<UserInfo> users = List.of(
                new UserInfo("boj1", 5L, 100),
                new UserInfo("boj2", 10L, 300)
        );

        when(repository.findAllUserInfo()).thenReturn(users);

        // when
        RepeatStatus result = tasklet.execute(contribution, chunkContext);

        // then
        assertThat(result).isEqualTo(RepeatStatus.FINISHED);

        ExecutionContext executionContext = stepExecution.getExecutionContext();
        assertThat(executionContext.containsKey("users")).isTrue();
        assertThat((List<UserInfo>) executionContext.get("users"))
                .hasSize(2)
                .isEqualTo(users);

        verify(repository, times(1)).findAllUserInfo();
        verify(logger, times(1)).stepStart("LoadAllUsersTasklet");
        verify(logger, times(1))
                .stepEnd(eq("LoadAllUsersTasklet"),
                         eq("usersSize=2"),
                         startsWith("duration="));
    }
}
