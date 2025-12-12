package store.slackjudge.batch.infra.solvedac.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import store.slackjudge.batch.infra.solvedac.SolvedAcProperties;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;

import java.util.HashMap;
import java.util.Map;

/**
 * 백준 유저의 정보(백준 티어, 푼 문제 수) 조회 클래스
 */
@Component
@Slf4j
public class SolvedAcUserInfoClient extends AbstractSolvedAcApiClient<UserInfoResponse>{
    private final SolvedAcProperties properties;
    private final ObjectMapper objectMapper;

    protected SolvedAcUserInfoClient(
            WebClient webClient,
            UrlBuilder urlBuilder,
            SolvedAcProperties properties,
            ObjectMapper objectMapper
            ) {
        super(webClient, urlBuilder);
        this.properties=properties;
        this.objectMapper=objectMapper;
    }

    /*==========================
    *
    *SolvedAcUserInfoClient
    * api 호출 시 파라미터 바인딩 메서드
    * @parm bojId:백준 아이디
    * @return HashMap 형태의 파라미터명-파라미터 값 반환
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 8.
    *
    ==========================**/
    @Override
    protected Map<String, String> createRequestParameter(String bojId,int page) {
        Map<String,String> params=new HashMap<>();
        params.put("handle",bojId);

        return params;
    }

    /*==========================
    *
    *SolvedAcUserInfoClient
    * 유저 정보 조회 url 반환
    * @parm x
    * @return baseUrl + 유저 정보 조회 url
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 8.
    *
    ==========================**/
    @Override
    protected String setUpUrl() {
        return properties.getApi().getUserInfo();
    }

    /*==========================
    *
    *SolvedAcUserInfoClient
    * 응답 Json 파싱 메서드
    * @parm response:solved.ac API 응답값
    * @return UserSearchResponse:필요 값(count:푼 문제 수,items:문제 정보들)
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 8.
    *
    ==========================**/
    @Override
    protected UserInfoResponse parseResponse(String response) {
        try {
            return objectMapper.readValue(response,UserInfoResponse.class);
        }catch (Exception e){
            log.error("[calling solved.ac API] fetch userInfo api json parsing error : {}",e.getMessage());
            throw new RuntimeException("[calling solved.ac API] fetch userInfo api json parsing error");
        }
    }


    @Override
    protected void handleError(Exception e) {
        //TODO:에러 처리 구체화 진행 예정
    }
}
