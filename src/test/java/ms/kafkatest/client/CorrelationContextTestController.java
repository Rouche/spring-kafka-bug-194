package ms.kafkatest.client;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CorrelationContextTestController {
    @GetMapping("/test/correlation-context")
    public void concurrencyFailure() {
        // Test Controller utilisé pour CorrelationContextInterceptor.
        // Wire mock non-utilisé car le test prend des fonctionnalité spring requise (restTemplate interceptor)
    }
}
