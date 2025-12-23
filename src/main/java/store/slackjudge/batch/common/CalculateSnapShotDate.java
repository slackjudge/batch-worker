package store.slackjudge.batch.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

/**
 * 집계 날짜 계산 util 클래스
 */
@Component
@RequiredArgsConstructor
public class CalculateSnapShotDate {
    private final Clock clock;
    private final ZoneId KST=ZoneId.of("Asia/Seoul");
    private final ZoneId UTC=ZoneId.of("UTC");

    /*==========================
    *
    *CalculateSnapShotDate
    *
    * @return 현재 시간
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    public LocalDateTime now(){
        return LocalDateTime.now(clock);
    }

    /*==========================
    *
    *CalculateSnapShotDate
    *
    * @return 현재 시간:hour
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    public LocalDateTime currentHour(LocalDateTime time){
        return time.truncatedTo(ChronoUnit.HOURS);
    }

    /*==========================
    *
    *CalculateSnapShotDate
    * @return 현재 시간: (hour-1)
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    public LocalDateTime snapshotDate(LocalDateTime time){
        return currentHour(time).minusHours(1);
    }


    /*==========================
    *
    *CalculateSnapShotDate
    * UTC->KST
    * @parm
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 16.
    *
    ==========================**/
    public LocalDateTime returnKst(LocalDateTime utcTime){
        return utcTime.atZone(UTC)
                .withZoneSameInstant(KST)
                .toLocalDateTime();
    }
}
