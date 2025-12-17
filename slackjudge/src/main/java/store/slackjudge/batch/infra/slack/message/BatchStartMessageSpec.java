package store.slackjudge.batch.infra.slack.message;

import java.time.LocalDateTime;

/**
 * 배치 시작 시 전송 메시지 DTO
 * @param jobName Batch Job 이름
 * @param batchTime 배치 시간
 * @param workerNode 구분자
 */
public record BatchStartMessageSpec(
        String jobName,
        LocalDateTime batchTime,
        String workerNode
) {
}
