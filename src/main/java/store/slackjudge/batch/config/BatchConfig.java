package store.slackjudge.batch.config;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.*;
import org.springframework.batch.core.listener.ExecutionContextPromotionListener;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.transaction.PlatformTransactionManager;
import store.slackjudge.batch.tasklet.*;

@Configuration
@RequiredArgsConstructor
@Profile("!test")
public class BatchConfig {

    private final LoadAllUsersTasklet loadAllUsersTasklet;
    private final LoadSnapshotTasklet loadSnapshotTasklet;
    private final FetchSolvedAcUserInfoTasklet fetchSolvedAcUserInfoTasklet;
    private final DetectAndUpdateUserTierAndProblemTasklet detectAndUpdateUserTierAndProblemTasklet;
    private final SaveSnapshotTasklet saveSnapshotTasklet;
    private final BatchJobListener jobListener;

    /*==========================
    *
    *BatchConfig
    * Job 정의
    * @parm
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @Bean
    public Job slackJudgeBatch(
            JobRepository jobRepository,
            Step loadAllUsersStep,
            Step loadSnapshotStep,
            Step fetchSolvedAcUserInfoStep,
            Step detectAndUpdateUserTierAndProblemStep,
            Step saveSnapshotStep
    ) {
        return new JobBuilder("slackJudge", jobRepository)
                .listener(jobListener)
                .start(loadAllUsersStep)
                .next(loadSnapshotStep)
                .next(fetchSolvedAcUserInfoStep)
                .next(detectAndUpdateUserTierAndProblemStep)
                .next(saveSnapshotStep)
                .build();
    }


    /*==========================
    *
    *BatchConfig
    * Step01: RDB에서 유저 조회
    * @parm  jobRepository     : Batch job repository
    *        transactionManager: 트랜잭션 엔진 공통 인터페이스
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @Bean
    public Step loadAllUsersStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("LoadAllUsersStep", jobRepository)
                .tasklet(loadAllUsersTasklet, transactionManager)
                .listener(promoteUsers())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener promoteUsers() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"users"});
        return listener;
    }


    /*==========================
    *
    *BatchConfig
    * Step02: Mongo Snapshot 조회
    * @parm jobRepository     : Batch job repository
    *       transactionManager: 트랜잭션 엔진 공통 인터페이스
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @Bean
    public Step loadSnapshotStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("LoadSnapshotStep", jobRepository)
                .tasklet(loadSnapshotTasklet, transactionManager)
                .listener(promoteSnapshots())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener promoteSnapshots() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"snapshots"});
        return listener;
    }


    /*==========================
    *
    *BatchConfig
    * Step03: solved.ac 유저 정보 조회
    * @parm jobRepository     : Batch job repository
    *       transactionManager: 트랜잭션 엔진 공통 인터페이스
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @Bean
    public Step fetchSolvedAcUserInfoStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("FetchSolvedAcUserInfoStep", jobRepository)
                .tasklet(fetchSolvedAcUserInfoTasklet, transactionManager)
                .listener(promoteUserSolvedInfo())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener promoteUserSolvedInfo() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"userSolvedInfo"});
        return listener;
    }


    /*==========================
    *
    *BatchConfig
    * Step04: 변경 감지
    * @parm jobRepository     : Batch job repository
    *       transactionManager: 트랜잭션 엔진 공통 인터페이스
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @Bean
    public Step detectAndUpdateUserTierAndProblemStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("detectAndUpdateUserTierAndProblemStep", jobRepository)
                .tasklet(detectAndUpdateUserTierAndProblemTasklet, transactionManager)
                .listener(promoteCurrentSnapshot())
                .build();
    }

    @Bean
    public ExecutionContextPromotionListener promoteCurrentSnapshot() {
        ExecutionContextPromotionListener listener = new ExecutionContextPromotionListener();
        listener.setKeys(new String[]{"currentSnapshot"});
        return listener;
    }


    /*==========================
    *
    *BatchConfig
    * Step05: Snapshot 저장
    * @parm jobRepository     : Batch job repository
    *       transactionManager: 트랜잭션 엔진 공통 인터페이스
    * @return
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 17.
    *
    ==========================**/
    @Bean
    public Step saveSnapshotStep(
            JobRepository jobRepository,
            PlatformTransactionManager transactionManager
    ) {
        return new StepBuilder("saveSnapshotStep", jobRepository)
                .tasklet(saveSnapshotTasklet, transactionManager)
                .build();
    }

}
