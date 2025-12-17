package store.slackjudge.batch.service;

import lombok.Builder;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;

import java.time.LocalDateTime;

/**
 * @parm <T> 현재 데이터 타입
 * @parm previous 이전 스냅샷 도큐먼트
 * @parm snapshotAt 스냅샷 기준 시각
 * @parm userId 사용자 ID
 * @parm bojId 백준 사용자 ID
 */
@Builder
public record DetectionContext <T>(
        T current,
        UserSolvedSnapShotDocument previous,
        LocalDateTime snapshotAt,
        Long userId,
        String bojId
){
    public static <T> DetectionContextBuilder<T> builder(){
        return new DetectionContextBuilder<T>();
    }
}
