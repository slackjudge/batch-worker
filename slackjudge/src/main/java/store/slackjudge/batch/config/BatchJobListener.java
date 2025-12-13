package store.slackjudge.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.annotation.AfterJob;
import org.springframework.batch.core.annotation.BeforeJob;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class BatchJobListener {
    private final BatchLogger logger;

    @BeforeJob
    public void beforeJob(JobExecution jobExecution) {
        logger.jobStart(jobExecution.getJobInstance().getJobName());
    }

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
    }
}
