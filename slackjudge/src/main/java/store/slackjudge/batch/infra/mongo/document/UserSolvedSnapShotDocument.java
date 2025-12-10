package store.slackjudge.batch.infra.mongo.document;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;
import org.antlr.v4.runtime.misc.NotNull;
import org.hibernate.annotations.Filter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Set;

@Builder
@Document(collection = "snapshot")
@Getter
@RequiredArgsConstructor
@AllArgsConstructor(access = AccessLevel.PACKAGE)
public class UserSolvedSnapShotDocument {
    //(백준 아이디, 배치 동작 시간) 복합 키
    @Id
    private SnapShotId id;

    @Field(name = "problems_ids")
    private Set<Integer> solvedProblemIds;

    @Builder.Default
    @Field(name = "solved_count")
    private Integer solvedCount=0;

    private Integer tier;

    @NotNull
    @Field(name = "user_id")
    private Long userId;
}
