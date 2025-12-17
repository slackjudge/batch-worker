package store.slackjudge.batch.infra.solvedac.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;

/**
 * solved.ac API 문제 정보 조회 DTO
 * @param count
 * @param items
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemSearchResponse(
    int count,
    List<ProblemInfoResponse> items
) implements Serializable {
}
