package ms.kafkatest.service.errors;

import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class KafkaStringErrorProducer {

    private KafkaTemplate<String, String> errorProducer;

    KafkaStringErrorProducer(KafkaTemplate<String, String> errorProducer) {
        this.errorProducer = errorProducer;
    }

    public ListenableFuture<SendResult<String, String>> sendRecordInError(ProducerRecord<String, String> record) {
        log.info("Sending message to topic {}", record.topic());
        return this.errorProducer.send(record);
    }
}
