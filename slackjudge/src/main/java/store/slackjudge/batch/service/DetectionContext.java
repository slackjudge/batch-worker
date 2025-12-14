package store.slackjudge.batch.service;

import lombok.Builder;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;

import java.time.LocalDateTime;
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
