package com.lt.bootrbac.mapper;

import com.lt.bootrbac.entity.SysUser;
import com.lt.bootrbac.vo.req.UserPageReqVO;
import io.lettuce.core.dynamic.annotation.Param;

import java.util.List;

public interface SysUserMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysUser record);

    int insertSelective(SysUser record);

    SysUser selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysUser record);

    int updateByPrimaryKey(SysUser record);

    SysUser getUserInfoByName(String username);

    List<SysUser> selectAll(UserPageReqVO vo);

    int deletedUsers(@Param("sysUser") SysUser sysUser, @Param("list") List<String> list);

    //根据部门id集合查找用户
    List<SysUser> selectUserInfoByDeptIds(List<String> deptIds);
}