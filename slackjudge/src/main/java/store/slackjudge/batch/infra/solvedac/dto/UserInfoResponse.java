package store.slackjudge.batch.infra.solvedac.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * solved.ac API 유저 정보 파싱용 dto
 */
@Getter
@AllArgsConstructor
public class UserInfoResponse {
    private int solvedCount;    //푼 문제 수
    private String handle;      //백준 아이디
    private int tier;           //백준 티어
    private int rating;         //백준 기준 점수
}
