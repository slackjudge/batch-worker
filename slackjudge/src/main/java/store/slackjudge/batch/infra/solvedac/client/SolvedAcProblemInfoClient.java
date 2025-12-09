package store.slackjudge.batch.infra.solvedac.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import store.slackjudge.batch.infra.solvedac.SolvedAcProperties;
import store.slackjudge.batch.infra.solvedac.dto.ProblemInfoResponse;
import store.slackjudge.batch.infra.solvedac.dto.ProblemSearchResponse;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;

import java.util.*;

/**
 * 백준 문제 정보(문제 번호) 조회 클래스
 */
@Component
@Slf4j
public class SolvedAcProblemInfoClient extends AbstractSolvedAcApiClient<ProblemSearchResponse> {
    private final SolvedAcProperties properties;
    private final ObjectMapper objectMapper;
    private final static int MAX_PAGE=689;
    private final static int MIN_PAGE=1;

    protected SolvedAcProblemInfoClient(WebClient webClient, UrlBuilder urlBuilder, SolvedAcProperties properties, ObjectMapper objectMapper) {
        super(webClient, urlBuilder);
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    /*==========================
    *
    *SolvedAcProblemInfoClient
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
        /**
         * 현재 존재하는 백준 문제 개수 : 34889
         * 최대 페이징 제한 : 689
         */
        Map<String,String> params=new HashMap<>();
        params.put("query","solved_by:"+bojId);

        if (page<MIN_PAGE || page>MAX_PAGE) {
            log.warn("[invalid page value] max page value : {} min page value : {} now page value : {}",MAX_PAGE,MIN_PAGE,page);
            params.put("page",String.valueOf(1));
            return params;
        }

        params.put("page",String.valueOf(page));

        return params;
    }

    /*==========================
    *
    *SolvedAcProblemInfoClient
    * 문제 정보 조회 url 반환
    * @parm x
    * @return baseUrl + 문제 정보 조회 url
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 8.
    *
    ==========================**/
    @Override
    protected String setUpUrl() {
        return properties.getApi().getProblemInfo();
    }

    /*==========================
    *
    *SolvedAcProblemInfoClient
    * 응답 Json 파싱 메서드
    * @parm response:solved.ac API 응답값
    * @return ProblemSearchResponse:필요 값(count:푼 문제 수,items:문제 정보들)
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 8.
    *
    ==========================**/
    @Override
    protected ProblemSearchResponse parseResponse(String response) {
        try {
            return objectMapper.readValue(response, ProblemSearchResponse.class);
        } catch (Exception e) {
            log.error("[calling solved.ac API] fetch problemInfo api json parsing error : {}", e.getMessage());
            throw new RuntimeException("[calling solved.ac API] fetch problemInfo api json parsing error");
        }
    }

    @Override
    protected void handleError(Exception e) {
        //TODO:에러 처리 구체화 진행 예정
    }

     //TODO:page 증가하면서 모든 문제 찾기 -> 배치 job에서 수행
    /*public List<Integer> fetchAllProblems(String bojId) {
        List<Integer> allProblemIds = new ArrayList<>();
        int page = 1;

        while (true) {
            ProblemSearchResponse response = this.callWithPage(bojId, page);
            log.info("[calling solved.ac API] call with page problem response : {}", response);
            if (response.items() == null || response.items().isEmpty()) {
                break;
            }
            log.info("[calling solved.ac API] fetch problemInfo api json parsing size : {}", response.items().size());

            allProblemIds.addAll(
                    response.items().stream()
                            .map(ProblemInfoResponse::problemId)
                            .toList()
            );

            page++;
        }

        return allProblemIds;
    }*/
}
