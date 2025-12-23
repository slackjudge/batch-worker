package store.slackjudge.batch.infra.slack.message;

import java.time.LocalDateTime;

/**
 * 배치 종료 메시지 DTO
 * @param status 상태
 * @param duration 배치 종료 시간 - 시작 시간
 * @param totalUsers 현재 총 유저 수
 * @param newUsers 새로 가입한 유저의 배치 처리 수
 * @param updatedUsers 변경이 감지된 유저의 배치 처리 수
 * @param failedUsers 실패한 유저의 배치 처리 수
 * @param time 현재 시간
 * @param reason 배치 실패 이유 (성공적으로 종료시는 반 겂)
 */
public record BatchEndMessageSpec(
    String status, //SUCCESS, FAILED
    long duration,
    int totalUsers,
    int newUsers,
    int updatedUsers,
    int failedUsers,
    LocalDateTime time,
    String reason
) {
}
