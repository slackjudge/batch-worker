package store.slackjudge.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import software.amazon.awssdk.services.eventbridge.EventBridgeClient;

@Configuration
@Profile("!test")
public class EventBridgeConfig {
    @Bean
    public EventBridgeClient eventBridgeClient(){
        return EventBridgeClient.builder().build();
    }
}
