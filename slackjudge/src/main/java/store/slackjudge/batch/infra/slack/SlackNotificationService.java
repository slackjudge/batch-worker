package store.slackjudge.batch.infra.slack;

import com.slack.api.model.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import store.slackjudge.batch.infra.slack.message.BatchEndMessageSpec;
import store.slackjudge.batch.infra.slack.message.BatchStartMessageSpec;
import store.slackjudge.batch.infra.slack.message.SlackMessageFactory;
import store.slackjudge.batch.infra.slack.sender.SlackSender;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SlackNotificationService {
    private final SlackSender sender;
    private final SlackMessageFactory messageFactory;

    public void notifyBatchStart(String jobName, LocalDateTime batchTime,String workerNode){
        Attachment message=messageFactory.batchStart(new BatchStartMessageSpec(
                jobName, batchTime, workerNode)
        );

        sender.sendMessage(message);
    }

    public void notifyBatchSuccess(long duration,int totalUsers,int newUsers,int updated,int failed,LocalDateTime time){
        Attachment message=messageFactory.batchEnd(new BatchEndMessageSpec(
                "SUCCESS",duration,totalUsers,newUsers,updated,failed,time,""
        ));

        sender.sendMessage(message);
    }

    public void notifyBatchFailed(LocalDateTime occurredAt,String reason){
        Attachment message = messageFactory.batchEnd(
            new BatchEndMessageSpec(
                    "FAILED",
                    0,
                    0,
                    0,
                    0,
                    0,
                    occurredAt,
                    reason
            )
    );
        sender.sendMessage(message);
    }

}
