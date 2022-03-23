package com.imooc.utils.extend;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@Component
// tencentcloud.properties文件
/**
 * @PropertySource - Annotation providing a convenient and declarative mechanism
 *                  for adding a PropertySource to Spring's Environment
  */
@PropertySource("classpath:tencentcloud.properties")
/**
 * @ConfigurationProperties - Annotation for externalized configuration.
 *                      Add this to a class definition or a @Bean method in a @Configuration class if you want
 *                      to bind and validate some external Properties (e.g. from a .properties file).
 */
@ConfigurationProperties(prefix = "tencentcloud")
public class TencentCloudResource {
    private String secretID;
    private String secretKey;

    public String getSecretID() {
        return secretID;
    }

    public void setSecretID(String secretID) {
        this.secretID = secretID;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }
}
