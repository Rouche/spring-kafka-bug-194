package ms.kafkatest.service.errors;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Component;
import org.springframework.util.concurrent.ListenableFuture;
import lombok.extern.slf4j.Slf4j;

import ms.kafkatest.messaging.response.AsyncResponse;

@Slf4j
@Component
public class KafkaErrorProducer<T extends SpecificRecordBase> {

    private KafkaTemplate<T, AsyncResponse> errorProducer;

    KafkaErrorProducer(KafkaTemplate<T, AsyncResponse> errorProducer){
        this.errorProducer = errorProducer;
    }

    public ListenableFuture<SendResult<T, AsyncResponse>> sendRecordInError(ProducerRecord<T, AsyncResponse> record) {
        log.info("Sending message to topic {}", record.topic());
        return this.errorProducer.send(record);
    }
}
