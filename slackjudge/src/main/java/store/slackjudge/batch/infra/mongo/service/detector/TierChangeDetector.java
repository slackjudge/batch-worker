package store.slackjudge.batch.infra.mongo.service.detector;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;
import store.slackjudge.batch.repository.UserJdbcRepository;

/* [ 사용자 티어 감지 전략 ]
 * 변경 감지 전략 구현체
 */
@Component
@RequiredArgsConstructor
public class TierChangeDetector implements SnapshotDetectStrategy<UserInfoResponse>{
    private final UserJdbcRepository userRepository;

    /*==========================
    *
    *TierChangeDetector
    * solved.ac API 통신으로 최신 유저 티어 정보와 1시간 전 유저 티어 비교 메서드
    * @parm context:변경감지 객체
    * @return true:변경 o / false:변경 x
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    @Override
    public boolean detect(DetectionContext<UserInfoResponse> context) {
        return context.current().tier()!=context.previous().getTier();
    }

    /*==========================
    *
    *TierChangeDetector
    * RDB에 유저의 새 티어정보 업데이트
    * @parm context:변경감지 객체
    * @return void
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    @Override
    public void update(DetectionContext<UserInfoResponse> context) {
        userRepository.updateUsersTier(context.current().handle(),context.current().tier());
    }
}
