package store.slackjudge.batch.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
@ReadingConverter
public class DateToLocalDateTimeKstConverter implements Converter<Date, LocalDateTime> {
    @Override
    public LocalDateTime convert(Date source) {
        //UTC->KST
        return source.toInstant()
                .atZone(ZoneId.of("UTC"))           // UTC로 명시
                .withZoneSameInstant(ZoneId.of("Asia/Seoul"))  // KST로 변환
                .toLocalDateTime();
    }
}
