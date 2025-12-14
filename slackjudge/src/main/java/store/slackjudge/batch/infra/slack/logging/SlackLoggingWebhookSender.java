package store.slackjudge.batch.infra.slack.logging;

import com.slack.api.model.Attachment;
import lombok.extern.slf4j.Slf4j;
import store.slackjudge.batch.infra.slack.sender.SlackSender;

@Slf4j
public class SlackLoggingWebhookSender implements SlackSender {

    private String webhookUrl;
    private SlackLogFormatter formatter;

    @Override
    public void sendMessage(Attachment attachment) {

    }

    @Override
    public void sendLog(String text) {

    }

    // LogAppender에서 직접 호출할 메서드
    public void send(LogEventMessageSpec messageSpec) {
        if (webhookUrl == null || webhookUrl.isEmpty()) {
            return;
        }

        try {
            if (formatter == null) {
                formatter = new SlackLogFormatter();
            }

            Attachment attachment = formatter.format(messageSpec);

            sendMessageInternal(messageSpec.isError() ? "🚨" : "⚠️", messageSpec.level(), attachment);

        } catch (Exception e) {
            log.error("Failed to send log to Slack", e);
        }
    }

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

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }
}