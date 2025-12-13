package store.slackjudge.batch.infra.mongo.service.detector;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.repository.UserSolvedSnapShotRepository;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;
import store.slackjudge.batch.repository.UserJdbcRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TierChangeDetectorTest {
    @InjectMocks
    private TierChangeDetector detector;

    @Mock
    private UserJdbcRepository repository;

    @DisplayName("context 1시간 전 스냅샷의 유저의 티어외 새로 조회한 유저의 티어가 일치하지 않으면 true 반환")
    @Test
    void detect_Return_TRUE() {
        //given
        UserSolvedSnapShotDocument previous = UserSolvedSnapShotDocument.builder()
                .tier(10)
                .build();
        UserInfoResponse current = new UserInfoResponse(10, "test", 12, 10);

        DetectionContext<UserInfoResponse> context = new DetectionContext<>(current, previous, LocalDateTime.now(), 1L, "test");

        //when
        Boolean result = detector.detect(context);

        //then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("context 1시간 전 스냅샷의 유저의 티어외 새로 조회한 유저의 티어가 일치하면 false 반환")
    void update_Return_FALSE() {
        //given
        UserSolvedSnapShotDocument previous = UserSolvedSnapShotDocument.builder()
                .tier(10)
                .build();
        UserInfoResponse current = new UserInfoResponse(10, "test", 10, 10);

        DetectionContext<UserInfoResponse> context = new DetectionContext<>(current, previous, LocalDateTime.now(), 1L, "test");

        //when
        Boolean result = detector.detect(context);

        //then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("사용자 티어 변경 시 userJdbcRepository-updateUserTier 호출")
    void update() {
        //given
        UserSolvedSnapShotDocument previous = UserSolvedSnapShotDocument.builder()
                .tier(10)
                .build();
        UserInfoResponse current = new UserInfoResponse(10, "test", 30, 10);
        DetectionContext<UserInfoResponse> context = new DetectionContext<>(current, previous, LocalDateTime.now(), 1L, "test");

        //when
        detector.update(context);

        //then
        verify(repository, times(1)).updateUsersTier("test",30,10);
    }
}