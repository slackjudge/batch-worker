package store.slackjudge.batch.job.tasklet;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.annotation.BeforeStep;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Component;
import store.slackjudge.batch.common.CalculateSnapShotDate;
import store.slackjudge.batch.config.BatchLogger;
import store.slackjudge.batch.dto.UserInfo;
import store.slackjudge.batch.infra.mongo.document.UserSolvedSnapShotDocument;
import store.slackjudge.batch.infra.mongo.service.detector.DetectionContext;
import store.slackjudge.batch.infra.solvedac.client.SolvedAcProblemInfoClient;
import store.slackjudge.batch.infra.solvedac.client.SolvedAcUserInfoClient;
import store.slackjudge.batch.infra.solvedac.dto.ProblemInfoResponse;
import store.slackjudge.batch.infra.solvedac.dto.ProblemSearchResponse;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;
import store.slackjudge.batch.infra.solvedac.dto.UserSearchResponse;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * [ step 03 ]
 * solved.ac 유저 정보 API 호출 tasklet (변경 감지는 해당 step에서 진행하지 않는다)
 */

@Slf4j
@RequiredArgsConstructor
@Component
public class FetchSolvedAcUserInfoTasklet implements Tasklet {
    /**
     * next step : DetectAndUpdateUserTierAndProblemTasklet
     */
    private final BatchLogger logger;
    private final SolvedAcUserInfoClient userInfoClient;
    private final RetryTemplate retryTemplate;

    private StepExecution stepExecution;
    private List<UserInfo> users;

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        logger.stepStart("FetchSolvedAcUserInfoTasklet");
        //시작 시간
        long startTime = System.currentTimeMillis();

        Map<String, UserInfoResponse> userInfoResponseMap = new ConcurrentHashMap<>();

        for (UserInfo user : users) {
            try {
                //solved.ac API에서 유저 정보 조회
                UserInfoResponse solvedAcInfo = retryTemplate.execute(context ->
                        userInfoClient.findExactUser(user.baekJoonId())
                );

                //다음 step으로 전달할 메타데이터 객체
                userInfoResponseMap.put(user.baekJoonId(), solvedAcInfo);
            } catch (Exception e) {
                //실패하는 경우
                logger.userWarn(user.userId(), user.baekJoonId(), e.getMessage());
            }
        }
        //종료 시간 - 시작 시간
        long duration = System.currentTimeMillis() - startTime;

        //현재 step 메타 데이터 객체
        ExecutionContext stepContext = this.stepExecution.getExecutionContext();

        //{ userSolvedInfo : Map<String, UserInfoResponse> }
        stepContext.put("userSolvedInfo", userInfoResponseMap);

        logger.stepEnd("FetchSolvedAcUserInfoTasklet",
                "fetched=", userInfoResponseMap.size(),
                "totalUserSize=", users.size(),
                "duration=", duration
        );

        return RepeatStatus.FINISHED;
    }

    @BeforeStep
    public void beforeStepExecution(StepExecution stepExecution) {
        this.stepExecution = stepExecution;

        final JobExecution jobExecution = stepExecution.getJobExecution();
        final ExecutionContext jobContext = jobExecution.getExecutionContext();

        this.users = (List<UserInfo>) jobContext.get("users");
    }
}
