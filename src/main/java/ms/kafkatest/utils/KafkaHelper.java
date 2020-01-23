package ms.kafkatest.utils;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.Header;
import org.apache.kafka.common.header.Headers;

import lombok.experimental.UtilityClass;

import ms.kafkatest.messaging.response.AsyncResponse;

@UtilityClass
public final class KafkaHelper {

    public static final String MESSAGE_ID_HEADER = "MessageId";
    public static final String PUBLIC_ID_HEADER = "PublicId";
    public static final String WARNINGS_HEADER = "Warnings";

    public static <T, U extends SpecificRecordBase> ProducerRecord<T, U> createRecordWithHeaders(String topic, T key, U value, Headers headers) {
        ProducerRecord<T, U> record = new ProducerRecord<>(topic, key, value);
        headers.forEach(header -> record.headers().add(header));
        return record;
    }

    public static <T, U extends SpecificRecordBase> ProducerRecord<T, U> createRecordWithWarnings(String topic, T key, U value, Headers headers, AsyncResponse warnings) throws IOException {
        ProducerRecord<T, U> record = createRecordWithHeaders(topic, key, value, headers);
        record.headers().add(WARNINGS_HEADER, warnings.toByteBuffer().array());
        return record;
    }

    public static AsyncResponse getWarningsFromHeaders(Headers headers) throws BadWarningSerializationException {
        AsyncResponse warnings = null;
        Header warningHeader = headers.lastHeader(WARNINGS_HEADER);
        if (warningHeader != null) {
            try {
                warnings = AsyncResponse.fromByteBuffer(ByteBuffer.wrap(warningHeader.value()));
            } catch (Exception ex) {
                throw new BadWarningSerializationException("Unable to recreate warning(s) from given values");
            }
        }
        return warnings;
    }

    static class BadWarningSerializationException extends Exception {
        public BadWarningSerializationException(String message) {
            super(message);
        }
    }
}
