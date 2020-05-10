package com.lt.bootrbac.mapper;

import com.lt.bootrbac.entity.SysLog;
import com.lt.bootrbac.vo.req.SysLogPageReqVO;
import com.lt.bootrbac.vo.resp.PageVO;

import java.util.List;

public interface SysLogMapper {
    int deleteByPrimaryKey(String id);

    int insert(SysLog record);

    int insertSelective(SysLog record);

    SysLog selectByPrimaryKey(String id);

    int updateByPrimaryKeySelective(SysLog record);

    int updateByPrimaryKey(SysLog record);

    List<SysLog> selectAll(SysLogPageReqVO vo);

    int batchDeletedlog(List<String > logIds);

}