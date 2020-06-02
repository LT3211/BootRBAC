package com.lt.bootrbac.service.impl;

import com.github.pagehelper.PageHelper;
import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.entity.SysDept;
import com.lt.bootrbac.entity.SysUser;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.mapper.SysDeptMapper;
import com.lt.bootrbac.mapper.SysUserMapper;
import com.lt.bootrbac.service.*;
import com.lt.bootrbac.utils.JwtTokenUtil;
import com.lt.bootrbac.utils.PageUtil;
import com.lt.bootrbac.utils.PasswordUtils;
import com.lt.bootrbac.utils.TokenSetting;
import com.lt.bootrbac.vo.req.*;
import com.lt.bootrbac.vo.resp.LoginRespVO;
import com.lt.bootrbac.vo.resp.PageVO;
import com.lt.bootrbac.vo.resp.UserOwnRoleRespVO;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private RedisService redisService;

    @Autowired
    private SysDeptMapper sysDeptMapper;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private TokenSetting tokenSetting;

    @Autowired
    private PermissionService permissionService;


    @Override
    public LoginRespVO login(LoginReqVO vo) {
        SysUser sysUser = sysUserMapper.getUserInfoByName(vo.getUsername());
        if (sysUser == null) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_ERROR);
        }
        //用户被锁定
        if (sysUser.getStatus() == 2) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_LOCK_TIP);
        }
        //用户账号密码不匹配
        if (!PasswordUtils.matches(sysUser.getSalt(), vo.getPassword(), sysUser.getPassword())) {
            throw new BusinessException(BaseResponseCode.ACCOUNT_PASSWORD_ERROR);
        }
        LoginRespVO respVO = new LoginRespVO();
        BeanUtils.copyProperties(sysUser, respVO);

        HashMap<String, Object> claims = new HashMap<>();
        //获得拥有的权限标识符
        claims.put(Constant.JWT_PERMISSIONS_KEY, getPermissionsByUserId(sysUser.getId()));
        //获得拥有的角色名称
        claims.put(Constant.JWT_ROLES_KEY, getRolesByUserId(sysUser.getId()));
        claims.put(Constant.JWT_USER_NAME, sysUser.getUsername());

        //获得accessToken
        String accessToken = JwtTokenUtil.getAccessToken(sysUser.getId(), claims);

        //获得刷新token
        String refreshToken = null;
        HashMap<String, Object> refreshTokenClaims = new HashMap<>();
        refreshTokenClaims.put(Constant.JWT_USER_NAME, sysUser.getUsername());
        if (vo.getType().equals("1")) {
            refreshToken = JwtTokenUtil.getRefreshToken(sysUser.getId(), refreshTokenClaims);
        } else {
            refreshToken = JwtTokenUtil.getRefreshAppToken(sysUser.getId(), refreshTokenClaims);
        }

        respVO.setAccessToken(accessToken);
        respVO.setRefreshToken(refreshToken);
        return respVO;
    }

    /**
     * 分页查询用户信息
     *
     * @param vo
     * @return
     */
    @Override
    public PageVO<SysUser> pageInfo(UserPageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        List<SysUser> sysUsers = sysUserMapper.selectAll(vo);
        for (SysUser sysUser : sysUsers) {
            SysDept sysDept = sysDeptMapper.selectByPrimaryKey(sysUser.getDeptId());
            if (sysDept != null) {
                sysUser.setDeptName(sysDept.getName());
            }
        }
        return PageUtil.getPageVO(sysUsers);
    }


    /**
     * mock 数据
     * 通过用户id获取该用户所拥有的角色
     * 后期修改为通过操作DB获取
     *
     * @param userId
     * @return java.util.List<java.lang.String>
     * @throws
     * @Author: 小霍
     * @UpdateUser:
     * @Version: 0.0.1
     */
    private List<String> getRolesByUserId(String userId) {
        return roleService.getNamesByUserId(userId);
    }

    /**
     * mock 数据
     * 通过用户id获取该用户所拥有的角色
     * 后期通过操作数据获取
     *
     * @param userId
     * @return java.util.List<java.lang.String>
     * @throws
     * @Author: 小霍
     * @UpdateUser:
     * @Version: 0.0.1
     */
    private List<String> getPermissionsByUserId(String userId) {
        return permissionService.getPermissionByUserId(userId);
    }

    /**
     * 新增用户接口
     * @param vo
     */
    @Override
    public void addUser(UserAddReqVO vo) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setId(UUID.randomUUID().toString());
        sysUser.setCreateTime(new Date());
        String salt = PasswordUtils.getSalt();
        String ecdPwd = PasswordUtils.encode(vo.getPassword(), salt);
        sysUser.setSalt(salt);
        sysUser.setPassword(ecdPwd);
        int i = sysUserMapper.insertSelective(sysUser);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public UserOwnRoleRespVO getUserOwnRole(String userId) {
        UserOwnRoleRespVO respVO = new UserOwnRoleRespVO();
        respVO.setOwnRoles(userRoleService.getRoleIdsByUserId(userId));
        respVO.setAllRole(roleService.selectAll());
        return respVO;
    }

    @Override
    public void setUserOwnRole(UserOwnRoleReqVO vo) {
        userRoleService.addUserRoleInfo(vo);
        /**
         * 标记用户 要主动去刷新
         */
        redisService.set(Constant.JWT_REFRESH_KEY + vo.getUserId(),
                vo.getUserId(), tokenSetting.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);

        /**
         * 删除用户缓存信息
         */
        redisService.delete(Constant.IDENTIFY_CACHE_KEY + vo.getUserId());
    }

    @Override
    public String refreshToken(String refreshToken) {
        //校验这个刷新token是否有效
        //校验刷新token是否呗加入黑名单
        if (!JwtTokenUtil.validateToken(refreshToken) || redisService.hasKey(Constant.JWT_REFRESH_TOKEN_BLACKLIST + refreshToken)) {
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }
        String userId = JwtTokenUtil.getUserId(refreshToken);
        String userName = JwtTokenUtil.getUserName(refreshToken);

        Map<String, Object> claims = new HashMap<>();
        claims.put(Constant.JWT_USER_NAME, userName);
        claims.put(Constant.JWT_ROLES_KEY, getRolesByUserId(userId));
        claims.put(Constant.JWT_PERMISSIONS_KEY, getPermissionsByUserId(userId));

        String newAccessToken = JwtTokenUtil.getAccessToken(userId, claims);

        return newAccessToken;
    }

    @Override
    public void updateUserInfo(UserUpdateReqVO vo, String operationId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(operationId);

        if (StringUtils.isEmpty(vo.getPassword())) {
            sysUser.setPassword(null);
        } else {
            String salt = PasswordUtils.getSalt();
            String endPwd = PasswordUtils.encode(vo.getPassword(), salt);
            sysUser.setSalt(salt);
            sysUser.setPassword(endPwd);
        }

        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);

        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        if (vo.getStatus() == 2) {
            redisService.set(Constant.ACCOUNT_LOCK_KEY + vo.getId(), vo.getId());
        } else {
            redisService.delete(Constant.ACCOUNT_LOCK_KEY + vo.getId());
        }
    }

    @Override
    public void deletedUsers(List<String> list, String operationId) {
        SysUser sysUser = new SysUser();
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(operationId);
        int i = sysUserMapper.deletedUsers(sysUser, list);
        if (i == 0) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        for (String userId : list) {
            redisService.set(Constant.DELETED_USER_KEY + userId, userId,
                    tokenSetting.getRefreshTokenExpireAppTime().toMillis(), TimeUnit.MILLISECONDS);

            /**
             * 删除用户缓存信息
             */
            redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
        }
    }

    @Override
    public List<SysUser> selectUserInfoByDeptIds(List<String> deptIds) {
        return sysUserMapper.selectUserInfoByDeptIds(deptIds);
    }

    @Override
    public void logout(String accessToken, String refreshToken) {
        if (StringUtils.isEmpty(accessToken) || StringUtils.isEmpty(refreshToken)) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }

        //获取主体，主体登出
        Subject subject = SecurityUtils.getSubject();
        if (subject != null) {
            subject.logout();
        }

        String userId = JwtTokenUtil.getUserId(accessToken);

        /**
         * 把accessToken加入黑名单，设置过期时间为Token的剩余时间
         */
        redisService.set(Constant.JWT_ACCESS_TOKEN_BLACKLIST + accessToken, userId, JwtTokenUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);

        /**
         * 把refreshToken加入黑名单，设置过期时间为Token的剩余时间
         */
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST + accessToken, userId, JwtTokenUtil.getRemainingTime(refreshToken), TimeUnit.MILLISECONDS);

    }

    @Override
    public SysUser detailInfo(String userId) {
        return sysUserMapper.selectByPrimaryKey(userId);
    }

    @Override
    public void userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo, String userId) {
        SysUser sysUser = new SysUser();
        BeanUtils.copyProperties(vo, sysUser);
        sysUser.setId(userId);
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(userId);
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public void userUpdatePwd(UserUpdatePwdReqVO vo, String accessToken, String refreshToken) {
        String userId = JwtTokenUtil.getUserId(accessToken);
        //校验旧密码
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        if (sysUser == null) {
            throw new BusinessException(BaseResponseCode.TOKEN_ERROR);
        }

        if (!PasswordUtils.matches(sysUser.getSalt(), vo.getOldPwd(), sysUser.getPassword())) {
            throw new BusinessException(BaseResponseCode.OLD_PASSWORD_ERROR);
        }

        //保存新密码
        sysUser.setUpdateTime(new Date());
        sysUser.setUpdateId(userId);
        sysUser.setPassword(PasswordUtils.encode(vo.getNewPwd(), sysUser.getSalt()));
        int i = sysUserMapper.updateByPrimaryKeySelective(sysUser);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
        /**
         * 把accessToken加入黑名单 禁止再访问我们的系统资源
         */
        redisService.set(Constant.JWT_ACCESS_TOKEN_BLACKLIST + accessToken, userId, JwtTokenUtil.getRemainingTime(accessToken), TimeUnit.MILLISECONDS);

        /**
         * 把refreshToken加入黑名单 禁止再拿来刷新token
         */
        redisService.set(Constant.JWT_REFRESH_TOKEN_BLACKLIST + refreshToken, userId, JwtTokenUtil.getRemainingTime(refreshToken), TimeUnit.MILLISECONDS);

        /**
         * 删除用户缓存信息
         */
        redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
    }
}
