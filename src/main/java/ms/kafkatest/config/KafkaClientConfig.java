package ms.kafkatest.config;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import java.util.ArrayList;
import java.util.List;

import ms.kafkatest.utils.TopicConfig;
import ms.kafkatest.utils.TopicConfigService;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

@Configuration
@Profile({"prod", "dev"})
public class KafkaClientConfig implements InitializingBean {

    private final BasicSchemaRegistryClient schemaRegistryClient;
    private final List<TopicConfigService> topicConfigServices;
    private final AdminClient adminClient;


    @Autowired
    public KafkaClientConfig(final BasicSchemaRegistryClient schemaRegistryClient,
                             final List<TopicConfigService> topicConfigServices,
                             final AdminClient adminClient) {
        this.schemaRegistryClient = schemaRegistryClient;
        this.topicConfigServices = topicConfigServices;
        this.adminClient = adminClient;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        List<NewTopic> newTopics = new ArrayList<>();
        for (TopicConfigService topicConfigLists : topicConfigServices) {
            for (TopicConfig topicConfig : topicConfigLists.getTopicConfigList()) {
                newTopics.add(
                    createTopic(
                        topicConfig.getTopicName(),
                        topicConfig.getKeySerializerSchema(),
                        topicConfig.getValueSerializerSchema()
                    )
                );
                newTopics.add(
                    createTopic(
                        topicConfig.getErrorTopicName(),
                        topicConfig.getKeySerializerSchema(),
                        TopicConfig.getErrorValueSerializerSchema()
                    )
                );
            }
        }
        this.adminClient.createTopics(newTopics);
        this.adminClient.close();
    }


    private NewTopic createTopic(String topicName, String keySchema, String valueSchema) {
        if (isNotBlank(keySchema)) {
            schemaRegistryClient.associateKeySchemaToTopic(keySchema, topicName);
        }
        if (isNotBlank(valueSchema)) {
            schemaRegistryClient.associateValueSchemaToTopic(valueSchema, topicName);
        }
        return createTopic(topicName);
    }

    private NewTopic createTopic(String topicName) {
        return new NewTopic(topicName, 1, (short) 1);
    }
}
