package store.slackjudge.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.common.CalculateSnapShotDate;
import store.slackjudge.batch.infra.slack.SlackNotificationService;

import java.time.Duration;
import java.time.LocalDateTime;


@Component
@RequiredArgsConstructor
public class BatchJobListener {
    @Value("${spring.batch.name}")
    private String jobName;

    private final BatchLogger logger;
    private final SlackNotificationService notificationService;
    private final CalculateSnapShotDate calculateSnapShotDate;

    /*==========================
    *
    * BatchJobListener
    * BatchJob 실행 이전 수행 작업 정의
    * @parm jobExecution : Job 실행 중에 발생 정보 저장 객체
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        logger.jobStart(jobExecution.getJobInstance().getJobName());

        LocalDateTime batchTime = jobExecution.getJobParameters()
                .getLocalDateTime("batchTime", LocalDateTime.now());

        //배치 시작 slack 알림 추가
        notificationService.notifyBatchStart(
                jobExecution.getJobInstance().getJobName(),
                batchTime,
                jobName
        );
    }

    /*==========================
    *
    * BatchJobListener
    * BatchJob 실행 이후 수행 작업 정의
    * @parm jobExecution : Job 실행 중에 발생 정보 저장 객체
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @AfterJob
    public void afterJob(JobExecution jobExecution) {
        ExecutionContext ctx = jobExecution.getExecutionContext();

        int total = ctx.getInt("TOTAL_USERS", 0);
        int updated = ctx.getInt("UPDATED_USERS", 0);
        int newUser = ctx.getInt("NEW_USERS", 0);
        int failedUser = ctx.getInt("FAILED_USERS", 0);


        LocalDateTime start = jobExecution.getStartTime();
        LocalDateTime end = jobExecution.getEndTime();
        long durationMs = 0L;

        if (start != null && end != null) {
            durationMs = Duration.between(start, end).toMillis();
        }

        logger.jobEnd(
                jobExecution.getJobInstance().getJobName(),
                durationMs,
                total,
                updated + newUser,
                failedUser
        );
        LocalDateTime occurredTime=calculateSnapShotDate.now();

        //배치 종료 slack 알림 추가
        if (jobExecution.getStatus().isUnsuccessful()) {
            //fail 예외 객체 없으면 기본 값 => Batch failed 출력
            String reason = jobExecution.getAllFailureExceptions().isEmpty()
                    ? "Batch failed"
                    : String.valueOf(jobExecution.getAllFailureExceptions().get(0).getMessage());

            //배치 실패 slack 알림 전송
            notificationService.notifyBatchFailed(occurredTime, reason);
        } else {
            //배치 성공 slack 알림 전송
            notificationService.notifyBatchSuccess(durationMs, total, newUser, updated, failedUser,occurredTime);
        }
    }
}
