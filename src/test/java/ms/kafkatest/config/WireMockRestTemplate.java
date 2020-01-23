package ms.kafkatest.config;

import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

/**
 * @author Jean-Francois Larouche (rouche) on 11/29/2019
 */
@Component
@Primary
@Profile("test-integration")
public class WireMockRestTemplate extends RestTemplate {

    public static final String WIREMOCK_PORT_PLACEHOLDER = "{port}";

    private String port;

    @Override
    public <T> ResponseEntity<T> postForEntity(String url, Object request, Class<T> responseType, Object... uriVariables) throws RestClientException {
        if(StringUtils.isNotBlank(port)) {
            url = url.replace(WIREMOCK_PORT_PLACEHOLDER, port);
        }

        return super.postForEntity(url, request, responseType, uriVariables);
    }

    public void setPort(int port) {
        this.port = Integer.toString(port);
    }
}
