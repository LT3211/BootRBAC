package com.lt.bootrbac.mapper;

import com.lt.bootrbac.entity.SysRole;
import com.lt.bootrbac.vo.req.RolePageReqVO;

import java.util.List;

public interface SysRoleMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysRole record);

    int insertSelective(SysRole record);

    SysRole selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysRole record);

    int updateByPrimaryKey(SysRole record);

    List<SysRole> selectAll(RolePageReqVO vo);

    List<String> getNamesByUserId(List<String> list);


}