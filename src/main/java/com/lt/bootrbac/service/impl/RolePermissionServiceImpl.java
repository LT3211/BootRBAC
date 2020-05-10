package com.lt.bootrbac.service.impl;

import com.lt.bootrbac.entity.SysRolePermission;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.mapper.SysRolePermissionMapper;
import com.lt.bootrbac.service.RolePermissionService;
import com.lt.bootrbac.vo.req.RolePermissionOperationReqVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class RolePermissionServiceImpl implements RolePermissionService {

    @Autowired
    private SysRolePermissionMapper sysRolePermissionMapper;

    @Override
    public void addRolePermission(RolePermissionOperationReqVO vo) {
        if (vo.getPermissionIds() == null || vo.getPermissionIds().isEmpty()) {
            return;
        }
        //删除拥有的权限
        sysRolePermissionMapper.removeByRoleId(vo.getRoleId());

        List<SysRolePermission> list = new ArrayList<>();
        for (String permissionId : vo.getPermissionIds()) {
            SysRolePermission sysRolePermission = new SysRolePermission();
            sysRolePermission.setId(UUID.randomUUID().toString());
            sysRolePermission.setCreateTime(new Date());
            sysRolePermission.setRoleId(vo.getRoleId());
            sysRolePermission.setPermissionId(permissionId);
            list.add(sysRolePermission);
        }
        int i = sysRolePermissionMapper.batchInsertRolePermission(list);
        if (i == 0) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public List<String> getRoleIdsByPermissionId(String permissionId) {
        return sysRolePermissionMapper.getRoleIdsByPermissionId(permissionId);
    }

    @Override
    public int removeRoleByPermissionId(String permissionId) {
        return sysRolePermissionMapper.removeByPermissionId(permissionId);
    }

    @Override
    public List<String> getPermissionIdsByRoleId(String roleId) {
        return sysRolePermissionMapper.getPermissionIdsByRoleId(roleId);
    }

    @Override
    public int removeByRoleId(String roleId) {
        return sysRolePermissionMapper.removeByRoleId(roleId);
    }

    @Override
    public List<String> getPermissionIdsByRoleIds(List<String> roleIds) {
        return sysRolePermissionMapper.getPermissionIdsByRoleIds(roleIds);
    }
}
