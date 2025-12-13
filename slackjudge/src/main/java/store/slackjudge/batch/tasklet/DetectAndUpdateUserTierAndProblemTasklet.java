package store.slackjudge.batch.tasklet;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
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
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;

import java.time.LocalDateTime;
import java.util.*;

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

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.stepStart("DetectAndUpdateUserTierAndProblemTasklet");
        long startTime = System.currentTimeMillis();

        // 1. Job ExecutionContext에서 데이터 가져오기
        StepExecution stepExecution = chunkContext.getStepContext().getStepExecution();
        JobExecution jobExecution = stepExecution.getJobExecution();
        ExecutionContext jobContext = jobExecution.getExecutionContext();

        List<UserInfo> users = (List<UserInfo>) jobContext.get("users");
        Map<String, UserSolvedSnapShotDocument> snapshots =
                (Map<String, UserSolvedSnapShotDocument>) jobContext.get("snapshots");
        Map<String, UserInfoResponse> userInfoResponses =
                (Map<String, UserInfoResponse>) jobContext.get("userSolvedInfo");

        // 2. 데이터 검증 및 null-safe 처리
        if (users == null || users.isEmpty()) {
            logger.stepEnd("DetectAndUpdateUserTierAndProblemTasklet", "No users found");
            return RepeatStatus.FINISHED;
        }

        if (snapshots == null) {
            snapshots = Collections.emptyMap();
        }
        if (userInfoResponses == null) {
            userInfoResponses = Collections.emptyMap();
        }

        // 3. 통계 변수 초기화
        int countOfUpdated = 0;
        int countOfNewUsers = 0;
        int countOfSkipped = 0;
        int totalOfUser = users.size();

        LocalDateTime snapshotAt = calculateSnapShotDate.currentHour();
        Map<String, SaveSnapshot> currentSnapshots = new HashMap<>();

        // 4. 유저별 처리
        for (UserInfo user : users) {
            String bojId = user.baekJoonId();

            // 4-1. solved.ac 통신 실패한 유저는 스킵
            if (!userInfoResponses.containsKey(bojId)) {
                countOfSkipped++;
                logger.taskletWarn("Skipped user due to no solved.ac info: " + bojId);
                continue;
            }

            UserInfoResponse currentUserInfo = userInfoResponses.get(bojId);
            UserSolvedSnapShotDocument previousSnapshot = snapshots.get(bojId);

            // 4-2. 신규 유저 처리
            if (previousSnapshot == null) {
                List<Integer> allProblems = problemInfoClient.fetchAllProblems(bojId);
                Set<Integer> problemSet = new HashSet<>(allProblems);
                logger.userInfo(user.userId(),user.baekJoonId(), allProblems.toString());

                logger.userInfo(user.userId(), user.baekJoonId(), "New user");

                DetectionContext<UserInfoResponse> tierContext = DetectionContext.<UserInfoResponse>builder()
                    .snapshotAt(snapshotAt)
                    .current(currentUserInfo)
                    .bojId(bojId)
                    .userId(user.userId())
                    .build();
                tierChangeDetector.update(tierContext);

                logger.userInfo(user.userId(), user.baekJoonId(),
                        "New User Tier updated : " + tierContext.current().tier());

                problemChangeDetector.saveNewProblem(snapshotAt,user.userId(),allProblems);
                logger.userInfo(user.userId(), user.baekJoonId(),
                        "New User Problems updated : " + allProblems.size());

                SaveSnapshot newUserSnapshot = new SaveSnapshot(
                        bojId,
                        snapshotAt,
                        problemSet,
                        problemSet.size(),
                        currentUserInfo.tier(),
                        user.userId(),
                        currentUserInfo.rating()
                );

                currentSnapshots.put(bojId, newUserSnapshot);
                countOfNewUsers++;
                continue;
            }

            // 4-3. 기존 유저 처리
            boolean hasChanges = false;

            // 4-3-1. 티어 변경 감지 및 업데이트
            DetectionContext<UserInfoResponse> tierContext = DetectionContext.<UserInfoResponse>builder()
                    .snapshotAt(snapshotAt)
                    .previous(previousSnapshot)
                    .current(currentUserInfo)
                    .bojId(bojId)
                    .userId(user.userId())
                    .build();

            if (tierChangeDetector.detect(tierContext)) {
                hasChanges = true;
                tierChangeDetector.update(tierContext);
                logger.userInfo(user.userId(), user.baekJoonId(),
                        "Tier changed : " + tierContext.current().tier());
            }

            // 4-3-2. 푼 문제 변경 감지 및 업데이트
            Set<Integer> currentProblems = new HashSet<>(previousSnapshot.getSolvedProblemIds());
            int previousSolvedCount = previousSnapshot.getSolvedCount();
            int currentSolvedCount = currentUserInfo.solvedCount();

            if (currentSolvedCount != previousSolvedCount) {
                List<Integer> fetchedProblems = problemInfoClient.fetchAllProblems(bojId);
                currentProblems = new HashSet<>(fetchedProblems);

                DetectionContext<List<Integer>> problemContext = DetectionContext.<List<Integer>>builder()
                        .userId(user.userId())
                        .snapshotAt(snapshotAt)
                        .previous(previousSnapshot)
                        .current(fetchedProblems)
                        .bojId(bojId)
                        .build();

                if (!problemChangeDetector.detect(problemContext)) {
                    hasChanges = true;
                    problemChangeDetector.update(problemContext);
                }
            }

            // 4-3-3. 현재 스냅샷 생성 및 저장
            SaveSnapshot newSnapshot = new SaveSnapshot(
                    bojId,
                    snapshotAt,
                    currentProblems,
                    currentProblems.size(),
                    currentUserInfo.tier(),
                    user.userId(),
                    currentUserInfo.rating()
            );

            currentSnapshots.put(bojId, newSnapshot);

            if (hasChanges) {
                countOfUpdated++;
            }
        }

        // 5. 다음 step으로 데이터 전달
        ExecutionContext stepContext = stepExecution.getExecutionContext();
        stepContext.put("currentSnapshot", currentSnapshots);

        // 6. 처리 결과 로깅
        long duration = System.currentTimeMillis() - startTime;
        logger.stepEnd(
                "DetectAndUpdateUserTierAndProblemTasklet",
                "new=" + countOfNewUsers,
                "updated=" + countOfUpdated,
                "skipped=" + countOfSkipped,
                "total=" + totalOfUser,
                "duration=" + duration
        );

        return RepeatStatus.FINISHED;
    }
}