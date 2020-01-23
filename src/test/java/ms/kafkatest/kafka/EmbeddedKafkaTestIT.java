package ms.kafkatest.kafka;

import java.time.Duration;
import java.util.Map;
import java.util.Properties;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.EmbeddedKafkaBroker;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.kafka.test.utils.KafkaTestUtils;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.util.concurrent.ListenableFuture;
import org.springframework.util.concurrent.ListenableFutureCallback;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import io.confluent.kafka.serializers.KafkaAvroSerializer;
import ms.kafkatest.messaging.avro.GenericAvroMessage;
import ms.kafkatest.utils.KafkaRecordItemReader;
import ms.kafkatest.utils.KafkaRecordItemReaderBuilder;
import ms.kafkatest.utils.MockSchemaRegistryConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author Jean-Francois Larouche (rouche) on 10/3/2019
 */
@ActiveProfiles("test-integration")
@SpringBootTest(classes = {
    LiquibaseProperties.class,

    MockSchemaRegistryConfig.class
})
@EmbeddedKafka(partitions = 1, topics = {"topic1"})
@EnableAutoConfiguration
@DirtiesContext
public class EmbeddedKafkaTestIT {

    @Autowired
    @Qualifier(value = EmbeddedKafkaBroker.BEAN_NAME)
    private EmbeddedKafkaBroker embeddedKafkaBroker;

    @Autowired
    private KafkaAvroSerializer kafkaAvroSerializer;
    @Autowired
    private KafkaAvroDeserializer kafkaAvroDeserializer;

    @Autowired
    private KafkaProperties kafkaProperties;

    private KafkaTemplate<String, GenericAvroMessage> template;

    @BeforeEach
    void setUp() {
        // creates template to use mocked serializer
        // set up the Kafka producer properties
        Map<String, Object> senderProperties = KafkaTestUtils.senderProps(embeddedKafkaBroker.getBrokersAsString());
        // create a Kafka producer factory
        DefaultKafkaProducerFactory producerFactory = new DefaultKafkaProducerFactory(senderProperties);
        producerFactory.setKeySerializer(kafkaAvroSerializer);
        producerFactory.setValueSerializer(kafkaAvroSerializer);

        // create a Kafka template
        template = new KafkaTemplate<>(producerFactory);
        // set the default topic to send to
        template.setDefaultTopic("topic1");

    }

    @Test
    public void exampleTest() {
        // Given
        GenericAvroMessage message = new GenericAvroMessage();
        message.setPayload("Payload test");

        // When
        ListenableFuture<SendResult<String, GenericAvroMessage>> future = template.sendDefault(message);
        future.addCallback(new ListenableFutureCallback<>() {
            @Override
            public void onSuccess(SendResult<String, GenericAvroMessage> result) {
                assertEquals(0, result.getRecordMetadata().offset());
            }

            @Override
            public void onFailure(Throwable ex) {
            }
        });

        Properties consumerProperties = new Properties();
        consumerProperties.putAll(kafkaProperties.buildConsumerProperties());

        // Read
        KafkaRecordItemReaderBuilder<ConsumerRecord<Long, GenericAvroMessage>> builder = new KafkaRecordItemReaderBuilder<ConsumerRecord<Long, GenericAvroMessage>>()
            .name("reader")
            .consumerProperties(consumerProperties)
            .partitions(0)
            .keyDeserializer(kafkaAvroDeserializer)
            .valueDeserializer(kafkaAvroDeserializer)
            .topic("topic1");

        KafkaRecordItemReader<ConsumerRecord<Long, GenericAvroMessage>> reader = builder.build();
        reader.setPollTimeout(Duration.ofSeconds(1));
        reader.open(new ExecutionContext());

        ConsumerRecord<Long, GenericAvroMessage> message2 = reader.read();

        assertEquals(message, message2.value());

    }
}
