package ms.kafkatest.config.swagger;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.github.jhipster.config.JHipsterProperties;
import io.github.jhipster.config.apidoc.customizer.SwaggerCustomizer;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiKey;
import springfox.documentation.service.AuthorizationScope;
import springfox.documentation.service.SecurityReference;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger.web.ApiKeyVehicle;


public class ApplicationSwaggerCustomizer implements SwaggerCustomizer {

    private final Logger log = LoggerFactory.getLogger(ApplicationSwaggerCustomizer.class);
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String API_KEY = "bearer";

    private final JHipsterProperties.Swagger properties;

    public ApplicationSwaggerCustomizer(JHipsterProperties jHipsterProperties) {
        this.properties = jHipsterProperties.getSwagger();
    }

    @Override
    public void customize(Docket docket) {
        log.debug("Customizing springfox docket...");
        docket.host(properties.getHost())

            .securitySchemes(Arrays.asList(apiKey()))
            .securityContexts(Arrays.asList(
                SecurityContext.builder()
                    .securityReferences(
                        Arrays.asList(SecurityReference.builder()
                            .reference(API_KEY)
                            .scopes(new AuthorizationScope[0])
                            .build()
                        )
                    )
                    .build())
            );

        docket.globalOperationParameters(Arrays.asList(
            new ParameterBuilder()
                .name("user_id")
                .description("User initiating the request")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(true)
                .defaultValue("A")
                .build()
        ));
    }

    private ApiKey apiKey() {
        return new ApiKey(API_KEY, AUTHORIZATION_HEADER, ApiKeyVehicle.HEADER.getValue());
    }
}
