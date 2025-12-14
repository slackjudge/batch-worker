package store.slackjudge.batch.infra.slack.logging;

import com.slack.api.model.Attachment;
import com.slack.api.model.Field;

import java.util.ArrayList;
import java.util.List;

public class SlackLogFormatter {

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

    private Field createField(String title, String value, boolean shortField) {
        return Field.builder()
                .title(title)
                .value(value)
                .valueShortEnough(shortField)
                .build();
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength) + "...(생략)";
    }
}