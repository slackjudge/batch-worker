package store.slackjudge.batch.infra.solvedac.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * solved.ac API 유저 정보 파싱용 DTO
 * @param solvedCount 푼 문제 수
 * @param handle 백준 아이디
 * @param tier 백준 티어
 * @param rating 백준 점수
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record UserInfoResponse(
        int solvedCount,    //푼 문제 수
        String handle,      //백준 아이디
        int tier,           //백준 티어
        int rating          //백준 기준 점수
) implements Serializable {
}
