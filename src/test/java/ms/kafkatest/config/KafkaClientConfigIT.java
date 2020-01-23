package ms.kafkatest.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import com.github.tomakehurst.wiremock.WireMockServer;
import ms.kafkatest.utils.TopicConfig;
import ms.kafkatest.utils.TopicConfigService;

import java.util.ArrayList;
import java.util.Collections;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.options;
import static ms.kafkatest.config.WireMockRestTemplate.WIREMOCK_PORT_PLACEHOLDER;

@ActiveProfiles("test-integration")
@EmbeddedKafka
@SpringBootTest(classes = {
    BasicSchemaRegistryClient.class,
    RestTemplateConfiguration.class,
    WireMockRestTemplate.class,
    AdminClientConfig.class
})
@TestPropertySource(properties = {
    "spring.kafka.properties.schema.registry.url:http://localhost:" + WIREMOCK_PORT_PLACEHOLDER
})
class KafkaClientConfigIT {

    @Autowired
    private BasicSchemaRegistryClient schemaRegistryClient;
    @Autowired
    private WireMockRestTemplate restTemplate;
    @MockBean
    private AdminClient adminClient;

    private WireMockServer wireMockServer;
    private TopicConfigService topicConfigService;

    @BeforeEach
    void setUp() {
        wireMockServer = new WireMockServer(options().dynamicPort());
        wireMockServer.stubFor(post(urlEqualTo("/subjects/TestTopic-key/versions")).withRequestBody(equalTo(wrappedKeySchema())).willReturn(ok()));
        wireMockServer.stubFor(post(urlEqualTo("/subjects/TestTopic-value/versions")).withRequestBody(equalTo(wrappedValueSchema())).willReturn(ok()));
        wireMockServer.stubFor(post(urlEqualTo("/subjects/TestTopic.error-key/versions")).withRequestBody(equalTo(wrappedKeySchema())).willReturn(ok()));
        wireMockServer.stubFor(post(urlEqualTo("/subjects/TestTopic.error-value/versions")).willReturn(ok()));
        wireMockServer.start();
        restTemplate.setPort(wireMockServer.port());
        topicConfigService = () -> Collections.singletonList(new TopicConfig("TestTopic", keySchema(), valueSchema()));
    }

    @AfterEach
    void teardown() {
        wireMockServer.stop();
    }

    @Test
    void createStubKafkaClientConfig() throws Exception {
        ArgumentCaptor<ArrayList<NewTopic>> captor = ArgumentCaptor.forClass(ArrayList.class);
        KafkaClientConfig kafkaClientConfig = new KafkaClientConfig(
            schemaRegistryClient,
            Collections.singletonList(topicConfigService),
            adminClient);
        kafkaClientConfig.afterPropertiesSet();
        Mockito.verify(adminClient, Mockito.times(1)).createTopics(captor.capture());
        Mockito.verify(adminClient, Mockito.times(1)).close();
        Assertions.assertEquals("TestTopic", captor.getValue().get(0).name());
    }

    private String keySchema() {
        return "{\"namespace\":\"messaging.avro\",\"type\": \"record\",\"name\": \"GenericAvroKey\",\"fields\": [{\"name\": \"id\", \"type\": \"int\"}]}";
    }

    private String valueSchema() {
        return "{\"namespace\":\"messaging.avro\",\"type\": \"record\",\"name\": \"GenericAvroValue\",\"fields\": [{\"name\": \"payload\", \"type\": \"string\"}]}";
    }

    private static String wrappedKeySchema() {
        return "{\"schema\":\"{\\\"namespace\\\":\\\"messaging.avro\\\",\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"GenericAvroKey\\\",\\\"fields\\\": [{\\\"name\\\": \\\"id\\\", \\\"type\\\": \\\"int\\\"}]}\"}";
    }

    private static String wrappedValueSchema() {
        return "{\"schema\":\"{\\\"namespace\\\":\\\"messaging.avro\\\",\\\"type\\\": \\\"record\\\",\\\"name\\\": \\\"GenericAvroValue\\\",\\\"fields\\\": [{\\\"name\\\": \\\"payload\\\", \\\"type\\\": \\\"string\\\"}]}\"}";
    }
}
