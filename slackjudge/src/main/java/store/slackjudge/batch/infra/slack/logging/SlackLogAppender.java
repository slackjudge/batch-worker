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

    /*==========================
    *
    *SlackLogAppender
    *
    * @parm event 로그백에서 전달된 로깅 이벤트
    * @return void
    * WARN 또는 ERROR 로그 발생 시 StackTrace를 가공 후 슬랙으로 전송
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
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
            LogEventMessageSpec messageSpec = LogEventMessageSpec.from(event);

            //slack 알림 전송
            slackSender.send(messageSpec);

        } catch (Exception e) {
            addError("Failed to send log to Slack", e);
        }
    }

    /*==========================
    *
    * formatStackTrace
    *
    * @parm throwable 로그 이벤트에 포함된 예외 정보
    * @return String 예외 클래스, 메시지 및 StackTrace 상위 최대 10줄을 포맷
    * 예외 발생 원인을 슬랙 전송용 문자열 형태로 가공
    *
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
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