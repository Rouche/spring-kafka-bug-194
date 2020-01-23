package ms.kafkatest.utils;

import lombok.*;

import ms.kafkatest.messaging.response.AsyncResponse;

/**
 * Stub utiliser pour enregistrer les topics Kafka.
 * <p>
 * L'absence de serializer signifie que la valeur sera une simple String.
 */
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PUBLIC)
@AllArgsConstructor(access = AccessLevel.PUBLIC)
public class TopicConfig {

    public static final String ERROR_SUFFIX = ".error";
    private static final String ERROR_VALUE_SERIALIZER_SCHEMA = AsyncResponse.getClassSchema().toString();

    private String topicName;
    private String keySerializerSchema;
    private String valueSerializerSchema;

    public String getTopicName(){
        return topicName;
    }

    public String getErrorTopicName() {
        return getTopicName() + ERROR_SUFFIX;
    }

    public static String getErrorValueSerializerSchema() {
        return ERROR_VALUE_SERIALIZER_SCHEMA;
    }

    public String getOriginalTopicName(){
        return topicName;
    }
}
