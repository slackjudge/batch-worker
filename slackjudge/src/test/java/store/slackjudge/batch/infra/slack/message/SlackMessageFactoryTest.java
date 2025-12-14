package store.slackjudge.batch.infra.slack.message;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class SlackMessageFactoryTest {

    private SlackMessageFactory factory;

    @BeforeEach
    void setUp(){
        SlackTextLayout layout=new SlackTextLayout();
        this.factory=new SlackMessageFactory(layout);
    }

    @Test
    void batchStart() {
        //given
        BatchStartMessageSpec spec=new BatchStartMessageSpec(
                "SlackJudgeJob",
                LocalDateTime.of(2025,12,12,12,0,0,0),
                "worker-1"
        );

        //when
        String message=factory.batchStart(spec);

        //then
        assertThat(message).contains("[ Slack Judge Batch - START ]");
        assertThat(message).contains("Job Name : SlackJudgeJob");
        assertThat(message).contains("Batch Time : 2025-12-12 12:00:00");
        assertThat(message).contains("Worker Node : worker-1");

    }

    @Test
    void batchEnd() {
    }

    @Test
    void runtimeLog() {
    }
}