package store.slackjudge.batch.infra.solvedac.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import store.slackjudge.batch.infra.solvedac.util.UrlBuilder;

import java.util.Map;
import java.util.function.Supplier;

/**
 * method-template 패턴으로 여러 solved.ac API 통신 구현을 위한 추상 클래스
 * @param <T>
 */
@Slf4j
public abstract class AbstractSolvedAcApiClient <T>{
    //재시도 횟수
    private final int RETRY=5;


    private final WebClient webClient;
    private final UrlBuilder urlBuilder;

    //생성자
    protected AbstractSolvedAcApiClient(WebClient webClient,UrlBuilder urlBuilder){
        this.webClient=webClient;
        this.urlBuilder=urlBuilder;
    }
    //API별 요청 파라미터 생성
    protected abstract Map<String,String> createRequestParameter(String bojId);

    //API별 URL 설정
    protected abstract String setUpUrl();

    //API별 응답 파싱
    protected abstract T parseResponse(String response);

    //API별 에러 처리
    protected abstract void handleError(Exception e);

    //API 호출
    protected String request(String url){
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(String.class)
                .block();
    }

    //재시도
    protected <R> R retry(Supplier<R> supplier){
        int attempt=0; //시도 횟수

        while(true){
            try {
                return supplier.get();
            }catch (Exception e){
                attempt++;

                if (attempt>=RETRY) {
                    log.error("[calling solved.ac API] retry all failed");//재시도 모두 실패
                    throw e;
                }

                log.warn("[calling solved.ac API] request failed attempt = {}/{}",attempt,RETRY);
            }
        }
    }

    //요청
    protected T callWithParams(Map<String,String> params){
        String baseUrl=setUpUrl();
        String url=urlBuilder.buildUrl(baseUrl,params);

        log.debug("[calling solved.ac API] url : {}",url);

        try {
            return retry(()->parseResponse(request(url)));
        }catch (Exception e){
            handleError(e);
            throw e;
        }
    }
    /**
     * =====================
     *        공통 로직
     * =====================
     */

    //페이징 쿼리 요청
    public final T callWithPage(String bojId,int page){
        Map<String,String> params=createRequestParameter(bojId);
        params.put("page",String.valueOf(page));

        return callWithParams(params);
    }

    //일반 요청
    public final T callWIthNonPage(String bojId){
        return callWithParams(createRequestParameter(bojId));
    }

}
