package store.slackjudge.batch.infra.solvedac.client;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import store.slackjudge.batch.infra.solvedac.SolvedAcProperties;
import store.slackjudge.batch.infra.solvedac.dto.UserInfoResponse;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;

import java.util.Map;
@Component
public class SolvedAcUserInfoClient extends AbstractSolvedAcApiClient<UserInfoResponse>{

    private final SolvedAcProperties properties;

    protected SolvedAcUserInfoClient(WebClient webClient, UrlBuilder urlBuilder,SolvedAcProperties properties) {
        super(webClient, urlBuilder);
        this.properties=properties;
    }

    @Override
    protected Map<String, String> createRequestParameter(String bojId) {
        return Map.of();
    }

    @Override
    protected String setUpUrl() {
        return properties.getApi().getUserInfo();
    }

    @Override
    protected UserInfoResponse parseResponse(String response) {
        return null;
    }

    @Override
    protected void handleError(Exception e) {
        //TODO:에러 처리 구체화 진행
    }
}
