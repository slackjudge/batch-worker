package store.slackjudge.batch.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import store.slackjudge.batch.config.converter.DateToLocalDateTimeKstConverter;
import store.slackjudge.batch.config.converter.LocalDateTimeToDateKstConverter;

import java.util.List;

@Configuration
public class MongoConfig {
    @Bean
    public MongoCustomConversions mongoCustomConversions(
            LocalDateTimeToDateKstConverter writer,
            DateToLocalDateTimeKstConverter reader
    ) {
        return new MongoCustomConversions(List.of(writer, reader));
    }
}
