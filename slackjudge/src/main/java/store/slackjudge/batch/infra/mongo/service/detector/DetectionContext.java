package store.slackjudge.batch.infra.mongo.service.detector;

import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;

import java.time.LocalDateTime;

public record DetectionContext <T>(
        T current,
        UserSolvedSnapShotDocument previous,
        LocalDateTime snapshotAt,
        Long userId,
        String bojId
){}
