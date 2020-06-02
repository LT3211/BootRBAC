package com.lt.bootrbac.controller;

import com.lt.bootrbac.aop.annotation.MyLog;
import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.service.HomeService;
import com.lt.bootrbac.utils.DataResult;
import com.lt.bootrbac.utils.JwtTokenUtil;
import com.lt.bootrbac.vo.resp.HomeRespVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api")
@Api(tags = "首页模块",description = "首页相关模块")
public class HomeController {

    @Autowired
    private HomeService homeService;

    @GetMapping("/home")
    @ApiOperation(value = "获取首页数据接口")
    @MyLog(title = "首页模块",action = "获取首页数据接口")
    public DataResult<HomeRespVO> getHome(HttpServletRequest request){
        //获取用户Token
        String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
        //从Token中解析除userId
        String userId = JwtTokenUtil.getUserId(accessToken);
        DataResult result = DataResult.success();
        //获得 用户信息 和 首页导航信息
        result.setData(homeService.getHome(userId));
        return result;
    }

}
