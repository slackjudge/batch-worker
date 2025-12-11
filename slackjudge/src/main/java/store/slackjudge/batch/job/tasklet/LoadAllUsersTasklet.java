package store.slackjudge.batch.job.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.repository.UserJdbcRepository;

import java.util.List;

/**
 * [ step 01 ]
 * RDB로부터 모든 유저 정보를 조회하는 tasklet
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class LoadAllUsersTasklet implements Tasklet {
    private final UserJdbcRepository userJdbcRepository;

    /**
     * next step : LoadSnapshotTasklet
     */

    private StepExecution stepExecution;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext){
        //모든 유저 정보 조회
        List<UserInfo> users=userJdbcRepository.findAllUserInfo();
        //햔재 step에 대한 메타데이터 객체
        ExecutionContext stepContext=this.stepExecution.getExecutionContext();
        //{ users : List<UserInfo> } 형태로 메타데이터 저장
        stepContext.put("users",users);

        return RepeatStatus.FINISHED;
    }

    //step 실행 전 Step Execution 가져오기
    @BeforeStep
    public void saveStepExecution(StepExecution stepExecution){
        this.stepExecution=stepExecution;
    }
}
