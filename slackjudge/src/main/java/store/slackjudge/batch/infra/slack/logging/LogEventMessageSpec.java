package store.slackjudge.batch.infra.slack.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public record LogEventMessageSpec(
        String level,
        LocalDateTime occurredAt,
        String logger,
        String message,
        String stackTrace
) {
    public static LogEventMessageSpec from(ILoggingEvent event) {
        return new LogEventMessageSpec(
                event.getLevel().toString(),
                LocalDateTime.ofInstant(
                        Instant.ofEpochMilli(event.getTimeStamp()),
                        ZoneId.systemDefault()
                ),
                event.getLoggerName(),
                event.getFormattedMessage(),
                event.getThrowableProxy() == null ? null :
                        event.getThrowableProxy().getClassName() + ": " +
                        event.getThrowableProxy().getMessage()
        );
    }

    public boolean isError() {
        return "ERROR".equals(level);
    }

    public boolean isWarn() {
        return "WARN".equals(level);
    }
}