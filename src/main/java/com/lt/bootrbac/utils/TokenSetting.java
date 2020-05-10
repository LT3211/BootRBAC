package com.lt.bootrbac.utils;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * 注入静态变量
 */
@Configuration
@ConfigurationProperties(prefix = "jwt")
@Data
public class TokenSetting {
    private  String securityKey;
    private  Duration accessTokenExpireTime;
    private  Duration refreshTokenExpireTime;
    private  Duration refreshTokenExpireAppTime;
    private  String issuer;
}
