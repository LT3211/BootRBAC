package com.lt.bootrbac.shiro;

import org.apache.shiro.authc.UsernamePasswordToken;

/**
 * 自定义token
 */
public class CustomUsernamePasswordToken extends UsernamePasswordToken {

    private String jwtToken;

    public CustomUsernamePasswordToken(String jwtToken) {
        this.jwtToken = jwtToken;
    }

    @Override
    public Object getPrincipal() {
        return jwtToken;
    }

    @Override
    public Object getCredentials() {
        return jwtToken;
    }
}
