package ms.kafkatest.service.errors;


import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.listener.ErrorHandler;
import org.springframework.stereotype.Component;

import java.util.Collections;

import ms.kafkatest.messaging.response.AsyncError;
import ms.kafkatest.messaging.response.AsyncResponse;
import ms.kafkatest.utils.AbstractAsyncErrorException;
import ms.kafkatest.utils.AsyncSystemErrorException;
import ms.kafkatest.utils.KafkaHelper;
import ms.kafkatest.utils.TopicConfig;

import static java.nio.charset.StandardCharsets.UTF_8;

@Component("AsyncErrorHandler")
public class AsyncErrorHandler implements ErrorHandler {

    private final Logger logger = LoggerFactory.getLogger(AsyncErrorHandler.class);

    private final KafkaErrorProducer errorProducer;

    @Autowired
    public AsyncErrorHandler(KafkaErrorProducer errorProducer) {
        this.errorProducer = errorProducer;
    }

    @Override
    @SuppressWarnings("FutureReturnValueIgnored")
    public void handle(Exception thrownException, ConsumerRecord<?, ?> data) {
        if (isInstanceOfOrCausedBy(thrownException, AsyncSystemErrorException.class)) {
            AsyncSystemErrorException ex = castExceptionAs(thrownException, AsyncSystemErrorException.class);
            this.errorProducer.sendRecordInError(KafkaHelper.createRecordWithHeaders(data.topic() + TopicConfig.ERROR_SUFFIX, data.key(), ex.getResponse(), data.headers().add("STATUS_CODE_HEADER", "STATUS_CODE_SYSTEM".getBytes(UTF_8))));
            logError(data, ex);
        } else {
            AsyncResponse error = buildUnhandledErrorResponse(thrownException);
            this.errorProducer.sendRecordInError(KafkaHelper.createRecordWithHeaders(data.topic() + TopicConfig.ERROR_SUFFIX, data.key(), error, data.headers().add("STATUS_CODE_HEADER", "STATUS_CODE_SYSTEM".getBytes(UTF_8))));
            logError(data, thrownException);
        }
    }

    private static <E extends AbstractAsyncErrorException> boolean isInstanceOfOrCausedBy(Throwable ex, Class<E> exceptionClass) {
        if (exceptionClass.isInstance(ex)) return true;
        Throwable cause = ex;
        while (cause.getCause() != null) {
            if (exceptionClass.isInstance(cause.getCause())) return true;
            cause = cause.getCause();
        }
        return false;
    }

    private static <E extends AbstractAsyncErrorException> E castExceptionAs(Exception ex, Class<E> exceptionClass) {
        Throwable toReturn = ex;
        while (!exceptionClass.isInstance(toReturn)) {
            toReturn = toReturn.getCause();
        }
        return exceptionClass.cast(toReturn);
    }

    private static AsyncResponse buildUnhandledErrorResponse(Exception ex) {
        return AsyncResponse.newBuilder()
            .setErrors(Collections.singletonList(AsyncError.newBuilder()
                .setCode("500")
                .setMessage("Error")
                .setDescription(ex.getMessage())
                .build()))
            .build();
    }

    private void logError(ConsumerRecord<?, ?> record, Exception ex) {
        logger.error("Error while processing: {}", record, ex);
    }
}
