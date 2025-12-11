package store.slackjudge.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BatchLogger {

    public void jobStart(String jobName) {
        log.info(
                "\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓" +
                "\n┃   {} STARTED" +
                "\n┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫" +
                "\n┃ Job Name    : {}" +
                "\n┃ Started At  : {}" +
                "\n┃ Worker Node : SlackJudge Batch Worker #1" +
                "\n┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛",
                jobName, jobName, LocalDateTime.now()
        );
    }

    public void jobEnd(String jobName, long duration, int total, int changed, int failed) {
        log.info(
                "\n┏━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┓" +
                "\n┃               BATCH SUMMARY           ┃" +
                "\n┣━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┫" +
                "\n┃ Total Users      : {}" +
                "\n┃ Changed Users    : {}" +
                "\n┃ Failed Users     : {}" +
                "\n┃ Total Duration   : {} ms" +
                "\n┗━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━┛" +
                "\n{} COMPLETED",
                total, changed, failed, duration, jobName
        );
    }

    public void stepStart(String step) {
        log.info("\n──────────────────────────────────────────────\n▶ STEP: {} START", step);
    }

    public void stepEnd(String step, Object... details) {
        log.info("✓ STEP: {} DONE | {}\n──────────────────────────────────────────────",
                step,
                Arrays.stream(details)
                        .map(Object::toString)
                        .collect(Collectors.joining(", "))
        );
    }

    public void userBlock(Long userId, String bojId, String body) {
        log.info(
                "\n┌─────────────────────────────────────────────┐" +
                "\n│ USER: {}    BOJ: {}" +
                "\n├─────────────────────────────────────────────┤" +
                "\n{}" +
                "\n└─────────────────────────────────────────────┘",
                userId, bojId, body
        );
    }

    public void warn(String format, Object... args) {
        log.warn(format, args);
    }

    public void error(String format, Object... args) {
        log.error(format, args);
    }
}

