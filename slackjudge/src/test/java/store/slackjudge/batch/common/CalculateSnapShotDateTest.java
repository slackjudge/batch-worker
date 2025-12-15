package store.slackjudge.batch.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class CalculateSnapShotDateTest {
    private final LocalDateTime fixedTime=LocalDateTime.of(2025,12,12,12,12,12,12);;
    private final Clock fixedClock=Clock.fixed(fixedTime.toInstant(ZoneOffset.UTC),ZoneOffset.UTC);

    @Test
    @DisplayName("clock 추상 객체를 이용해 현재 시간 LocalDateTime 생성")
    void createNow(){
        //given
        CalculateSnapShotDate calculator=new CalculateSnapShotDate(fixedClock);

        //when
        LocalDateTime result=calculator.now();

        //then
        assertThat(result).isEqualTo(fixedTime);
    }

    @Test
    @DisplayName("clock 추상 객체 이용하여 현재 시간 기준 hour 아래값 절삭")
    void currentHour(){
        //given
        CalculateSnapShotDate calculator=new CalculateSnapShotDate(fixedClock);

        //when
        LocalDateTime result=calculator.currentHour(fixedTime);

        //then
        assertThat(result).isEqualTo(LocalDateTime.of(2025,12,12,12,0,0,0));
    }

    @Test
    @DisplayName("clock 추상 객체 이용하여 현재 시간 기준 hour 아래값 절삭 후 -1 hour")
    void snapshotDate(){
        //when
        CalculateSnapShotDate calculator=new CalculateSnapShotDate(fixedClock);

        //when
        LocalDateTime result=calculator.snapshotDate(fixedTime);

        //then
        assertThat(result).isEqualTo(LocalDateTime.of(2025,12,12,11,0,0,0));

    }
}