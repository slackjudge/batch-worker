package store.slackjudge.batch.infra.aws;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequest;
import software.amazon.awssdk.services.eventbridge.model.PutEventsRequestEntry;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResponse;
import software.amazon.awssdk.services.eventbridge.model.PutEventsResultEntry;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventBridgePublisher {
    private final EventBridgeClient eventBridgeClient;
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

            PutEventsResponse response = eventBridgeClient.putEvents(request);

            if (response.failedEntryCount() > 0) {
                for (PutEventsResultEntry entry : response.entries()) {
                    if (entry.errorCode() != null) {
                        log.error("EventBridge 전송 실패 - JobId: {}, ErrorCode: {}, Message: {}",
                                jobId, entry.errorCode(), entry.errorMessage());
                    }
                }
            } else {
                log.info("EventBridge 이벤트 전송 성공 - JobId: {}, Status: {}", jobId, status);
            }
        } catch (JsonProcessingException e) {
            log.error("EventBridge 이벤트 직렬화 실패 - JobId: {}", jobId, e);
            throw new RuntimeException("EventBridge 이벤트 직렬화 실패", e);
        }
    }

}
