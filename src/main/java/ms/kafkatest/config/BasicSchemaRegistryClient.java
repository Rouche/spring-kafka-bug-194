package ms.kafkatest.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestTemplate;

@Configuration
public class BasicSchemaRegistryClient {
    @Value("${spring.kafka.properties.schema.registry.url}")
    private String schemaRegistryUrl;
    private static final String SUBJECTS_KEY_PATH = "%s/subjects/%s-key/versions";
    private static final String SUBJECTS_VALUE_PATH = "%s/subjects/%s-value/versions";
    private static final String SCHEMA_WRAPPER = "{\"schema\":\"%s\"}";
    private final RestTemplate restTemplate;
    private HttpHeaders requestHeader = new HttpHeaders();

    @Autowired
    public BasicSchemaRegistryClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.requestHeader.add("Content-Type", "application/json");
    }

    void associateValueSchemaToTopic(String schema, String topic) {
        this.restTemplate.postForEntity(String.format(SUBJECTS_VALUE_PATH, this.schemaRegistryUrl, topic), this.httpEntityMaker(schema), Object.class);
    }

    void associateKeySchemaToTopic(String schema, String topic) {
        this.restTemplate.postForEntity(String.format(SUBJECTS_KEY_PATH, this.schemaRegistryUrl, topic), this.httpEntityMaker(schema), Object.class);
    }

    HttpEntity<String> httpEntityMaker(String schema) {
        return new HttpEntity<>(String.format(SCHEMA_WRAPPER, schema.replace("\"", "\\\"")), this.requestHeader);
    }
}
