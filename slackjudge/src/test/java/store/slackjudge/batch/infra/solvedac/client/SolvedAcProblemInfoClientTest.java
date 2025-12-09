package store.slackjudge.batch.infra.solvedac.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import store.slackjudge.batch.infra.solvedac.SolvedAcProperties;
import store.slackjudge.batch.infra.solvedac.dto.ProblemSearchResponse;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 *   SolvedAcProblemInfoClient 테스트
 *   추상 메서드 구현부만 테스트
 */
@ExtendWith(MockitoExtension.class)
class SolvedAcProblemInfoClientTest {
    @Spy
    private ObjectMapper objectMapper;

    @Mock
    private SolvedAcProperties properties;

    @Mock
    private SolvedAcProperties.Api api;

    @InjectMocks
    private SolvedAcProblemInfoClient client;

    private static final String BASE_URL="https://solved.ac/api/v3/search/problem";
    private static final String PATH="valid_problem.json"; //정상 응답 json

    @Nested
    @DisplayName("parameter 생성 테스트")
    class CreateRequestParameterTests{

        @Test
        @DisplayName("solved_by + page가 붙은 url를 생성")
        void createRequestParameter_WithSolvedByPrefix(){
            //given
            String bojId="test";
            int page=10;

            //when
            Map<String,String> params=client.createRequestParameter(bojId,page);

            //then
            assertThat(params)
                    .containsEntry("query","solved_by:"+bojId)
                    .containsEntry("page",String.valueOf(page));
        }

        @Test
        @DisplayName("page가 1보다 작을 시 페이지 기본값 1으로 파라미터 생성")
        void createRequestParameter_WhenPageIsLessThanOne_UsesPageOne(){
            //given
            String bojId="test";
            int page=-1;

            //when
            Map<String,String> params=client.createRequestParameter(bojId,page);

            //then
            assertThat(params)
                    .containsEntry("query","solved_by:"+bojId)
                    .containsEntry("page",String.valueOf(1));
        }

        @Test
        @DisplayName("page가 689보다 크면 페이지 기본값 1으로 파라미터 생성")
        void createRequestParameter_WhenPageExceedsMax_ThrowsException(){
            //given
            String bojId="test";
            int page=700;

            //when
            Map<String,String> params=client.createRequestParameter(bojId,page);

            //then
            assertThat(params)
                    .containsEntry("query","solved_by:"+bojId)
                    .containsEntry("page",String.valueOf(1));
        }
    }

    @Nested
    @DisplayName("setUpUrl 테스트")
    class SetUpUrlTests{
        @BeforeEach
        void setUp(){
            when(properties.getApi()).thenReturn(api);
            when(api.getProblemInfo()).thenReturn(BASE_URL);
        }

        @Test
        @DisplayName("properties에서 https://solved.ac/api/v3/search/problem를 반환")
        void setUpUrl_ReturnCorrectUrl(){
            //when
            String url=client.setUpUrl();

            //then
            assertThat(url).isEqualTo(BASE_URL);
            verify(properties).getApi();
            verify(api).getProblemInfo();
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
            ProblemSearchResponse response=client.parseResponse(json);

            //then
            assertThat(response).isNotNull();
            assertThat(response.count()).isEqualTo(2);
            assertThat(response.items()).isNotEmpty();
            assertThat(response.items().get(0).problemId()).isEqualTo(1000);
            assertThat(response.items().get(1).problemId()).isEqualTo(1001);
        }

        @DisplayName("items가 빈 배열로 반환될 때 json 파싱 성공")
        @Test
        void parseResponse_Validation_With_Empty_Items(){
            //given
            String json = """
                    {
                        "count":0,
                        "items":[]
                    }
                    """;

            //when
            ProblemSearchResponse response=client.parseResponse(json);

            //then
            assertThat(response).isNotNull();
            assertThat(response.items()).isEqualTo(Collections.EMPTY_LIST);
            assertThat(response.count()).isEqualTo(0);
        }

    }

}