package com.suibian.cofig;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnClass({OpenAPI.class})
@ConditionalOnProperty(prefix = "springdoc.api-docs", name = "enabled", havingValue = "true", matchIfMissing = true)
public class Knife4jConfig {

    @Bean
    public OpenAPI openApi(SwaggerProperties swaggerProperties) {
        return new OpenAPI()
                .info(new Info()
                        .title(swaggerProperties.getTitle())
                        .version(swaggerProperties.getVersion())
                        .description(swaggerProperties.getDescription())
                        .contact(new Contact()
                                .name(swaggerProperties.getContactName())
                                .email(swaggerProperties.getContactEmail()))
                        );
    }
}
