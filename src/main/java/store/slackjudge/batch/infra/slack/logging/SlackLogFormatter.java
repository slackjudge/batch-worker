package store.slackjudge.batch.infra.slack.logging;

import com.slack.api.model.Attachment;
import com.slack.api.model.Field;

import java.util.ArrayList;
import java.util.List;

public class SlackLogFormatter {
    /*==========================
     *
     * format
     *
     * @parm messageSpec Slack 전송용 가공된 로그 이벤트 정보
     * @return Attachment Slack Webhook 전송에 사용되는 Attachment 객체
     * 로그 레벨에 따라 색상을 구분, 메시지 및 StackTrace를 필드로 구성
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    //로그 포맷팅 메서드
    public Attachment format(LogEventMessageSpec messageSpec) {
        List<Field> fields = new ArrayList<>();

        fields.add(createField("발생 시각", messageSpec.occurredAt().toString(), true));
        fields.add(createField("레벨", messageSpec.level(), true));
        fields.add(createField("Logger", messageSpec.logger(), false));
        fields.add(createField("메시지", "```" + messageSpec.message() + "```", false));

        if (messageSpec.stackTrace() != null && !messageSpec.stackTrace().isEmpty()) {
            fields.add(createField("Stack Trace", "```" + truncate(messageSpec.stackTrace(), 1500) + "```", false));
        }

        return Attachment.builder()
                .color(messageSpec.isError() ? "#ff0000" : "#ffcc00")  // 여기!
                .fields(fields)
                .footer("SlackJudge Batch System")
                .ts(String.valueOf(System.currentTimeMillis() / 1000))
                .build();
    }

    /*==========================
     *
     * createField
     *
     * @parm title 슬랙 필드 제목
     * @parm value 슬랙 필드 값
     * @parm shortField 한 줄 표시 여부
     * @return Field Slack Attachment에 포함될 Field 객체
     * Slack 메시지의 각 항목을 표준화된 필드 형태로 생성
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    private Field createField(String title, String value, boolean shortField) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(shortField)
                .build();
    }

    /*==========================
     *
     * truncate
     *
     * @parm text 원본 문자열
     * @parm maxLength 최대 허용 길이
     * @return String 최대 길이를 초과할 경우 잘린 문자열
     *
     * @author kimdoyeon
     * @version 1.0.0
     * @date 25. 12. 17.
     *
     ==========================**/
    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...(생략)";
    }
}