package store.slackjudge.batch.infra.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.EventBridgeClientBuilder;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;

@Service
@RequiredArgsConstructor
@Profile("!test")
public class EventBridgePublisher {
    private final EventBridgeClient bridgeClientBuilder;
    private final ObjectMapper mapper;

    @Value("${aws.eventbridge.source}")
    private String source;

    @Value("${aws.eventbridge.detail-type}")
    private String detailType;

    public void publishBatchSuccessCompleteEvent(String jobId, String status) {
        try {
            String detail = mapper.writeValueAsString(new BatchEventDetail(jobId, status));
            PutEventsRequestEntry eventsRequestEntry = PutEventsRequestEntry.builder()
                    .source(source)
                    .detailType(detailType)
                    .detail(detail)
                    .build();

            PutEventsRequest request = PutEventsRequest.builder()
                    .entries(eventsRequestEntry)
                    .build();

            bridgeClientBuilder.putEvents(request);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

    }

}
