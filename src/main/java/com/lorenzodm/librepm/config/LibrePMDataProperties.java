package com.lorenzodm.librepm.config;

import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

@Validated
@ConfigurationProperties(prefix = "librepm.data")
public class LibrePMDataProperties {

    @NotBlank
    private String path = "./data";

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
