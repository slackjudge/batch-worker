package store.slackjudge.batch.infra.mongo.dto;

import lombok.Getter;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Set;

/**
 * Document 저장용 DTO
 * @param bojId 백준아이디
 * @param snapShotAt 스냅셧 기준일
 * @param solvedProblemIds 유저가 푼 문제 번호 리스트
 * @param solvedCount 유저가 푼 문제 수
 * @param tier 백준 티어
 * @param userId 유저 아이디 PK
 * @param rating 유저 점수(solved.ac 기준)
 */
public record SaveSnapshot(
    String bojId,
    LocalDateTime snapShotAt,
    Set<Integer> solvedProblemIds,
    Integer solvedCount,
    Integer tier,
    Long userId,
    Integer rating
) implements Serializable {
}
