package store.slackjudge.batch.infra.slack.logging;

import com.slack.api.model.Attachment;
import lombok.extern.slf4j.Slf4j;
import store.slackjudge.batch.infra.slack.sender.SlackSender;

@Slf4j
public class SlackLoggingWebhookSender implements SlackSender {

    private String webhookUrl;
    private final SlackLogFormatter formatter = new SlackLogFormatter();
    /*==========================
     *
     * sendMessage
     *
     * @parm attachment Slack으로 전송할 Attachment 객체
     * @return void
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    @Override
    public void sendMessage(Attachment attachment) {

    }
    /*==========================
     *
     * send
     *
     * @parm messageSpec Slack 전송용 로그 이벤트 정보
     * @return void
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     ==========================**/
    public void send(LogEventMessageSpec messageSpec) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        try {
            Attachment attachment = formatter.format(messageSpec);

            sendMessageInternal(messageSpec.isError() ? "🚨" : "⚠️", messageSpec.level(), attachment);

        } catch (Exception e) {
            log.error("Failed to send log to Slack", e);
        }
    }

    /*==========================
     *
     * sendMessageInternal
     *
     * @parm emoji 로그 심각도 표시용 이모지
     * @parm level 로그 레벨
     * @parm attachment Slack으로 전송할 Attachment
     * @return void
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    private void sendMessageInternal(String emoji, String level, Attachment attachment) {
        try {
            com.slack.api.Slack slack = com.slack.api.Slack.getInstance();
            com.slack.api.webhook.Payload payload = com.slack.api.webhook.Payload.builder()
                    .text(emoji + " *배치 " + level + " 발생*")
                    .attachments(java.util.List.of(attachment))
                    .build();

            slack.send(webhookUrl, payload);
        } catch (Exception e) {
            log.error("Failed to send to Slack webhook", e);
        }
    }

    /*==========================
     *
     * setWebhookUrl
     *
     * @parm webhookUrl Slack Incoming Webhook URL
     * @return void
     * logback 설정 파일에서 주입되며 슬랙 전송 대상 Webhook을 설정
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}