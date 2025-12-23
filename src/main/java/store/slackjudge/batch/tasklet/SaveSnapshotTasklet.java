package store.slackjudge.batch.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.infra.mongo.dto.SaveSnapshot;
import store.slackjudge.batch.infra.mongo.service.UserSnapShotService;

import java.util.List;
import java.util.Map;

/**
 * [ step 05 ]
 * 최신 스냅샷 저장 tasklet
 */

@Component
@RequiredArgsConstructor
public class SaveSnapshotTasklet implements Tasklet {
    private final UserSnapShotService service;
    private final BatchLogger logger;
    /*==========================
     *
     * execute
     *
     * @parm contribution Step 실행 기여도 정보
     * @parm chunkContext Step/Job 실행 컨텍스트
     * @return RepeatStatus Tasklet 실행 완료 여부
     *
     * JobExecutionContext에서 최신 스냅샷 데이터를 조회한 후
     * MongoDB에 사용자별 스냅샷을 저장
     *
     * 저장할 데이터가 없는 경우 별도 처리 없이 Step을 종료
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) {
        logger.stepStart("SaveSnapshotTasklet");

        //시작 시간
        long startTime = System.currentTimeMillis();
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();

        // Job ExecutionContext에서 직접 가져오기
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();

        Map<String, SaveSnapshot> currentSnapshots = (Map<String, SaveSnapshot>) jobContext.get("currentSnapshot");

        //새로 업데이트된 snapshot이 없다면 종료
        if (currentSnapshots==null ||currentSnapshots.isEmpty()){
            logger.stepEnd("SaveSnapshotTasklet","No current snapshots");
            return RepeatStatus.FINISHED;
        }
        for (SaveSnapshot snapshot : currentSnapshots.values()) {
            service.saveSnapshot(snapshot);
        }

        long duration = System.currentTimeMillis() - startTime;
        logger.stepEnd("SaveSnapshotTasklet", "duration=", duration);
        return RepeatStatus.FINISHED;
    }

}
