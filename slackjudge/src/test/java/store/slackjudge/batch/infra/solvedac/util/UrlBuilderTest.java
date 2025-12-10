package store.slackjudge.batch.infra.solvedac.util;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

class UrlBuilderTest {

    private static UrlBuilder urlBuilder;

    @BeforeAll
    static void setUp(){
        urlBuilder =new UrlBuilder();
    }

    @Test
    @DisplayName("base url과 파라미터 값들로 요청 url 생성")
    void buildUrl() {
        //given
        String expectedUrl="https://test.com?query=testUser&page=0&sort=desc";
        String baseUrl="https://test.com";
        Map<String, String> params=new HashMap<>();
        params.put("query","testUser");
        params.put("page","0");
        params.put("sort","desc");

        //when
        String resultUrl=urlBuilder.buildUrl(baseUrl,params);

        //then
        assertThat(resultUrl).isEqualTo(expectedUrl);
    }
}