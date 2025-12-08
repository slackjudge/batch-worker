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
    private String handle;
    private String bio;
    private boolean verified;
    private String badgeId;
    private String backgroundId;
    private String profileImageUrl;
    private int solvedCount;
    private int voteCount;

    @JsonProperty("class")
    private int clazz; // class는 예약어 이름 변경

    private String classDecoration;
    private int rivalCount;
    private int reverseRivalCount;
    private int tier;
    private int rating;
    private int ratingByProblemsSum;
    private int ratingByClass;
    private int ratingBySolvedCount;
    private int ratingByVoteCount;
    private int overRating;
    private int overRatingCutoff;
    private int arenaTier;
    private int arenaRating;
    private int arenaMaxTier;
    private int arenaMaxRating;
    private int arenaCompetedRoundCount;
    private int maxStreak;
    private int coins;
    private int stardusts;
    private String joinedAt;
    private String bannedUntil;
    private String proUntil;
    private int rank;
}
