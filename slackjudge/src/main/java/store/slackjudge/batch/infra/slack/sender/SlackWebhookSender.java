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
    /*==========================
     *
     * sendMessage
     *
     * @parm attachment Slack으로 전송할 Attachment 메시지
     * @return void
     * Slack Webhook URL을 이용해 메시지를 전송
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
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
