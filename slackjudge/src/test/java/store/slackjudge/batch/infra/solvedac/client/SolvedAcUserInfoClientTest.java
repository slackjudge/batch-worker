package store.slackjudge.batch.infra.solvedac.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import store.slackjudge.batch.infra.solvedac.SolvedAcProperties;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SolvedAcUserInfoClientTest {
     @Spy
    private ObjectMapper objectMapper;

    @Mock
    private SolvedAcProperties properties;

    @Mock
    private SolvedAcProperties.Api api;

    @InjectMocks
    private SolvedAcUserInfoClient client;

    private static final String BASE_URL="https://solved.ac/api/v3/search/user";
    private static final String PATH="valid_user.json"; //정상 응답 json

    @Nested
    @DisplayName("parameter 생성 테스트")
    class CreateRequestParameterTests{

        @Test
        @DisplayName("handle + 백준Id 붙은 url를 생성")
        void createRequestParameter_WithSolvedByPrefix(){
            //given
            String bojId="test";
            int page=10;

            //when
            Map<String,String> params=client.createRequestParameter(bojId,page);

            //then
            assertThat(params)
                    .containsEntry("handle",bojId);
        }
    }

    @Nested
    @DisplayName("setUpUrl 테스트")
    class SetUpUrlTests{
        @BeforeEach
        void setUp(){
            when(properties.getApi()).thenReturn(api);
            when(api.getUserInfo()).thenReturn(BASE_URL);
        }

        @Test
        @DisplayName("properties에서 https://solved.ac/api/v3/search/userm를 반환")
        void setUpUrl_ReturnCorrectUrl(){
            //when
            String url=client.setUpUrl();

            //then
            assertThat(url).isEqualTo(BASE_URL);
            verify(properties).getApi();
            verify(api).getUserInfo();
        }
    }

    @Nested
    @DisplayName("parseResponse 테스트")
    class ParseResponseTests{
        @DisplayName("json 정상 파싱")
        @Test
        void parseResponse_ValidJson() throws IOException {
            //given
            String json=new String(
                    Objects.requireNonNull(
                            Objects.requireNonNull(getClass().getClassLoader().getResourceAsStream(PATH)).readAllBytes()
                    )
            );

            //when
            UserInfoResponse response=client.parseResponse(json);

            //then
            assertThat(response).isNotNull();
            assertThat(response.handle()).isEqualTo("test");
            assertThat(response.rating()).isEqualTo(1628);
            assertThat(response.solvedCount()).isEqualTo(379);
            assertThat(response.tier()).isEqualTo(16);
        }


        @DisplayName("잘못된 값으로 json 파싱 시 실패 - RuntimeException 예외 발생")
        @Test
        void parseResponse_Invalid_Json(){
            //given
            String invalidJson_1 = """
                    {
                        "count":,
                    }
                    """;
            String invalidJson_2="""
                    {
                        "items":1,
                    
                    """;
            //when & then
            assertThatThrownBy(()->client.parseResponse(invalidJson_1))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("[calling solved.ac API] fetch userInfo api json parsing error");

            assertThatThrownBy(()->client.parseResponse(invalidJson_2))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessage("[calling solved.ac API] fetch userInfo api json parsing error");

        }

    }
}