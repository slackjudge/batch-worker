package store.slackjudge.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.common.CalculateSnapShotDate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
@Profile("!test")
public class SlackJudgeBatchRunner implements CommandLineRunner {
    private final JobLauncher jobLauncher;
    private final Job slackJudgeBatch;
    private final CalculateSnapShotDate calculateSnapShotDate;

    @Override
    public void run(String... args) throws Exception {

        LocalDateTime batchTime = parseBatchTime(args);
        log.info("Slack Judge Batch Container Start  now time = {}", batchTime);

        JobParameters jobParameters = new JobParametersBuilder()
                .addLocalDateTime("batchTime", batchTime)
                .toJobParameters();

        jobLauncher.run(slackJudgeBatch, jobParameters);

        log.info("Slack Judge Batch Complete");
        System.exit(0);

    }

    private LocalDateTime parseBatchTime(String[] args) {
        if (args.length > 0) {
            try {
                return calculateSnapShotDate.returnKst(LocalDateTime.parse(args[0], DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } catch (Exception e) {
                log.warn("Invalid batchTime format: {}, using current time", args[0]);
            }
        }
        return calculateSnapShotDate.returnKst(LocalDateTime.now());
    }
}
