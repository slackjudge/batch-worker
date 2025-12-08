package store.slackjudge.batch.infra.solvedac;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "solved")
public class SolvedAcProperties {
    private Api api;

    @Getter
    @Setter
    public static class Api{
        private String userInfo;
        private String problemInfo;
    }
}
