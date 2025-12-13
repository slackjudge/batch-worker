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
    private final static int MAX_PAGE = 689;
    private final static int MIN_PAGE = 1;
    private final static int ITEMS_PER_PAGE = 50;

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
    protected Map<String, String> createRequestParameter(String bojId, int page) {
        /**
         * 현재 존재하는 백준 문제 개수 : 34889
         * 최대 페이징 제한 : 689
         */
        Map<String, String> params = new HashMap<>();
        params.put("query", "solved_by:" + bojId);

        if (page < MIN_PAGE || page > MAX_PAGE) {
            log.warn("[invalid page value] max page value : {} min page value : {} now page value : {}", MAX_PAGE, MIN_PAGE, page);
            params.put("page", String.valueOf(MIN_PAGE));
            return params;
        }

        params.put("page", String.valueOf(page));

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



    public List<Integer> fetchAllProblems(String bojId) {
        List<Integer> allProblemIds = new ArrayList<>();

        try {
            //1.첫 번째 페이지로 전체 문제 개수 확인
            ProblemSearchResponse firstResponse = this.call(bojId, 1);

            if (firstResponse == null) {
                log.warn("[fetchAllProblems] No response for user : {}", bojId);
                return Collections.emptyList();
            }

            int totalCount = firstResponse.count();
            log.info("[fetchAllProblems] user : {}, total problems : {}", bojId, totalCount);

            //푼 문제가 없는 경우
            if (totalCount == 0) {
                return Collections.emptyList();
            }

            //첫 번째 페이지 추가
            if (firstResponse.items() != null && !firstResponse.items().isEmpty()) {
                allProblemIds.addAll(
                        firstResponse.items().stream()
                                .map(ProblemInfoResponse::problemId)
                                .toList()
                );
            }

            //2.필요 페이지 계산
            int totalPages = (int) Math.ceil((double) totalCount / ITEMS_PER_PAGE);
            totalPages= Math.min(totalPages, MAX_PAGE);

            //3. 나머지 페이지 조회
            for (int page=2;page<=totalPages;page++){
                ProblemSearchResponse response=this.call(bojId,page);

                if (response==null || response.items()==null || response.items().isEmpty()){
                    break;
                }

                allProblemIds.addAll(
                        response.items().stream()
                                .map(ProblemInfoResponse::problemId)
                                .toList()
                );

                log.debug("[fetchAllProblems] Fetched page {}/{}",page,totalPages);
            }

            //4. 페이지 수 검증
            if (allProblemIds.size()!=totalCount){
                log.warn("[fetchAllProblems] size mismatch - user : {}, fetched : {}, expected : {}",
                        bojId,allProblemIds.size(),totalCount
                );
            }

            return allProblemIds;
        }catch (Exception e){
            log.error("[fetchAllProblems] failed to fetch problems for user : {}, error:{}",bojId,e.getMessage(),e);
            return Collections.emptyList();
        }
    }
}
