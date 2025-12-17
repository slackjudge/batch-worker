package store.slackjudge.batch.infra.solvedac.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;

/**
 * 문제 번호 DTO
 * @param problemId 문제 번호
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemInfoResponse(
        int problemId //백준 문제 번호
)implements Serializable {
}
