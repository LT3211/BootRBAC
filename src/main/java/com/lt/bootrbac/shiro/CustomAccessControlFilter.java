package com.lt.bootrbac.shiro;

import com.alibaba.fastjson.JSON;
import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.utils.DataResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.web.filter.AccessControlFilter;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.OutputStream;

@Slf4j
public class CustomAccessControlFilter extends AccessControlFilter {

    /**
     * 如果返回的是true就流转到下一个链式调用
     * 如果返回false就会流转到onAccessDenied()方法
     *
     * @param servletRequest
     * @param servletResponse
     * @param o
     * @return
     * @throws Exception
     */
    @Override
    protected boolean isAccessAllowed(ServletRequest servletRequest, ServletResponse servletResponse, Object o) throws Exception {
        return false;
    }

    /**
     * 表示访问拒绝是否自己处理
     * 如果返回true 表示自己不处理且继续拦截器链执行
     * 如果返回false 表示自己已经处理了(直接通过response返回给前端)
     *
     * @param servletRequest
     * @param servletResponse
     * @return
     * @throws Exception
     */
    @Override
    protected boolean onAccessDenied(ServletRequest servletRequest, ServletResponse servletResponse) throws Exception {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        log.info("request 接口地址:{}", request.getRequestURI());
        log.info("request 接口的请求方式{}", request.getMethod());

        try {
            //获得用户端的accessToken
            String accessToken = request.getHeader(Constant.ACCESS_TOKEN);
            //判断客户端是否携带accessToken
            if (StringUtils.isEmpty(accessToken)) {
                //如果客户端没有携带
                throw new BusinessException(BaseResponseCode.TOKEN_NOT_NULL);
            }

            //封装成自定义UsernamePasswordToken
            CustomUsernamePasswordToken customUsernamePasswordToken = new CustomUsernamePasswordToken(accessToken);
            //主体提交给realm进行处理
            getSubject(servletRequest, servletResponse).login(customUsernamePasswordToken);
        } catch (BusinessException e) {
            customResponse(e.getMessageCode(),e.getMessage(),servletResponse);
            return false;
        }catch (AuthenticationException e){
            if (e.getCause() instanceof BusinessException){
                BusinessException exception= (BusinessException) e.getCause();
                customResponse(exception.getMessageCode(),exception.getMessage(),servletResponse);
            }else {
                customResponse(BaseResponseCode.SHIRO_AUTHENTICATION_ERROR.getCode(),
                        BaseResponseCode.SHIRO_AUTHENTICATION_ERROR.getMsg(),servletResponse);
            }
            return false;
        }catch (Exception e){
            customResponse(BaseResponseCode.SYSTEM_ERROR.getCode(),
                    BaseResponseCode.SYSTEM_ERROR.getMsg(),servletResponse);
            return false;
        }
        return true;
    }

    /**
     * 自定义错误响应
     * 如果出现异常，直接通过response返回
     * @param code
     * @param msg
     * @param response
     */
    private void customResponse(int code,String msg,ServletResponse response){
        //自定义异常的类，用户返回给客户端相应的json格式的信息
        try{
            DataResult result=DataResult.getResult(code,msg);
            response.setContentType(MediaType.APPLICATION_JSON_UTF8.toString());
            response.setCharacterEncoding("UTF-8");
            String json= JSON.toJSONString(result);
            OutputStream outputStream = response.getOutputStream();
            outputStream.write(json.getBytes("UTF-8"));
            outputStream.flush();
        }catch (IOException e){
            log.error("customResponse 响应错误",e);
        }
    }
}
