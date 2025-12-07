package store.slackjudge.batch.dto;

import org.antlr.v4.runtime.misc.NotNull;

public record UserInfo(
        String baekJoonId,
        Long userId,
        Integer baekJoonTier
) {
}
