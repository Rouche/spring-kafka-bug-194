package ms.kafkatest.utils;

import ms.kafkatest.messaging.response.AsyncResponse;

public class AsyncSystemErrorException extends AbstractAsyncErrorException {

    public AsyncSystemErrorException(AsyncResponse reply, String logMessage) {
        super(reply, logMessage);
    }
}
