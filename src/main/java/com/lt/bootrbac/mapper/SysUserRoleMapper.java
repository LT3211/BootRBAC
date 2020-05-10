package com.lt.bootrbac.mapper;

import com.lt.bootrbac.entity.SysUserRole;

import java.util.List;

public interface SysUserRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysUserRole record);

    int insertSelective(SysUserRole record);

    SysUserRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysUserRole record);

    int updateByPrimaryKey(SysUserRole record);

    List<String> getRoleIdsByUserId(String userId);

    int batchRemoveRoleByUserId(String userId);

    int batchInsertUserRole(List<SysUserRole> list);

    List<String> getUserIdsByRoleIds(List<String > roleIds);

    List<String> getUserIdsByRoleId(String roleId);

    int removeByRoleId(String id);

}