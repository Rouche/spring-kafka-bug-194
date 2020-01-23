package ms.kafkatest.config.swagger;
import io.github.jhipster.config.JHipsterProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CustomSwaggerConfig {
    public CustomSwaggerConfig() {
    }

    @Bean
    public ApplicationSwaggerCustomizer applicationSwaggerCustomizer(JHipsterProperties jHipsterProperties) {
        return new ApplicationSwaggerCustomizer(jHipsterProperties);
    }
}
