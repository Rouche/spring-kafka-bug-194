package ms.kafkatest.utils;

import java.util.List;

/**
 * Interface a implementer pour l'enregistrement automatique des topics dans Kafka ainsi que (si disponible) des schemas
 * dans le Schema Registry.
 */
public interface TopicConfigService {

    List<TopicConfig> getTopicConfigList();
}
