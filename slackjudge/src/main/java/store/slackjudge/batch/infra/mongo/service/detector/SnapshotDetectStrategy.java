package store.slackjudge.batch.infra.mongo.service.detector;

import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;

/**
 * strategy pattern 적용
 */
public interface SnapshotDetectStrategy<T> {
    boolean detect(DetectionContext<T> context);
    void update(DetectionContext<T> context);
}
