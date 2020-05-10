package com.lt.bootrbac.shiro;

import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.service.PermissionService;
import com.lt.bootrbac.service.RedisService;
import com.lt.bootrbac.service.RoleService;
import com.lt.bootrbac.utils.JwtTokenUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 用户自定义域
 */
@Slf4j
public class CustomRealm extends AuthorizingRealm {

    @Autowired
    private RedisService redisService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PermissionService permissionService;

    /**
     * 开启token的支持
     *
     * @param token
     * @return
     */
    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof CustomUsernamePasswordToken;
    }

    /**
     * 用户授权调用
     *
     * @param principalCollection
     * @return
     */
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        String accessToken = (String) principalCollection.getPrimaryPrincipal();
        Claims claimsFromToken = JwtTokenUtil.getClaimsFromToken(accessToken);
        SimpleAuthorizationInfo info = new SimpleAuthorizationInfo();

        String userId = JwtTokenUtil.getUserId(accessToken);
        log.info("userId={}", userId);

        //如果redis中存在refreshKey,那么刷新用户token
        if (redisService.hasKey(Constant.JWT_REFRESH_KEY + userId) && redisService.getExpire(
                Constant.JWT_REFRESH_KEY + userId, TimeUnit.MILLISECONDS) > JwtTokenUtil.getRemainingTime(accessToken)) {
            List<String> roles = roleService.getNamesByUserId(userId);
            if (roles != null && !roles.isEmpty()) {
                info.addRoles(roles);
            }
            List<String> permissionByUserId = permissionService.getPermissionByUserId(userId);
            if (permissionByUserId != null && !permissionByUserId.isEmpty()) {
                info.addStringPermissions(permissionByUserId);
            }
        } else {
            //返回该用户权限色信息给授权器
            if (claimsFromToken.get(Constant.JWT_PERMISSIONS_KEY) != null) {
                info.addStringPermissions((Collection<String>) claimsFromToken.get(Constant.JWT_PERMISSIONS_KEY));
            }
            //返回该用户的角色信息给授权器
            if (claimsFromToken.get(Constant.JWT_ROLES_KEY) != null) {
                info.addRoles((Collection<String>) claimsFromToken.get(Constant.JWT_ROLES_KEY));
            }
        }

        return info;
    }

    /**
     * 用户认证调用
     *
     * @param authenticationToken
     * @return
     * @throws AuthenticationException
     */
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        CustomUsernamePasswordToken customUsernamePasswordToken = (CustomUsernamePasswordToken) authenticationToken;
        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(customUsernamePasswordToken.getPrincipal(), customUsernamePasswordToken.getCredentials(), this.getName());

        return info;
    }
}
