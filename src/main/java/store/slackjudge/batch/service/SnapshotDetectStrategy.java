package store.slackjudge.batch.service;

/**
 * strategy pattern 적용
 */
public interface SnapshotDetectStrategy<T> {
    boolean detect(DetectionContext<T> context);
    void update(DetectionContext<T> context);
}
