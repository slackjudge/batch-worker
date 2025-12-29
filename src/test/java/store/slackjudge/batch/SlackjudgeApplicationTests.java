package store.slackjudge.batch;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import store.slackjudge.batch.infra.aws.EventBridgePublisher;

@SpringBootTest
@ActiveProfiles("test")
class SlackjudgeApplicationTests {

	@Test
	void contextLoads() {
	}

}
