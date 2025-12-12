package store.slackjudge.batch.job.tasklet;

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
import store.slackjudge.batch.infra.mongo.dto.SaveSnapshot;
import store.slackjudge.batch.infra.mongo.service.UserSnapShotService;

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

    private StepExecution stepExecution;
    private Map<String, SaveSnapshot> currentSnapshots;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext){
        logger.stepStart("SaveSnapshotTasklet");

        //시작 시간
        long startTime=System.currentTimeMillis();

        for (SaveSnapshot snapshot:currentSnapshots.values()){
            service.saveSnapshot(snapshot);
        }

        long duration=System.currentTimeMillis()-startTime;
        logger.stepEnd("SaveSnapshotTasklet","duration=",duration);
        return RepeatStatus.FINISHED;
    }

    @BeforeStep
    public void retrievePreviousStepData(StepExecution stepExecution){
        this.stepExecution=stepExecution;

        final JobExecution jobExecution=stepExecution.getJobExecution();
        final ExecutionContext jobContext=jobExecution.getExecutionContext();

        this.currentSnapshots=(Map<String, SaveSnapshot>) jobContext.get("currentSnapshot");
    }
}
