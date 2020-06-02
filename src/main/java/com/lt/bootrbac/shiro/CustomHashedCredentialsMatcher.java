package com.lt.bootrbac.shiro;

import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.service.RedisService;
import com.lt.bootrbac.utils.JwtTokenUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.HashedCredentialsMatcher;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.concurrent.TimeUnit;

/**
 * 可以自己实现 CredentialsMatcher 的一个类来实现定制化的账户密码验证机制
 */
@Slf4j
public class CustomHashedCredentialsMatcher extends HashedCredentialsMatcher {

    @Autowired
    private RedisService redisService;

    @Override
    public boolean doCredentialsMatch(AuthenticationToken token, AuthenticationInfo info) {
        CustomUsernamePasswordToken customUsernamePasswordToken = (CustomUsernamePasswordToken) token;
        String accessToken = (String) customUsernamePasswordToken.getCredentials();
        String userId = JwtTokenUtil.getUserId(accessToken);
        log.info("doCredentialsMatch....userId={}", userId);


        /**
         * 判断用户是否被删除
         */
        if (redisService.hasKey(Constant.DELETED_USER_KEY + userId)) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_HAS_DELETED_ERROR);
        }


        /**
         * 判断是否被锁定
         */
        if (redisService.hasKey(Constant.ACCOUNT_LOCK_KEY + userId)) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_LOCK);
        }

        /**
         * 判断用户是否退出登陆
         */
        if (redisService.hasKey(Constant.JWT_ACCESS_TOKEN_BLACKLIST + accessToken)) {
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }


        /**
         * 校验token
         */
        if (!JwtTokenUtil.validateToken(accessToken)) {
            throw new BusinessException(BaseResponseCode.TOKEN_PAST_DUE);
        }

        /**
         * 判断这个登录用户是否要主动去刷新
         *
         * 如果key=Constant.JWT_REFRESH_KEY+userId大于accessToken说明accessToken不是重新生成的
         * 这样就要判断它是否刷新过了/或者是否新生成的token
         */
        if (redisService.hasKey(Constant.JWT_REFRESH_KEY + userId)) {
            /**
             * 判断用户是否已经刷新过
             */
            if (redisService.getExpire(Constant.JWT_REFRESH_KEY + userId, TimeUnit.MILLISECONDS) > JwtTokenUtil.getRemainingTime(accessToken)) {
                throw new BusinessException(BaseResponseCode.TOKEN_PAST_DUE);
            }
        }

        return true;
    }
}
