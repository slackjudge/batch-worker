package store.slackjudge.batch.infra.solvedac.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import store.slackjudge.batch.infra.solvedac.SolvedAcProperties;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;
import store.slackjudge.batch.infra.solvedac.dto.UserSearchResponse;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;

import java.util.Map;
@Component
@Slf4j
public class SolvedAcUserInfoClient extends AbstractSolvedAcApiClient<UserSearchResponse>{

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

    @Override
    protected Map<String, String> createRequestParameter(String bojId) {
        return Map.of(
                "query",bojId
        );
    }

    @Override
    protected String setUpUrl() {
        return properties.getApi().getUserInfo();
    }

    @Override
    protected UserSearchResponse parseResponse(String response) {
        try {
            return objectMapper.readValue(response,UserSearchResponse.class);
        }catch (Exception e){
            log.error("[calling solved.ac API] fetch userInfo api json parsing error : {}",e.getMessage());
            throw new RuntimeException("[calling solved.ac API] fetch userInfo api json parsing error");
        }
    }

    @Override
    protected void handleError(Exception e) {
        //TODO:에러 처리 구체화 진행 예정
    }

    public UserInfoResponse findExactUser(String bojId){
        int page=0;

        while (true){
            UserSearchResponse response=this.callWithPage(bojId,page);

            if (response.items()==null || response.items().isEmpty()) return null;

            for (UserInfoResponse userInfo:response.items()){
                //유저의 백준 아이디가 정확히 일치하는 경우
                if (userInfo.handle().equalsIgnoreCase(bojId)){
                    return userInfo;
                }
            }

            page++;
        }
    }
}
