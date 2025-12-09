package store.slackjudge.batch.infra.solvedac.client;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.reactive.function.client.WebClient;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * 추상 메서드는 각 구현 클래스에서 테스트 진행
 * 해당 테스트에서는 작성 X
 */
@ExtendWith(MockitoExtension.class)
class AbstractSolvedAcApiClientTest {
    private AbstractSolvedAcApiClient<String> client;

    /**
     * test용 stub 객체
     */
    @BeforeEach
    void setUp() {
        client = new AbstractSolvedAcApiClient<String>(webClient, urlBuilder) {
            @Override
            protected Map<String, String> createRequestParameter(String bojId, int page) {
                bojId = "test";
                page = 3;
                Map<String, String> params = new HashMap<>();
                params.put("query", bojId);
                params.put("page", String.valueOf(page));

                return params;
            }

            @Override
            protected String setUpUrl() {
                return "https://test-url";
            }

            @Override
            protected String parseResponse(String response) {
                return response;
            }

            @Override
            protected void handleError(Exception e) {

            }
        };
    }

    @Mock
    private WebClient webClient;

    @Mock
    private UrlBuilder urlBuilder;

    private static final String FAIL = "FAIL";
    private static final String SUCCESS = "SUCCESS";


    @Nested
    @DisplayName("retry 동작 테스트")
    class retryTest {
        @Test
        @DisplayName("처음 성공 시 결과 값 반환")
        void retry_First_Success() {
            //given
            AtomicInteger attempt = new AtomicInteger(0);
            Supplier<String> supplier = () -> {
                attempt.incrementAndGet();
                return SUCCESS;
            };

            //when
            String result = client.retry(supplier).toString();

            //then
            assertThat(result).isEqualTo(SUCCESS);
            assertThat(attempt.get()).isEqualTo(1);
        }

        @Test
        @DisplayName("3번 실패 후 성공하면 결과 반환")
        void retry_ThirdAttemptNextSuccess() {
            //given
            AtomicInteger attempt = new AtomicInteger(0);
            Supplier<String> supplier = () -> {
                if (attempt.incrementAndGet() < 3) {
                    throw new RuntimeException(FAIL);
                }
                return SUCCESS;
            };

            //when
            String result = client.retry(supplier);

            //then
            assertThat(result).isEqualTo(SUCCESS);
            assertThat(attempt.get()).isEqualTo(3);
        }

        @Test
        @DisplayName("5회 모두 실패하면 예외 발생")
        void retry_AllFailed() {
            //given
            Supplier<String> supplier = () -> {
                throw new RuntimeException(FAIL);
            };

            //when & then
            assertThatThrownBy(() -> client.retry(supplier))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage(FAIL);
        }
    }

}