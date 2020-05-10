package com.lt.bootrbac.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.lt.bootrbac.entity.SysLog;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.mapper.SysLogMapper;
import com.lt.bootrbac.service.LogService;
import com.lt.bootrbac.utils.PageUtil;
import com.lt.bootrbac.vo.req.SysLogPageReqVO;
import com.lt.bootrbac.vo.resp.PageVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogServiceImpl implements LogService {

    @Autowired
    private SysLogMapper sysLogMapper;

    @Override
    public PageVO<SysLog> pageInfo(SysLogPageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        return PageUtil.getPageVO(sysLogMapper.selectAll(vo));
    }

    @Override
    public void deletedLog(List<String> logIds) {
        int i = sysLogMapper.batchDeletedlog(logIds);
        if (i == 0) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }
    }
}
