package com.lt.bootrbac.service;

import com.lt.bootrbac.entity.SysUser;
import com.lt.bootrbac.vo.req.*;
import com.lt.bootrbac.vo.resp.LoginRespVO;
import com.lt.bootrbac.vo.resp.PageVO;
import com.lt.bootrbac.vo.resp.UserOwnRoleRespVO;

import java.util.List;

public interface UserService {

    LoginRespVO login(LoginReqVO vo);

    PageVO<SysUser> pageInfo(UserPageReqVO vo);

    void addUser(UserAddReqVO vo);

    UserOwnRoleRespVO getUserOwnRole(String userId);

    void setUserOwnRole(UserOwnRoleReqVO vo);

    String refreshToken(String refreshToken);

    void updateUserInfo(UserUpdateReqVO vo, String operationId);

    void deletedUsers(List<String> list, String operationId);

    List<SysUser> selectUserInfoByDeptIds(List<String> deptIds);

    void logout(String accessToken, String refreshToken);

    SysUser detailInfo(String userId);

    //个人用户编辑信息接口
    void userUpdateDetailInfo(UserUpdateDetailInfoReqVO vo, String userId);

    void userUpdatePwd(UserUpdatePwdReqVO vo,String accessToken,String refreshToken);
}
