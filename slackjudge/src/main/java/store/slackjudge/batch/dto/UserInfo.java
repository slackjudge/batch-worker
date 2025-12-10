package store.slackjudge.batch.dto;


public record UserInfo(
        String baekJoonId,
        Long userId,
        Integer baekJoonTier
) {
}
