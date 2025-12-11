package store.slackjudge.batch.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.stream.Collectors;

@Component
@Slf4j
public class BatchLogger {

    /* ====================
        JOB LOGGING
       ==================== */
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



    /* ====================
        STEP LOGGING
       ==================== */
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

    /** Step 전체가 실패 */
    public void stepError(String stepName, String message, Throwable e) {
        log.error(
                "\n[STEP-ERROR] STEP '{}' FAILED\n" +
                "Reason: {}\n",
                stepName, message, e
        );
    }



    /* ====================
       USER / TASKLET LOGGING
       ==================== */
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

    /** Tasklet 내부에서 처리할 때 발생한 경고 */
    public void taskletWarn(String message, Object... args) {
        log.warn("[TASKLET-WARN] " + message, args);
    }

    /** Tasklet 내부에서 처리할 때 발생한 예외(해당 유저만 실패) */
    public void taskletError(String message, Throwable e, Object... args) {
        log.error("[TASKLET-ERROR] " + message, args, e);
    }

    /** 유저 단위 경고 */
    public void userWarn(Long userId, String bojId, String message) {
        log.warn("[WARN][USER:{}][BOJ:{}] {}", userId, bojId, message);
    }

    /** 유저 단위 심각한 오류 */
    public void userError(Long userId, String bojId, String message, Throwable e) {
        log.error("[ERROR][USER:{}][BOJ:{}] {}", userId, bojId, message, e);
    }
}
