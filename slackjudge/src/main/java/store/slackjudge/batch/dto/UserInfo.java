package store.slackjudge.batch.dto;


import java.io.Serializable;

public record UserInfo(
        String baekJoonId,
        Long userId,
        Integer baekJoonTier
) implements Serializable {
}
