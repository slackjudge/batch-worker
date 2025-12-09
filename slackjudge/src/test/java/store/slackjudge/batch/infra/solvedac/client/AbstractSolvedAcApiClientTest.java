package store.slackjudge.batch.infra.solvedac.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;


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
                Map<String, String> params = new HashMap<>();
                params.put("query", bojId);
                params.put("page", String.valueOf(page));

                return params;
            }

            @Override
            protected String setUpUrl() {
                return BASE_URL;
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

    @Mock
    private WebClient.ResponseSpec responseSpec;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.RequestHeadersSpec requestHeadersSpec;


    private static final String FAIL = "FAIL";
    private static final String SUCCESS = "SUCCESS";
    private static final String BASE_URL="https://test";

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

    @Nested
    @DisplayName("call 동작 테스트")
    class callTets{
        @DisplayName("전체 플로우 동작")
        @Test
        void call_allFlow(){
            //given
            String bojId="testUser";
            int page=1;
            String expectedUrl=BASE_URL+"?query="+bojId+"&page="+page;
            String expectedResponse="{\"data\":\"test\"}";

            when(urlBuilder.buildUrl(anyString(), anyMap())).thenReturn(expectedUrl);
            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(expectedResponse));

            //when
            String result=client.call(bojId,page);

            //then
            assertThat(result).isEqualTo(expectedResponse);
            verify(urlBuilder, times(1)).buildUrl(anyString(), anyMap());
            verify(webClient).get();
            verify(requestHeadersUriSpec).uri(expectedUrl);
        }

        @DisplayName("parseResponse 예외 발생 시 handleError 호출")
        @Test
        void call_ParseResponseError_CallHandleError(){
            //given
            String bojId="test";
            int page=1;
            String expectedUrl=BASE_URL+"?query="+bojId+"&page="+page;
            String nonParsingJson="{\"invalid\":\"json\"}";
            AtomicInteger handleErrorCount=new AtomicInteger(0);


            AbstractSolvedAcApiClient<String> clientErrorHandler= new AbstractSolvedAcApiClient<String>(webClient,urlBuilder){

                @Override
                protected Map<String, String> createRequestParameter(String bojId, int page) {
                    Map<String,String> params=new HashMap<>();
                    params.put("query",bojId);
                    params.put("page",String.valueOf(page));

                    return params;
                }

                @Override
                protected String setUpUrl() {
                    return BASE_URL;
                }

                @Override
                protected String parseResponse(String response) {
                    throw  new RuntimeException("json parsing error");
                }

                @Override
                protected void handleError(Exception e) {
                    handleErrorCount.incrementAndGet();
                }
            };

            when(webClient.get()).thenReturn(requestHeadersUriSpec);
            when(requestHeadersUriSpec.uri(expectedUrl)).thenReturn(requestHeadersSpec);
            when(requestHeadersSpec.retrieve()).thenReturn(responseSpec);
            when(urlBuilder.buildUrl(anyString(),anyMap())).thenReturn(expectedUrl);
            when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(nonParsingJson));


            //when & then
            assertThatThrownBy(()->clientErrorHandler.call(bojId,page))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("json parsing error");
        }
    }

}