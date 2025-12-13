package store.slackjudge.batch.infra.slack.message;

import java.time.LocalDateTime;

public record BatchStartMessageSpec(
        String jobName,
        LocalDateTime batchTime,
        String workerNode
) {
}
