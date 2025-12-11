package store.slackjudge.batch.infra.mongo.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.slackjudge.batch.infra.mongo.document.SnapShotId;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.dto.SaveSnapshot;
import store.slackjudge.batch.infra.mongo.repository.UserSolvedSnapShotRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserSnapShotServiceTest {
    @InjectMocks
    private UserSnapShotService service;

    @Mock
    private UserSolvedSnapShotRepository repository;


    @Test
    @DisplayName("service는 bojId와 snapAt로 이전 스냅샷을 repository에서 조회")
    void findPreviousSnapshot() {
        //given
        String bojId="test";
        LocalDateTime previousTime=LocalDateTime.now();
        Long userId=2341442L;
        int tier=123;
        int solvedCount=3;
        int rating=1251;

        UserSolvedSnapShotDocument dummyDoc=makeDummyDocument(bojId,userId,tier,solvedCount,previousTime,rating);

        when(repository.findByIdBojIdAndIdSnapShotAt(bojId,previousTime)).thenReturn(Optional.ofNullable(dummyDoc));

        // when
        Optional<UserSolvedSnapShotDocument> result=service.findPreviousSnapshot(bojId,previousTime);

        //then
        UserSolvedSnapShotDocument document=result.get();
        verify(repository).findByIdBojIdAndIdSnapShotAt(bojId,previousTime);
        assertThat(document).isEqualTo(dummyDoc);

    }

    @Test
    @DisplayName("service는 SaveSnapshot dto 저장하고 저장된 document 반환")
    void saveSnapshot() {
        //given
        String bojId="test";
        LocalDateTime snapshotAt=LocalDateTime.now();
        Long userId=2341442L;
        int tier=123;
        int solvedCount=3;
        int rating=1234;


        UserSolvedSnapShotDocument dummyDoc=makeDummyDocument(bojId,userId,tier,solvedCount,snapshotAt,rating);

        SaveSnapshot request=new SaveSnapshot(
                bojId,snapshotAt,new HashSet<>(),solvedCount,tier,userId,rating
        );

        when(repository.save(any(UserSolvedSnapShotDocument.class))).thenReturn(dummyDoc);

        //when
        UserSolvedSnapShotDocument result=service.saveSnapshot(request);

        //then
        verify(repository,times(1)).save(any());
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getTier()).isEqualTo(tier);
        assertThat(result.getSolvedCount()).isEqualTo(solvedCount);
        assertThat(result.getRating()).isEqualTo(rating);
    }

    private static UserSolvedSnapShotDocument makeDummyDocument(String bojId, Long userId, int tier, int solvedCount, LocalDateTime snapshotAt,int rating){
        SnapShotId id=SnapShotId.of(bojId,snapshotAt);

        return UserSolvedSnapShotDocument.builder()
                .id(id)
                .rating(rating)
                .solvedCount(solvedCount)
                .solvedProblemIds(new HashSet<>())
                .tier(tier)
                .userId(userId)
                .build();
    }

}