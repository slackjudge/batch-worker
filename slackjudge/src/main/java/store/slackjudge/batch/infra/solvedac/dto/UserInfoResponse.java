package store.slackjudge.batch.infra.solvedac.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * solved.ac API 유저 정보 파싱용 dto
 */

public record UserInfoResponse(
        int solvedCount,    //푼 문제 수
        String handle,      //백준 아이디
        int tier,           //백준 티어
        int rating          //백준 기준 점수
) {
}
