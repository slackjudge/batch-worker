package store.slackjudge.batch.infra.slack.sender;

import com.slack.api.Slack;
import com.slack.api.model.Attachment;
import com.slack.api.webhook.Payload;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Slf4j
public class SlackWebhookSender implements SlackSender{
    @Value("${slack.webhook.url}")
    private String webhookUrl;
    private final Slack slack=Slack.getInstance();

    //배치 시작 , 종료(성공,실패) 전송
    @Override
    public void sendMessage(Attachment attachment) {
        try {
            slack.send(webhookUrl, Payload.builder().attachments(List.of(attachment)).build());
        }catch (Exception e){
            log.error("[sending slack message] send message failed Error : {}",e.getMessage());
        }
    }
}
