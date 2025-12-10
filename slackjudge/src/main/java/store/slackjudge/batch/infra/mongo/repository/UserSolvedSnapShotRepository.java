package store.slackjudge.batch.infra.mongo.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import store.slackjudge.batch.infra.mongo.document.SnapShotId;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;

import java.util.Optional;

@Repository
public interface UserSolvedSnapShotRepository extends MongoRepository<UserSolvedSnapShotDocument, SnapShotId> {
    Optional<UserSolvedSnapShotDocument> findById(SnapShotId id);
}
