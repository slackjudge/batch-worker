package store.slackjudge.batch.infra.solvedac.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import store.slackjudge.batch.infra.solvedac.SolvedAcProperties;
import store.slackjudge.batch.infra.solvedac.dto.ProblemSearchResponse;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;

import java.util.Map;
@Component
@Slf4j
public class SolvedAcProblemInfoClient extends AbstractSolvedAcApiClient<ProblemSearchResponse> {
    private final SolvedAcProperties properties;
    private final ObjectMapper objectMapper;

    protected SolvedAcProblemInfoClient(WebClient webClient, UrlBuilder urlBuilder, SolvedAcProperties properties, ObjectMapper objectMapper) {
        super(webClient, urlBuilder);
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected Map<String, String> createRequestParameter(String bojId) {
        return Map.of("query","solved_by:"+bojId);
    }

    @Override
    protected String setUpUrl() {
        return properties.getApi().getProblemInfo();
    }

    @Override
    protected ProblemSearchResponse parseResponse(String response) {
        try {
            return objectMapper.readValue(response, ProblemSearchResponse.class);
        }catch (Exception e){
            log.error("[calling solved.ac API] fetch problemInfo api json parsing error : {}",e.getMessage());
            throw new RuntimeException("[calling solved.ac API] fetch problemInfo api json parsing error");
        }
    }

    @Override
    protected void handleError(Exception e) {
        //TODO:에러 처리 구체화 진행 예정
    }
}
