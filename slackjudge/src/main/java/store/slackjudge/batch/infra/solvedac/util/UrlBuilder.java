package store.slackjudge.batch.infra.solvedac.util;

import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class UrlBuilder {
    /*==========================
    *
    *UrlBuilder
    * 파라미터가 포함된 solved.ac API 요청 url 생성 메서드
    * @parm baseUrl:solved.ac API 기본 주소 / params:요청 파라미터 ex.{"handle":"tkv00"} 형태
    * @return 최종 요청 url
    * @author kimdoyeon
    * @version 1.0.0
    * @date 25. 12. 8.
    *
    ==========================**/
    public String buildUrl(String baseUrl, Map<String,String> params){
        String queryString = params.entrySet().stream()
                .map(e-> e.getKey() + "=" + URLEncoder.encode(e.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

        return baseUrl+"?"+queryString;
    }
}
