package ms.kafkatest.config;

import java.util.Properties;

import org.apache.kafka.clients.admin.AdminClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminClientConfig {

    @Bean
    public AdminClient adminClient(@Value("${spring.kafka.bootstrap-servers}") String kafkaServer,
                                   @Value("${spring.application.name}") String msName) {
        Properties properties = new Properties();
        properties.put(org.apache.kafka.clients.admin.AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaServer);
        properties.put(org.apache.kafka.clients.admin.AdminClientConfig.CLIENT_ID_CONFIG, msName + "Client");
        return AdminClient.create(properties);
    }
}
