package store.slackjudge.batch.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.messaging.Task;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.common.CalculateSnapShotDate;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.config.TimeConfig;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.repository.UserSolvedSnapShotRepository;
import store.slackjudge.batch.infra.mongo.service.UserSnapShotService;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [ step 02 ]
 * Mongo 로부터 모든 유저 snapshot를 조회하는 tasklet
 */

@Component

public class LoadSnapshotTasklet implements Tasklet {
    /**
     * next step : FetchTierAndCountTasklet
     */
    private final UserSolvedSnapShotRepository repository;
    private final CalculateSnapShotDate snapShotDate;
    private final BatchLogger logger;
    private final LocalDateTime batchTime;

    public LoadSnapshotTasklet(
            UserSolvedSnapShotRepository repository,
            CalculateSnapShotDate snapShotDate,
            BatchLogger logger,
            @Value("#{jobParameters['batchTime']}") LocalDateTime batchTime)
    {
        this.repository = repository;
        this.snapShotDate = snapShotDate;
        this.logger = logger;
        this.batchTime = batchTime;
    }


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.stepStart("LoadSnapshotTasklet");

        //시작 시간
        long startTime = System.currentTimeMillis();

        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();

        // Job ExecutionContext에서 직접 가져오기
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();
        List<UserInfo> users = (List<UserInfo>) jobContext.get("users");

        if (users == null || users.isEmpty()) {
            logger.stepEnd("LoadSnapshotTasklet", "No users found");
            return RepeatStatus.FINISHED;
        }

        //스냅샷 기준 조회
        LocalDateTime snapshotAt = snapShotDate.snapshotDate(batchTime);

        //모든 유저의 bojIds 리스트
        List<String> usersBojIds = users.stream()
                .map(UserInfo::baekJoonId)
                .toList();
        Map<String, UserSolvedSnapShotDocument> mappingUsersSolvedSnapshots = new HashMap<>();

        //모든 유저의 스냅샷 조회
        usersBojIds.forEach(bojId -> {
            UserSolvedSnapShotDocument doc = repository.findByIdBojIdAndIdSnapShotAt(bojId, snapshotAt)
                    .orElse(null); //1시간 전 스냅샷 없는 경우 null값 삽입 -> step3에서 null-safe 처리
            mappingUsersSolvedSnapshots.put(bojId, doc);
        });

        //종료 시간 - 시작 시간
        long duration = System.currentTimeMillis() - startTime;

        //현재 step에 대한 메타데이터 객체

        //{ snapshot : List<UserSolvedSnapShotDocument> } 데이터 저장
        ExecutionContext stepContext = stepExecution.getExecutionContext();
        stepContext.put("snapshots", mappingUsersSolvedSnapshots);

        logger.stepEnd("LoadSnapshotTasklet", "usersSnapshotSize=", mappingUsersSolvedSnapshots.size(), "duration=", duration);

        return RepeatStatus.FINISHED;
    }
}
