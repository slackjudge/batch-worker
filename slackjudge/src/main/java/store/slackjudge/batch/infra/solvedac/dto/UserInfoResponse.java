package store.slackjudge.batch.infra.solvedac.dto;

public record UserInfoResponse(
        //푼 문제 수
        Integer problemCount,
        //백준 Id
        String bojId,
        //백준 티어
        Integer tier,
        //백준 레이팅
        Integer rating
) {
}
