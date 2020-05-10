package com.lt.bootrbac.utils;


import com.lt.bootrbac.contants.Constant;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.StringUtils;

import javax.xml.bind.DatatypeConverter;
import java.time.Duration;
import java.util.Date;
import java.util.Map;

@Slf4j
public class JwtTokenUtil {

    //密钥
    private static String securityKey;
    //过期时间
    private static Duration accessTokenExpireTime;
    //刷新时间
    private static Duration refreshTokenExpireTime;
    //app刷新时间
    private static Duration refreshTokenExpireAppTime;
    //签发人
    private static String issuer;

    public static void setJwtProperties(TokenSetting tokenSetting) {
        securityKey = tokenSetting.getSecurityKey();
        accessTokenExpireTime = tokenSetting.getAccessTokenExpireTime();
        refreshTokenExpireTime = tokenSetting.getRefreshTokenExpireTime();
        refreshTokenExpireAppTime = tokenSetting.getRefreshTokenExpireAppTime();
        issuer = tokenSetting.getIssuer();
    }

    /**
     * 生成 access_token
     *
     * @param subject
     * @param claims
     * @return java.lang.String
     * @throws
     */
    public static String getAccessToken(String subject, Map<String, Object> claims) {

        return generateToken(issuer, subject, claims, accessTokenExpireTime.toMillis(), securityKey);
    }

    /**
     * 签发token
     *
     * @param issuer    签发人
     * @param subject   代表这个JWT的主体，即它的所有人 一般是用户id
     * @param claims    存储在JWT里面的信息 一般放些用户的权限/角色信息
     * @param ttlMillis 有效时间(毫秒)
     * @return java.lang.String
     */
    public static String generateToken(String issuer, String subject, Map<String, Object> claims, long ttlMillis, String security) {

        //使用HS256的算法
        SignatureAlgorithm signatureAlgorithm = SignatureAlgorithm.HS256;

        long nowMillis = System.currentTimeMillis();
        Date now = new Date(nowMillis);

        //将security使用Base64加密
        byte[] signingKey = DatatypeConverter.parseBase64Binary(security);

        JwtBuilder builder = Jwts.builder();
        if (null != claims) {
            builder.setClaims(claims);
        }
        if (!StringUtils.isEmpty(subject)) {
            builder.setSubject(subject);
        }
        if (!StringUtils.isEmpty(issuer)) {
            builder.setIssuer(issuer);
        }
        builder.setIssuedAt(now);
        if (ttlMillis >= 0) {
            long expMillis = nowMillis + ttlMillis;
            Date exp = new Date(expMillis);
            builder.setExpiration(exp);
        }

        builder.signWith(signatureAlgorithm, signingKey);
        //组合
        return builder.compact();
    }

    // 上面我们已经有生成 access_token 的方法，下面加入生成 refresh_token 的方法(PC 端过期时间短一些)

    /**
     * 生产 PC refresh_token
     *
     * @param subject
     * @param claims
     * @return java.lang.String
     * @throws
     */
    public static String getRefreshToken(String subject, Map<String, Object> claims) {
        return generateToken(issuer, subject, claims, refreshTokenExpireTime.toMillis(), securityKey);
    }

    /**
     * 生产 App端 refresh_token
     *
     * @param subject
     * @param claims
     * @return java.lang.String
     */
    public static String getRefreshAppToken(String subject, Map<String, Object> claims) {
        return generateToken(issuer, subject, claims, refreshTokenExpireAppTime.toMillis(), securityKey);
    }

    /**
     * 从令牌中获取数据声明
     *
     * @param token
     * @return io.jsonwebtoken.Claims
     * @throws
     */
    public static Claims getClaimsFromToken(String token) {
        Claims claims;
        try {
            claims = Jwts.parser().setSigningKey(DatatypeConverter.parseBase64Binary(securityKey)).parseClaimsJws(token).getBody();
        } catch (Exception e) {
            claims = null;
        }
        return claims;
    }

    /**
     * 获取用户id
     *
     * @param token
     * @return java.lang.String
     * @throws
     */
    public static String getUserId(String token) {
        String userId = null;
        try {
            Claims claims = getClaimsFromToken(token);
            userId = claims.getSubject();
        } catch (Exception e) {
            log.error("eror={}", e);
        }
        return userId;
    }

    /**
     * 获取用户名
     *
     * @param token
     * @return java.lang.String
     * @throws
     */
    public static String getUserName(String token) {

        String username = null;
        try {
            Claims claims = getClaimsFromToken(token);
            username = (String) claims.get(Constant.JWT_USER_NAME);
        } catch (Exception e) {
            log.error("eror={}", e);
        }
        return username;
    }

    /**
     * 验证token 是否过期
     *
     * @param token
     * @param
     * @return java.lang.Boolean
     * @throws
     */
    public static Boolean isTokenExpired(String token) {

        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("error={}", e);
            return true;
        }
    }

    /**
     * 校验令牌（true:校验通过 false:验证失败）
     *
     * @param token
     * @return java.lang.Boolean
     * @throws
     */
    public static Boolean validateToken(String token) {
        Claims claimsFromToken = getClaimsFromToken(token);
        return (null != claimsFromToken && !isTokenExpired(token));
    }

    /**
     * 刷新token
     *
     * @param refreshToken
     * @param claims       主动去刷新的时候 改变JWT payload 内的信息
     * @return java.lang.String
     * @throws
     */
    public static String refreshToken(String refreshToken, Map<String, Object> claims) {
        String refreshedToken;
        try {
            Claims parserclaims = getClaimsFromToken(refreshToken);
            /**
             * 刷新token的时候如果为空说明原先的 用户信息不变 所以就引用上个token里的内容
             */
            if (null == claims) {
                claims = parserclaims;
            }
            refreshedToken = generateToken(parserclaims.getIssuer(), parserclaims.getSubject(), claims, accessTokenExpireTime.toMillis(), securityKey);
        } catch (Exception e) {
            refreshedToken = null;
            log.error("error={}", e);
        }
        return refreshedToken;
    }

    /**
     * 获取token的剩余过期时间
     *
     * @param token
     * @param
     * @return long
     * @throws
     */
    public static long getRemainingTime(String token) {
        long result = 0;
        try {
            long nowMillis = System.currentTimeMillis();
            result = getClaimsFromToken(token).getExpiration().getTime() - nowMillis;
        } catch (Exception e) {
            log.error("error={}", e);
        }
        return result;
    }
}
