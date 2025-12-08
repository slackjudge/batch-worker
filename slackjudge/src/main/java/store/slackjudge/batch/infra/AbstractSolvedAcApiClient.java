package store.slackjudge.batch.infra;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
public abstract class AbstractSolvedAcApiClient <T>{
    //재시도 횟수
    private final int RETRY=5;

    private final WebClient webClient;
    //생성자
    protected AbstractSolvedAcApiClient(WebClient webClient){
        this.webClient=webClient;
    }
    //API별 요청 파라미터 생성
    protected abstract Map<String,String> createRequestParameter(String bojId);

    //API별 URL 설정
    protected abstract String setUpUrl();

    //API별 응답 파싱
    protected abstract T parseResponse(String body);

    //API별 에러 처리
    protected abstract void handleError();

    /**
     * =====================
     *        공통 로직
     * =====================
     */
    public final T call(){
        String url=setUpUrl()
    }

}
