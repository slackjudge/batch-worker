package store.slackjudge.batch.infra.slack.sender;

public interface SlackSender {
    void sendMessage(String text);
}
