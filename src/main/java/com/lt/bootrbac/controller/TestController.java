package com.lt.bootrbac.controller;

import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.utils.DataResult;
import com.lt.bootrbac.vo.req.TestReqVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@Api(tags = "测试接口模块",description = "主要是为了提供测试接口使用")
@RequestMapping("/test")
public class TestController {

    @GetMapping("/index")
    @ApiOperation(value = "测试接口")
    public String testResult(){
        return "springboot整合swagger成功！";
    }

    @GetMapping("/home")
    @ApiOperation(value = "测试DataResult接口")
    public DataResult<String> getHome(){
        int i=1/0;
        DataResult result = DataResult.success("使用DataResult统一返回接口");
        return result;
    }


    @GetMapping("/type")
    @ApiOperation(value = "测试捕获业务异常")
    public DataResult testBusinessException(@RequestParam String tye) {
        if (!tye.equals("1")) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        DataResult dataResult = DataResult.success(tye);
        return dataResult;
    }

    @PostMapping("/valid/error")
    @ApiOperation(value = "测试validator抛出业务异常接口")
    public DataResult testValid(@RequestBody @Valid TestReqVO vo){
        DataResult result=DataResult.success();
        return result;
    }
}
