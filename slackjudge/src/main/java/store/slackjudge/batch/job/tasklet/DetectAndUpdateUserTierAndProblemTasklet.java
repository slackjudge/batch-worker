package store.slackjudge.batch.job.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.common.CalculateSnapShotDate;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.dto.SaveSnapshot;
import store.slackjudge.batch.infra.mongo.service.detector.DetectionContext;
import store.slackjudge.batch.infra.mongo.service.detector.ProblemChangeDetector;
import store.slackjudge.batch.infra.mongo.service.detector.TierChangeDetector;
import store.slackjudge.batch.infra.solvedac.client.SolvedAcProblemInfoClient;
import store.slackjudge.batch.infra.solvedac.dto.ProblemSearchResponse;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * [ step 04 ]
 * solved.ac 유저 / 푼 문제 수 변경 감지 및 업데이트 tasklet
 */

@Component
@RequiredArgsConstructor
public class DetectAndUpdateUserTierAndProblemTasklet implements Tasklet {
    private final ProblemChangeDetector problemChangeDetector;
    private final TierChangeDetector tierChangeDetector;
    private final SolvedAcProblemInfoClient problemInfoClient;
    private final CalculateSnapShotDate calculateSnapShotDate;
    private final BatchLogger logger;

    private StepExecution stepExecution;
    private List<UserInfo> users;
    private Map<String, UserSolvedSnapShotDocument> snapshots;
    private Map<String, UserInfoResponse> userInfoResponses;


    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.stepStart("DetectAndUpdateUserTierAndProblemTasklet");

        //시작 시간
        long startTime = System.currentTimeMillis();

        //업데이트 된 유저
        int countOfUpdated=0;
        //변경 요청 총 유저 수
        int totalOfUser=users.size();

        LocalDateTime snapshotAt=calculateSnapShotDate.currentHour();
        Map<String, SaveSnapshot> currentSnapshots=new ConcurrentHashMap<>();


        for (UserInfo user : users) {
            //유저의 스냅샷
            //기존 유저인 경우
            if (snapshots.containsKey(user.baekJoonId())) {
                UserSolvedSnapShotDocument previous = snapshots.get(user.baekJoonId());
                //이전 스냅샷은 존재하지만 solved.ac 통신에 실패한 유저 -> step03에서 재시도 후 장애 대응
                if (!userInfoResponses.containsKey(user.baekJoonId())) continue;
                boolean changed=false;

                //최신 유저 정보
                UserInfoResponse current = userInfoResponses.get(user.baekJoonId());

                /*===========================
                 * 1) 티어 변경 감지
                 *===========================*/
                DetectionContext<UserInfoResponse> tierContext=DetectionContext.<UserInfoResponse>builder()
                        .snapshotAt(snapshotAt)
                        .previous(previous)
                        .current(current)
                        .bojId(user.baekJoonId())
                        .userId(user.userId())
                        .build();

                //티어가 변경된 경우
                if (tierChangeDetector.detect(tierContext)){
                    changed=true;
                    countOfUpdated++;
                    tierChangeDetector.update(tierContext);
                }

                /*===========================
                 * 2) 푼 문제 변경 감지
                 *===========================*/
                //default : 현재 스냅샷의 푼 문제 번호들
                Set<Integer> problems=previous.getSolvedProblemIds();

                if (current.solvedCount()!=previous.getSolvedCount()){
                    //사용자가 푼 문제 조회
                    List<Integer> fetchAllProblemsIds=problemInfoClient.fetchAllProblems(user.baekJoonId());
                    problems=new HashSet<>(fetchAllProblemsIds);

                    DetectionContext<List<Integer>> problemContext= DetectionContext.<List<Integer>>builder()
                            .userId(user.userId())
                            .snapshotAt(snapshotAt)
                            .previous(previous)
                            .current(fetchAllProblemsIds)
                            .bojId(user.baekJoonId())
                            .build();

                    //변경이 된 경우
                    if (problemChangeDetector.detect(problemContext)){
                        //단일 업데이트 TODO:벌크성 업데이트 고려 Pull Request #22 @starboxxxx 님 제안
                        if (!changed) countOfUpdated++;
                        problemChangeDetector.update(problemContext);
                    }

                }

                Set<Integer> newProblemsIds=new HashSet<>(problems);
                SaveSnapshot newSnapshot=new SaveSnapshot(
                        user.baekJoonId(),
                        snapshotAt,
                        newProblemsIds,
                        newProblemsIds.size(),
                        userInfoResponses.get(user.baekJoonId()).tier(),
                        user.userId(),
                        userInfoResponses.get(user.baekJoonId()).rating()
                );

                //다음 step 전달 값에 저장
                currentSnapshots.put(user.baekJoonId(),newSnapshot);
            }
            //새로 회원가입한 유저인 경우
            else{
                List<Integer> fetchAllProblemsId=problemInfoClient.fetchAllProblems(user.baekJoonId());
                Set<Integer> setOfAllProblemIds=new HashSet<>(fetchAllProblemsId);

                //새로 조회(solved.ac)한 정보를 모두 삽입
                SaveSnapshot newSnapShot=new SaveSnapshot(
                        user.baekJoonId(),
                        snapshotAt,
                        setOfAllProblemIds,
                        setOfAllProblemIds.size(),
                        userInfoResponses.get(user.baekJoonId()).tier(),
                        user.userId(),
                        userInfoResponses.get(user.baekJoonId()).rating()
                );

                //다음 step 전달 값에 저장
                currentSnapshots.put(user.baekJoonId(),newSnapShot);
            }
        }

        //현재 step 메타 데이터 객체
        ExecutionContext stepContext=this.stepExecution.getExecutionContext();

        //{ currentSnapshot :  Map<String, SaveSnapshot> }
        stepContext.put("currentSnapshot",currentSnapshots);

        long duration=System.currentTimeMillis()-startTime;
        logger.stepEnd("DetectAndUpdateUserTierAndProblemTasklet","updated=",countOfUpdated,"total=",totalOfUser,"duration=",duration);

        return RepeatStatus.FINISHED;
    }

    @BeforeStep
    public void retrievePreviousStepData(StepExecution stepExecution) {
        this.stepExecution = stepExecution;

        final JobExecution jobExecution = stepExecution.getJobExecution();
        final ExecutionContext jobContext = jobExecution.getExecutionContext();

        this.users = (List<UserInfo>) jobContext.get("users");
        this.snapshots = (Map<String, UserSolvedSnapShotDocument>) jobContext.get("snapshots");
        this.userInfoResponses = (Map<String, UserInfoResponse>) jobContext.get("userSolvedInfo");
    }
}
