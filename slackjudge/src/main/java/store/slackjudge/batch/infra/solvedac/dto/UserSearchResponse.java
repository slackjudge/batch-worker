package store.slackjudge.batch.infra.solvedac.dto;

import lombok.Getter;

import java.util.List;


public record UserSearchResponse(
        int count,
        List<UserInfoResponse> items
) {
}
