package store.slackjudge.batch.infra.aws;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

@Service
@RequiredArgsConstructor
public class EventBridgePublisher {
    private final EventBridgeClient bridgeClientBuilder =
            EventBridgeClient.builder().build();

    public void publishBatchSuccessCompleteEvent(String jobId,String status){
        PutEventsRequestEntry eventsRequestEntry=PutEventsRequestEntry.builder()
                .source("com.slackJudge.batch")
                .detailType("Batch Finished")
                .detail("{\"jobId\":\""+jobId+"\",\"status\":\""+status+"\"}")
                .build();

        PutEventsRequest request=PutEventsRequest.builder()
                .entries(eventsRequestEntry)
                .build();

        bridgeClientBuilder.putEvents(request);
    }

}
