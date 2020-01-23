package ms.kafkatest.service.errors;

import java.util.Collections;

import ms.kafkatest.messaging.response.AsyncError;
import ms.kafkatest.messaging.response.AsyncResponse;

public class TestAsyncResponses {
    public static AsyncResponse systemError() {
        return AsyncResponse.newBuilder()
            .setErrors(
                Collections.singletonList(
                    AsyncError.newBuilder()
                        .setCode("415")
                        .setMessage("415_Message")
                        .build()))
            .build();
    }
}
