package com.pavelkostal.aiwithjava.utils;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.cloud.azure.cosmos")
@Getter
@Setter
public class CosmosProperties {
    private String key;
    private String database;
}
