package com.lt.bootrbac.service.impl;

import com.lt.bootrbac.entity.SysUserRole;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.mapper.SysUserRoleMapper;
import com.lt.bootrbac.service.UserRoleService;
import com.lt.bootrbac.vo.req.UserOwnRoleReqVO;
import com.lt.bootrbac.vo.resp.UserOwnRoleRespVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;


@Service
public class UserRoleServiceImpl implements UserRoleService {

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Override
    public List<String> getRoleIdsByUserId(String userId) {
        return sysUserRoleMapper.getRoleIdsByUserId(userId);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void addUserRoleInfo(UserOwnRoleReqVO vo) {
        //删除关联数据
        sysUserRoleMapper.batchRemoveRoleByUserId(vo.getUserId());

        if (vo.getRoleIds() == null || vo.getRoleIds().isEmpty()) {
            return;
        }

        List<SysUserRole> list = new ArrayList<>();
        for (String roleId : vo.getRoleIds()) {
            SysUserRole sysUserRole = new SysUserRole();
            sysUserRole.setId(UUID.randomUUID().toString());
            sysUserRole.setCreateTime(new Date());
            sysUserRole.setUserId(vo.getUserId());
            sysUserRole.setRoleId(roleId);
            list.add(sysUserRole);

        }
        int i = sysUserRoleMapper.batchInsertUserRole(list);
        if (i == 0) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }

    @Override
    public List<String> getUserIdsByRoleIds(List<String> roleIds) {
        return sysUserRoleMapper.getUserIdsByRoleIds(roleIds);
    }

    @Override
    public List<String> getUserIdsByRoleId(String roleId) {
        return sysUserRoleMapper.getUserIdsByRoleId(roleId);
    }

    @Override
    public int removeByRoleId(String id) {
        return sysUserRoleMapper.removeByRoleId(id);
    }
}
