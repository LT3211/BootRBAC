package com.lt.bootrbac.controller;

import com.lt.bootrbac.aop.annotation.MyLog;
import com.lt.bootrbac.entity.SysLog;
import com.lt.bootrbac.service.LogService;
import com.lt.bootrbac.utils.DataResult;
import com.lt.bootrbac.vo.req.SysLogPageReqVO;
import com.lt.bootrbac.vo.resp.PageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@Api(tags = "系统管理", description = "日志管理相关接口")
public class LogController {

    @Autowired
    private LogService logService;

    @PostMapping("/logs")
    @ApiOperation(value = "分页查找操作日志接口")
    @RequiresPermissions("sys:log:list")
    public DataResult<PageVO<SysLog>> pageInfo(@RequestBody SysLogPageReqVO vo) {
        PageVO<SysLog> sysLogPageVO = logService.pageInfo(vo);
        DataResult result = DataResult.success();
        result.setData(sysLogPageVO);
        return result;
    }

    @DeleteMapping("/log")
    @ApiOperation(value = "删除日志接口")
    @MyLog(title = "系统日志-日志管理", action = "删除日志接口")
    @RequiresPermissions("sys:log:delete")
    public DataResult deletedLog(@RequestBody @ApiParam(value = "日志id集合") List<String> logIds) {
        logService.deletedLog(logIds);
        DataResult result = DataResult.success();
        return result;
    }
}
