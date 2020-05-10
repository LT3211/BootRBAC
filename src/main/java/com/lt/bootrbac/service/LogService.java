package com.lt.bootrbac.service;

import com.lt.bootrbac.entity.SysLog;
import com.lt.bootrbac.vo.req.SysLogPageReqVO;
import com.lt.bootrbac.vo.resp.PageVO;
import org.springframework.stereotype.Service;

import java.util.List;

public interface LogService {

    PageVO<SysLog> pageInfo(SysLogPageReqVO vo);

    void deletedLog(List<String> logIds);

}
