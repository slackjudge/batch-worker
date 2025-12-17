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
    /*==========================
     *
     * notifyBatchStart
     *
     * @parm jobName 배치 Job 이름
     * @parm batchTime 배치 실행 기준 시간
     * @parm workerNode 배치를 실행한 워커 노드 정보
     * @return void
     * 배치 시작 시 Slack 알림을 전송
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    public void notifyBatchStart(String jobName, LocalDateTime batchTime,String workerNode){
        Attachment message=messageFactory.batchStart(new BatchStartMessageSpec(
                jobName, batchTime, workerNode)
        );

        sender.sendMessage(message);
    }

    /*==========================
     *
     * notifyBatchSuccess
     *
     * @parm duration 배치 수행 시간(ms)
     * @parm totalUsers 전체 처리 대상 사용자 수
     * @parm newUsers 신규 사용자 수
     * @parm updated 업데이트된 사용자 수
     * @parm failed 실패한 사용자 수
     * @parm time 배치 종료 시각
     * @return void
     * 배치 성공 시 처리 결과를 Slack으로 전송
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    public void notifyBatchSuccess(long duration,int totalUsers,int newUsers,int updated,int failed,LocalDateTime time){
        Attachment message=messageFactory.batchEnd(new BatchEndMessageSpec(
                "SUCCESS",duration,totalUsers,newUsers,updated,failed,time,""
        ));

        sender.sendMessage(message);
    }
    /*==========================
     *
     * notifyBatchFailed
     *
     * @parm occurredAt 배치 실패 발생 시각
     * @parm reason 배치 실패 사유
     * @return void
     * 배치 실패 시 원인 정보를 포함한 Slack 알림을 전송
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
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
