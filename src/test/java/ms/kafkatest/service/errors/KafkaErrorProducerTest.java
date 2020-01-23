package ms.kafkatest.service.errors;

import java.time.Duration;
import java.util.Properties;
import java.util.concurrent.ExecutionException;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.util.concurrent.ListenableFuture;

import io.confluent.kafka.serializers.KafkaAvroDeserializer;
import ms.kafkatest.avro.KeyAvro;
import ms.kafkatest.messaging.avro.GenericAvroMessage;
import ms.kafkatest.messaging.response.AsyncResponse;
import ms.kafkatest.utils.KafkaRecordItemReader;
import ms.kafkatest.utils.KafkaRecordItemReaderBuilder;
import ms.kafkatest.utils.MockSchemaRegistryConfig;

@EmbeddedKafka(partitions = 1, topics = {KafkaErrorProducerTest.TEST_TOPIC})
@EnableAutoConfiguration
@SpringBootTest(classes = {MockSchemaRegistryConfig.class})
@DirtiesContext
public class KafkaErrorProducerTest {

    protected static final String TEST_TOPIC = "test.error";

    @Autowired
    private DefaultKafkaProducerFactory defaultKafkaProducerFactory;
    @Autowired
    private KafkaAvroDeserializer kafkaAvroDeserializer;

    @Autowired
    private KafkaProperties kafkaProperties;

    private KafkaErrorProducer<KeyAvro> kafkaErrorProducer;

    @BeforeEach
    void init() {
        KafkaTemplate<KeyAvro, AsyncResponse> kafkaTemplate = new KafkaTemplate<>(defaultKafkaProducerFactory);
        kafkaErrorProducer = new KafkaErrorProducer<>(kafkaTemplate);
    }

    @Test
    void produceErrors() throws ExecutionException, InterruptedException {
        KeyAvro key = new KeyAvro("key");

        ListenableFuture<SendResult<KeyAvro, AsyncResponse>> result = kafkaErrorProducer.sendRecordInError(new ProducerRecord<>(TEST_TOPIC, key, TestAsyncResponses.systemError()));

        Assertions.assertNotNull(result.get());
        Assertions.assertTrue(result.isDone());
        Assertions.assertFalse(result.isCancelled());

        result = kafkaErrorProducer.sendRecordInError(new ProducerRecord<>(TEST_TOPIC, key, TestAsyncResponses.systemError()));

        Assertions.assertNotNull(result.get());
        Assertions.assertTrue(result.isDone());
        Assertions.assertFalse(result.isCancelled());

        Properties consumerProperties = new Properties();
        consumerProperties.putAll(kafkaProperties.buildConsumerProperties());

        // Read
        KafkaRecordItemReaderBuilder<ConsumerRecord<Long, GenericAvroMessage>> builder = new KafkaRecordItemReaderBuilder<ConsumerRecord<Long, GenericAvroMessage>>()
            .name("reader")
            .consumerProperties(consumerProperties)
            .partitions(0)
            .keyDeserializer(kafkaAvroDeserializer)
            .valueDeserializer(kafkaAvroDeserializer)
            .topic(TEST_TOPIC);

        KafkaRecordItemReader<ConsumerRecord<Long, GenericAvroMessage>> reader = builder.build();
        reader.setPollTimeout(Duration.ofSeconds(3));
        reader.open(new ExecutionContext());

        ConsumerRecord<Long, GenericAvroMessage> record = reader.read();

        Assertions.assertEquals(TEST_TOPIC, record.topic());
        Assertions.assertEquals(key, record.key());
        Assertions.assertEquals(TestAsyncResponses.systemError(), record.value());

        record = reader.read();

        Assertions.assertEquals(TEST_TOPIC, record.topic());
        Assertions.assertEquals(key, record.key());
        Assertions.assertEquals(TestAsyncResponses.systemError(), record.value());
    }
}
