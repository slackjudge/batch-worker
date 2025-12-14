package store.slackjudge.batch.common;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * 집계 날짜 계산 util 클래스
 */
@Component
@RequiredArgsConstructor
public class CalculateSnapShotDate {
    private final Clock clock;

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

}
