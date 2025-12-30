package store.slackjudge.batch.infra.mongo.repository;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.testcontainers.containers.MongoDBContainer;
import store.slackjudge.batch.MongoContainer;
import store.slackjudge.batch.SlackjudgeApplication;
import store.slackjudge.batch.infra.mongo.document.SnapShotId;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@ContextConfiguration(classes = SlackjudgeApplication.class)
@DataMongoTest
@Import(MongoAutoConfiguration.class)
@ActiveProfiles("test")
class UserSolvedSnapShotRepositoryTest extends MongoContainer {
    @Autowired
    private UserSolvedSnapShotRepository repository;

    @Test
    @DisplayName("백준Id와 집계날짜로 document를 조회")
    void findByBojIdAndIdSnapshotAt(){
        //given
        LocalDateTime now=LocalDateTime.now().truncatedTo(ChronoUnit.HOURS);
        String bojId="test";
        Long userId=123L;
        int tier=23;
        int solvedCount=33;

        repository.save(makeDummyDocument(
                bojId,userId,tier,solvedCount,now
        ));

        //when
        UserSolvedSnapShotDocument result=repository.findByIdBojIdAndIdSnapShotAt(bojId,now).get();

        //then
        assertThat(result).isNotNull();
        assertThat(result.getId().getBojId()).isEqualTo(bojId);
        assertThat(result.getId().getSnapShotAt()).isEqualTo(now);
        assertThat(result.getRating()).isEqualTo(100);
        assertThat(result.getSolvedCount()).isEqualTo(solvedCount);
        assertThat(result.getTier()).isEqualTo(tier);
        assertThat(result.getUserId()).isEqualTo(userId);
    }


    private static UserSolvedSnapShotDocument makeDummyDocument(String bojId, Long userId, int tier, int solvedCount, LocalDateTime snapshotAt){
        SnapShotId id=SnapShotId.of(bojId,snapshotAt);

        return UserSolvedSnapShotDocument.builder()
                .id(id)
                .rating(100)
                .solvedCount(solvedCount)
                .solvedProblemIds(new HashSet<>())
                .tier(tier)
                .userId(userId)
                .build();
    }
}