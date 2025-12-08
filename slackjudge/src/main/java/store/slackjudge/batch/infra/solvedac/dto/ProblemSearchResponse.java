package store.slackjudge.batch.infra.solvedac.dto;

import java.util.List;

public record ProblemSearchResponse(
    int count,
    List<ProblemInfoResponse> items
) {
}
