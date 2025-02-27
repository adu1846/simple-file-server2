package io.devin.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties
@ConfigurationProperties(prefix = "fileserver")
public class FileServerConfig {

    @Value("${fileserver.home}")
    private String home;

    public String getHome() {
        System.out.
        return home;
    }

    public void setHome(String home) {
        this.home = home;
    }
}
