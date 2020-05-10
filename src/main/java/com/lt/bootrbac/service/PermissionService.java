package com.lt.bootrbac.service;

import com.lt.bootrbac.entity.SysPermission;
import com.lt.bootrbac.vo.req.PermissionAddReqVO;
import com.lt.bootrbac.vo.req.PermissionUpdateReqVO;
import com.lt.bootrbac.vo.resp.PermissionRespNodeVO;

import java.util.List;

public interface PermissionService {

    List<SysPermission> selectAll();

    List<PermissionRespNodeVO> selectAllMenuByTree();

    SysPermission addPermission(PermissionAddReqVO vo);

    List<PermissionRespNodeVO> permissionTreeList(String userId);

    List<PermissionRespNodeVO> selectAllTree();

    void updatePermission(PermissionUpdateReqVO vo);

    void deletedPermission(String permissionId);

    List<String> getPermissionByUserId(String userId);

    List<SysPermission> getPermissions(String userId);
}
