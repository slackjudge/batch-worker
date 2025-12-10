package store.slackjudge.batch.infra.mongo.document;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

@Document(collection = "snapshot")
@Getter
@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSolvedSnapShotDocument {
    //(백준 아이디, 배치 동작 시간) 복합 키
    @Id
    private SnapShotId id;

    private Set<Integer> solvedProblemIds;

    private Integer solvedCount;

    private Integer tier;

    private Long userId;
}
