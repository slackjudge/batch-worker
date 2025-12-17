package store.slackjudge.batch.config.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

/**
 * UTC -> KST 변환 컨버터
 */
@Component
@WritingConverter
public class LocalDateTimeToDateKstConverter implements Converter<LocalDateTime, Date> {
    /*==========================
    *
    *LocalDateTimeToDateKstConverter
    * UTC -> KST로 변환합니다.
    * @parm source:UTC 기준 시간대
    * @return KST 변환된 시간
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @Override
    public Date convert(LocalDateTime source) {
        //KST->UTC
        return Date.from(
            source.atZone(ZoneId.of("Asia/Seoul"))  // KST로 명시
                  .withZoneSameInstant(ZoneId.of("UTC"))  // UTC로 변환
                  .toInstant()
        );
    }
}
