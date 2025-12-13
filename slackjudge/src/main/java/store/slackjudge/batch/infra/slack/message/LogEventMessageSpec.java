package store.slackjudge.batch.infra.slack.message;

import java.time.LocalDateTime;

public record LogEventMessageSpec(
        String level,
        LocalDateTime occurredAt,
        String logger,
        String message,
        String stackTrace
) {
}
