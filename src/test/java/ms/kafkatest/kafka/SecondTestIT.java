package ms.kafkatest.kafka;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import com.hazelcast.config.CacheConfiguration;
import ms.kafkatest.messaging.avro.GenericAvroMessage;
import ms.kafkatest.utils.MockSchemaRegistryConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ActiveProfiles({"test-integration"})
@SpringBootTest(classes = {

    LiquibaseProperties.class,

    MockSchemaRegistryConfig.class,

    CacheConfiguration.class
})
@EnableAutoConfiguration
@EmbeddedKafka(partitions = 1, topics = {"topic2"})
@DirtiesContext
class SecondTestIT {

    @Autowired
    private DefaultKafkaProducerFactory producerFactory;

    private KafkaTemplate<String, GenericAvroMessage> template;

    @BeforeEach
    void setUp() {

        // create a Kafka template
        template = new KafkaTemplate<>(producerFactory);
        // set the default topic to send to
        template.setDefaultTopic("topic2");
    }

    @Test
    void doit_lancer_le_job_d_envoi_a_ima_et_construire_le_fichier() throws Exception {

        // Given
        GenericAvroMessage message = new GenericAvroMessage();
        message.setPayload("1234567890 1234567890 1234567890 1234567890 1234567890");

        // When
        ListenableFuture<SendResult<String, GenericAvroMessage>> future = template.sendDefault(message);
        template.sendDefault(message);
        template.sendDefault(message);
        template.sendDefault(message);
        template.sendDefault(message);
        template.sendDefault(message);

        // Then
        // register a callback with the listener to receive the result of the send asynchronously
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, GenericAvroMessage> result) {
                assertEquals(0, result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
            }
        });
    }
}
