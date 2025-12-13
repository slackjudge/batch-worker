package store.slackjudge.batch.infra.slack.message;

import java.time.LocalDateTime;

public record BatchEndMessageSpec(
    String status, //SUCCESS, FAILED
    long duration,
    int totalUsers,
    int newUsers,
    int updatedUsers,
    int failedUsers,
    LocalDateTime time
) {
}
