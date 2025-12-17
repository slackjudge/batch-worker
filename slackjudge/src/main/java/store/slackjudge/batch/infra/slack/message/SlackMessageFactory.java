package store.slackjudge.batch.infra.slack.message;

import com.slack.api.model.Attachment;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Slack 메시지 포맷팅 - slack 메시지만을 구성
 */
@RequiredArgsConstructor
@Component
public class SlackMessageFactory {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final SlackTextLayout layout;

    @Value("${slack.color.green}")
    private String GREEN;

    @Value("${slack.color.blue}")
    private String BLUE;

    @Value("${slack.color.yellow}")
    private String YELLOW;

    @Value("${slack.color.red}")
    private String RED;

    /*==========================
     *
     * batchStart
     *
     * @parm spec 배치 시작 이벤트 정보 (Job명, 배치 시간, 워커 노드)
     * @return Attachment 배치 시작 알림용 Slack Attachment
     * 배치 시작 시점을 Slack 메시지로 구성하여 반환
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    public Attachment batchStart(BatchStartMessageSpec spec) {
        String text = layout.render(
                layout.title("Slack Judge Batch - START"),
                layout.section("실행정보", List.of(
                        layout.kv("Job Name", spec.jobName()),
                        layout.kv("Batch Time", FORMATTER.format(spec.batchTime())),
                        layout.kv("Worker Node", spec.workerNode())
                )),
                layout.footer("SlackJudge Batch System")
        );
        return Attachment.builder()
                .color(BLUE)
                .text(text)
                .mrkdwnIn(List.of("text"))
                .build();
    }
    /*==========================
     *
     * batchEnd
     *
     * @parm spec 배치 종료 이벤트 정보 (상태, 처리 결과, 소요 시간)
     * @return Attachment 배치 종료 알림용 Slack Attachment
     * 배치 성공/실패 여부에 따라 색상을 구분하여 결과 메시지를 생성
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    public Attachment batchEnd(BatchEndMessageSpec spec) {
        String text = layout.render(
                layout.title("SlackJudge Batch - " + spec.status()),
                layout.section("실행 정보", List.of(
                        layout.kv("발생 시간", FORMATTER.format(spec.time())),
                        layout.kv("소요 시간", spec.duration() + "ms")
                )),
                layout.section("처리 결과", List.of(
                        layout.kv("All Users", String.valueOf(spec.totalUsers())),
                        layout.kv("New Users", String.valueOf(spec.newUsers())),
                        layout.kv("Updated Users", String.valueOf(spec.updatedUsers())),
                        layout.kv("Failed Users", String.valueOf(spec.failedUsers()))
                )),
                layout.footer("SlackJudge Batch System")
        );

        String color = switch (spec.status()) {
            case "FAILED" -> RED;
            case "SUCCESS" -> spec.failedUsers() > 0 ? YELLOW : GREEN;
            default -> BLUE;
        };

        return Attachment.builder()
                .color(color)
                .text(text)
                .mrkdwnIn(List.of("text"))
                .build();
    }

}
