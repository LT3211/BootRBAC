package com.lt.bootrbac.service;

import com.lt.bootrbac.vo.req.UserOwnRoleReqVO;
import com.lt.bootrbac.vo.resp.UserOwnRoleRespVO;

import java.util.List;

public interface UserRoleService {

    List<String> getRoleIdsByUserId(String userId);

    void addUserRoleInfo(UserOwnRoleReqVO vo);

    List<String> getUserIdsByRoleIds(List<String> roleIds);

    List<String> getUserIdsByRoleId(String roleId);

    int removeByRoleId(String id);
}
