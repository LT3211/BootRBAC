package com.lt.bootrbac.service;

import com.lt.bootrbac.entity.SysRole;
import com.lt.bootrbac.entity.SysUser;
import com.lt.bootrbac.vo.req.AddRoleReqVO;
import com.lt.bootrbac.vo.req.RolePageReqVO;
import com.lt.bootrbac.vo.req.RoleUpdateReqVO;
import com.lt.bootrbac.vo.resp.PageVO;

import java.util.List;

public interface RoleService {
    PageVO<SysRole> pageInfo(RolePageReqVO vo);

    SysRole addRole(AddRoleReqVO vo);

    List<SysRole> selectAll();

    SysRole detailInfo(String id);

    void updateRole(RoleUpdateReqVO vo);

    void deleteRole(String id);

    List<String> getNamesByUserId(String userId);
}
