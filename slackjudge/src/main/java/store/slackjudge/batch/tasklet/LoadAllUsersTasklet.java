package store.slackjudge.batch.tasklet;

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
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.repository.UserJdbcRepository;

import java.util.List;

/**
 * [ step 01 ]
 * RDB로부터 모든 유저 정보를 조회하는 tasklet
 */

@RequiredArgsConstructor
@Component
public class LoadAllUsersTasklet implements Tasklet {
    private final UserJdbcRepository userJdbcRepository;
    private final BatchLogger logger;

    /**
     * next step : LoadSnapshotTasklet
     */

    /*==========================
     *
     * execute
     *
     * @parm contribution Step 실행 기여도 정보
     * @parm chunkContext Step/Job 실행 컨텍스트
     * @return RepeatStatus Tasklet 실행 완료 여부
     *
     * RDB에서 전체 사용자 정보를 조회한 후
     * 다음 Step에서 사용할 수 있도록 ExecutionContext에 저장
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext){
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();

        logger.stepStart("LoadAllUsersTasklet");
        //시작 시간
        long startTime=System.currentTimeMillis();

        //모든 유저 정보 조회
        List<UserInfo> users=userJdbcRepository.findAllUserInfo();

        //종료 시간 - 시작 시간
        long duration=System.currentTimeMillis()-startTime;

        //햔재 step에 대한 메타데이터 객체
        ExecutionContext stepContext=stepExecution.getExecutionContext();
        //{ users : List<UserInfo> } 형태로 메타데이터 저장
        stepContext.put("users",users);

        logger.stepEnd("LoadAllUsersTasklet","usersSize="+users.size(),"duration="+duration);

        return RepeatStatus.FINISHED;
    }
}
