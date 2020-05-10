package com.lt.bootrbac.controller;


import com.lt.bootrbac.aop.annotation.MyLog;
import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.entity.SysUser;
import com.lt.bootrbac.service.UserService;
import com.lt.bootrbac.utils.DataResult;
import com.lt.bootrbac.utils.JwtTokenUtil;
import com.lt.bootrbac.vo.req.*;
import com.lt.bootrbac.vo.resp.LoginRespVO;
import com.lt.bootrbac.vo.resp.PageVO;
import com.lt.bootrbac.vo.resp.UserOwnRoleRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.List;
import java.util.zip.DataFormatException;

@RestController
@Api(tags = "组织管理-用户管理", description = "用户模块相关接口")
@RequestMapping("/api")
@Slf4j
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/user/login")
    @ApiOperation(value = "用户登陆接口")
    public DataResult<LoginRespVO> login(@RequestBody @Valid LoginReqVO vo) {
        DataResult<LoginRespVO> result = DataResult.success(userService.login(vo));
        return result;
    }

    @PostMapping("/users")
    @ApiOperation(value = "分页获取用户列表接口")
    @RequiresPermissions("sys:user:list")
    @MyLog(title = "组织管理-用户管理", action = "分页查询用户接口")
    public DataResult<PageVO<SysUser>> pageInfo(@RequestBody UserPageReqVO vo) {
        DataResult<PageVO<SysUser>> result = DataResult.success(userService.pageInfo(vo));
        return result;
    }

    @PostMapping("/user")
    @ApiOperation(value = "新增用户接口")
    @RequiresPermissions("sys:user:add")
    @MyLog(title = "组织管理-用户管理", action = "新增用户接口")
    public DataResult addUser(@RequestBody @Valid UserAddReqVO vo) {
        DataResult result = DataResult.success();
        userService.addUser(vo);
        return result;
    }

    @GetMapping("/user/roles/{userId}")
    @ApiModelProperty(value = "查询用户拥有的角色数据接口")
    @MyLog(title = "组织管理-用户管理", action = "查询用户拥有的角色数据接口")
    @RequiresPermissions("sys:user:role:update")
    public DataResult<UserOwnRoleRespVO> getUserOwnRole(@PathVariable("userId") String userId) {
        DataResult result = DataResult.success();
        result.setData(userService.getUserOwnRole(userId));
        return result;
    }

    @PutMapping("/user/roles")
    @ApiOperation(value = "保存用户拥有的角色信息接口")
    @MyLog(title = "组织管理-用户管理", action = "保存用户拥有的角色信息接口")
    @RequiresPermissions("sys:user:role:update")
    public DataResult saveUserOwnRole(@RequestBody @Valid UserOwnRoleReqVO vo) {
        DataResult result = DataResult.success();
        userService.setUserOwnRole(vo);
        return result;
    }

    @GetMapping("/user/token")
    @ApiOperation(value = "刷新token接口")
    @MyLog(title = "组织管理-用户管理", action = "刷新token接口")
    public DataResult<String> refreshToken(HttpServletRequest request) {
        String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
        String s = userService.refreshToken(refreshToken);
        DataResult result = DataResult.success();
        result.setData(s);
        return result;
    }

    @PutMapping("/user")
    @ApiOperation(value = "列表修改用户信息接口")
    @MyLog(title = "组织管理-用户管理", action = "列表修改用户信息接口")
    @RequiresPermissions("sys:user:update")
    public DataResult updateUserInfo(@RequestBody @Valid UserUpdateReqVO vo, HttpServletRequest request) {
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);

        String userId = JwtTokenUtil.getUserId(accessToken);
        DataResult result = DataResult.success();
        userService.updateUserInfo(vo, userId);
        return result;

    }

    @DeleteMapping("/user")
    @ApiOperation(value = "批量/删除用户接口")
    @MyLog(title = "组织管理-用户管理", action = "批量/删除用户接口")
    @RequiresPermissions("sys:user:delete")
    public DataResult deletedUsers(@RequestBody @ApiParam(value = "用户id集合") List<String> list, HttpServletRequest request) {
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String operationId = JwtTokenUtil.getUserId(accessToken);
        userService.deletedUsers(list, operationId);
        DataResult result = DataResult.success();
        return result;
    }

    @GetMapping("/user/logout")
    @ApiOperation(value = "用户退出登陆")
    public DataResult logout(HttpServletRequest request) {
        try {
            String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
            String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
            userService.logout(accessToken, refreshToken);
        } catch (Exception e) {
            log.error("logout:{}", e);
        }
        return DataResult.success();
    }

    @GetMapping("/user/info")
    @ApiOperation(value = "用户信息详情接口")
    @MyLog(title = "组织管理-用户管理", action = "用户信息详情接口")
    public DataResult<SysUser> detailInfo(HttpServletRequest request) {
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String userId = JwtTokenUtil.getUserId(accessToken);
        DataResult result = DataResult.success();
        result.setData(userService.detailInfo(userId));
        return result;
    }

    @PutMapping("/user/info")
    @ApiOperation(value = "列表修改用户信息接口")
    @MyLog(title = "组织管理-用户管理", action = "列表修改用户信息接口")
    @RequiresPermissions("sys:user:update")
    public DataResult saveUserInfo(@RequestBody UserUpdateDetailInfoReqVO vo, HttpServletRequest request) {
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);

        String userId = JwtTokenUtil.getUserId(accessToken);
        userService.userUpdateDetailInfo(vo, userId);
        DataResult result = DataResult.success();
        return result;
    }

    @PutMapping("/user/pwd")
    @ApiOperation(value = "修改个人密码接口")
    @MyLog(title = "组织管理-用户管理", action = "修改个人密码接口")
    public DataResult updatePwd(@RequestBody @Valid UserUpdatePwdReqVO vo, HttpServletRequest request) {
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        String refreshToken = request.getHeader(Constant.REFRESH_TOKEN);
        userService.userUpdatePwd(vo, accessToken, refreshToken);
        DataResult result = DataResult.success();
        return result;
    }
}
