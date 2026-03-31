package com.lorenzodm.librepm.config;

import com.lorenzodm.librepm.security.AuthProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
        LibrePMDataProperties.class,
        LibrePMAssetsProperties.class,
        CorsProperties.class,
        AuthProperties.class
})
public class LibrePMPropertiesConfig {
}
