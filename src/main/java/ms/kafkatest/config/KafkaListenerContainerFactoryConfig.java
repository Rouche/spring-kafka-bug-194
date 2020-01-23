package ms.kafkatest.config;

import org.apache.avro.specific.SpecificRecordBase;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaListenerContainerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

import ms.kafkatest.service.errors.AsyncErrorHandler;

@Configuration
@EnableKafka
public class KafkaListenerContainerFactoryConfig {

    @Bean
    public <T, U extends SpecificRecordBase> KafkaListenerContainerFactory kafkaListenerContainerFactory(
        AsyncErrorHandler asyncErrorHandler,
        KafkaProperties kafkaProperties)
    {
        ConcurrentKafkaListenerContainerFactory<T, U> factory = new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(new DefaultKafkaConsumerFactory<>(kafkaProperties.buildConsumerProperties()));
        factory.setErrorHandler(asyncErrorHandler);
        return factory;
    }
}

