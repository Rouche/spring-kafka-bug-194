package ms.kafkatest.utils;

import org.springframework.util.Assert;

import ms.kafkatest.messaging.response.AsyncResponse;

public abstract class AbstractAsyncErrorException extends RuntimeException {

    private AsyncResponse response;

    public AbstractAsyncErrorException(AsyncResponse response, String logMessage) {
        super(logMessage);
        Assert.notNull(response, "The response cannot be null");
        this.response = response;
    }

    public AsyncResponse getResponse() {
        return this.response;
    }
}
