package store.slackjudge.batch.infra.slack.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.IThrowableProxy;
import ch.qos.logback.classic.spi.StackTraceElementProxy;
import ch.qos.logback.core.AppenderBase;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class SlackLogAppender extends AppenderBase<ILoggingEvent> {

    private SlackLoggingWebhookSender slackSender;  // 구체 타입으로 변경

    @Override
    protected void append(ILoggingEvent event) {
        if (slackSender == null) {
            return;
        }

        try {
            String level = event.getLevel().toString();

            if (!"WARN".equals(level) && !"ERROR".equals(level)) {
                return;
            }

            LocalDateTime timestamp = LocalDateTime.ofInstant(
                    Instant.ofEpochMilli(event.getTimeStamp()),
                    ZoneId.systemDefault()
            );

            String stackTrace = null;
            IThrowableProxy throwable = event.getThrowableProxy();
            if (throwable != null) {
                stackTrace = formatStackTrace(throwable);
            }

            //로그 메세지 객체 생성
            LogEventMessageSpec messageSpec = new LogEventMessageSpec(
                    level,
                    timestamp,
                    event.getLoggerName(),
                    event.getFormattedMessage(),
                    stackTrace
            );

            //slack 알림 전송
            slackSender.send(messageSpec);

        } catch (Exception e) {
            addError("Failed to send log to Slack", e);
        }
    }

    //stackTrece 초기화 메서드
    private String formatStackTrace(IThrowableProxy throwable) {
        StringBuilder sb = new StringBuilder();

        sb.append(throwable.getClassName());
        if (throwable.getMessage() != null) {
            sb.append(": ").append(throwable.getMessage());
        }
        sb.append("\n");

        StackTraceElementProxy[] trace = throwable.getStackTraceElementProxyArray();
        int maxLines = Math.min(10, trace.length);

        for (int i = 0; i < maxLines; i++) {
            sb.append("  at ").append(trace[i].toString()).append("\n");
        }

        if (trace.length > maxLines) {
            sb.append("  ... ").append(trace.length - maxLines).append(" more");
        }

        return sb.toString();
    }

    //log-back 파일에서 매핑됨.
    public void setSlackSender(SlackLoggingWebhookSender slackSender) {
        this.slackSender = slackSender;
    }
}