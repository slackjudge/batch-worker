package store.slackjudge.batch.infra.slack.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

/**
 * Batch log event DTO
 * @param level 로그 레벨
 * @param occurredAt 로그 발생 시각
 * @param logger 로그 객체
 * @param message 로그 메시지
 * @param stackTrace 로그가 호출 메서드 메타 데이터
 */
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