package com.suibian.cofig;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "info")
public class SwaggerProperties {

    // swagger 配置
    private String title;
    private String version;
    private String description;
    private String contactName;
    private String contactEmail;
}
