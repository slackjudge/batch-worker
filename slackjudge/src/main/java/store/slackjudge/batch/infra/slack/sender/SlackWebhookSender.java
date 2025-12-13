package store.slackjudge.batch.infra.slack.sender;

import com.slack.api.Slack;
import com.slack.api.webhook.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class SlackWebhookSender implements SlackSender{
    @Value("${slack.webhook.url}")
    private String webhookUrl;

    private final Slack slack=Slack.getInstance();

    @Override
    public void sendMessage(String text) {
        try {
            slack.send(webhookUrl, Payload.builder().text(text).build());
        }catch (Exception e){
            log.error("[sending slack message] send message failed Error : {}",e.getMessage());
        }
    }
}
