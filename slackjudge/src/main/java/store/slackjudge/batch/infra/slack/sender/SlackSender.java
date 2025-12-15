package store.slackjudge.batch.infra.slack.sender;

import com.slack.api.model.Attachment;

public interface SlackSender {
    void sendMessage(Attachment attachment);
}
