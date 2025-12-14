package store.slackjudge.batch.infra.solvedac.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.io.Serializable;
import java.util.List;
@JsonIgnoreProperties(ignoreUnknown = true)
public record ProblemSearchResponse(
    int count,
    List<ProblemInfoResponse> items
) implements Serializable {
}
