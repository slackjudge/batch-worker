package store.slackjudge.batch.infra.mongo.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import store.slackjudge.batch.infra.mongo.document.SnapShotId;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.dto.SaveSnapshot;
import store.slackjudge.batch.infra.mongo.repository.UserSolvedSnapShotRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * user의 스냅샷 저장 / 조회 클래스
 */
@Service
@RequiredArgsConstructor
public class UserSnapShotService {
    private final UserSolvedSnapShotRepository repository;

    /*==========================
    *
    *UserSnapShotService
    * 1시간 전 스냅 샷 조회 메서드
    * @parm bojId:백준 아이디 / previousTime:1시간 전 시간
    * @return 유저+문제 풀이 정보 도큐먼트
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    @Transactional(readOnly = true)
    public Optional<UserSolvedSnapShotDocument> findPreviousSnapshot(String bojId, LocalDateTime previousTime){
        return repository.findByIdBojIdAndIdSnapShotAt(bojId,previousTime);
    }

    /*==========================
    *
    *UserSnapShotService
    * Snapshot Document 객체를 저장하는 메서드
    * @parm SaveSnapshot:데이터 전달을 위한 DTO
    * @return 저장된 snapshot document
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 10.
    *
    ==========================**/
    @Transactional
    public UserSolvedSnapShotDocument saveSnapshot(SaveSnapshot snapshot){
        SnapShotId id=SnapShotId.of(snapshot.bojId(),snapshot.snapShotAt());
        UserSolvedSnapShotDocument document=UserSolvedSnapShotDocument.builder()
                .id(id)
                .solvedCount(snapshot.solvedCount())
                .solvedProblemIds(snapshot.solvedProblemIds())
                .tier(snapshot.tier())
                .userId(snapshot.userId())
                .build();

        //객체 저장
        return repository.save(document);
    }

    /*==========================
    *
    *UserSnapShotService
    * 1시간 전 모든 유저의 스냅 샷 조회 메서드
    * @parm bojId:백준 아이디 리스트 / previousTime:1시간 전 시간
    * @return 유저+문제 풀이 정보 도큐먼트 리스트
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 12.
    *
    ==========================**/
    //TODO:user단위 트랜잭션 처리로 이 부분은 삭제해야 함.
    @Transactional(readOnly = true)
    public List<UserSolvedSnapShotDocument> findUsersAllPreviousSnapshot(List<String> bojIds,LocalDateTime snapshotAt){
        return bojIds
                .stream()
                .map(b->repository.findByIdBojIdAndIdSnapShotAt(b,snapshotAt))
                .flatMap(Optional::stream)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<UserSolvedSnapShotDocument> findUserPreviousSnapshot(String bojId,LocalDateTime snapshotAt){
        return repository.findByIdBojIdAndIdSnapShotAt(bojId,snapshotAt);
    }
}
