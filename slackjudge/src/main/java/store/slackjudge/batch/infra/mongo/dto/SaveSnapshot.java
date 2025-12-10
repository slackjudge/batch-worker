package store.slackjudge.batch.infra.mongo.dto;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Set;

/**
 * Document 저장용 DTO
 */
public record SaveSnapshot(
    String bojId,
    LocalDateTime snapShotAt,
    Set<Integer> solvedProblemIds,
    Integer solvedCount,
    Integer tier,
    Long userId
) {
}
