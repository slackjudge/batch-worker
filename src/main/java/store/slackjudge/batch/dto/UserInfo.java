package store.slackjudge.batch.dto;


import java.io.Serializable;

/**
 * 유저 정보 DTO
 * @param baekJoonId 백준 아이디
 * @param userId 유저 PK
 * @param baekJoonTier 백준 티어
 */
public record UserInfo(
        String baekJoonId,
        Long userId,
        Integer baekJoonTier
) implements Serializable {
}
