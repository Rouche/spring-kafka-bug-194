package ms.kafkatest.config;

import ms.kafkatest.client.UserFeignClientInterceptor;

import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Configuration
public class RestTemplateConfiguration {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplateBuilder().
            interceptors(List.of(new UserFeignClientInterceptor()))
            .build();
    }
}
