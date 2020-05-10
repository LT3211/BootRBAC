package com.lt.bootrbac.exception.handler;


import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.utils.DataResult;
import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.authz.UnauthorizedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@RestControllerAdvice
@Slf4j
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public DataResult handleException(Exception e){
        log.error("handlerException...{}",e);
        return DataResult.getResult(BaseResponseCode.SYSTEM_ERROR);
    }

    @ExceptionHandler(BusinessException.class)
    public DataResult handleBusinessException(BusinessException e){
        log.error("BusinessException...{}",e);
        return DataResult.getResult(e.getMessageCode(),e.getDetailMessage());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public DataResult handleMethodArgumentNotValidException(MethodArgumentNotValidException e){
        List<ObjectError> allErrors = e.getBindingResult().getAllErrors();
        log.error("MethodArgumentNotValidException...{}",e.getBindingResult().getAllErrors(),e);

        String msg = null;
        for (ObjectError error:allErrors){
            msg=error.getDefaultMessage();
            break;
        }
        return DataResult.getResult(BaseResponseCode.METHOD_IDENTITY_ERROR.getCode(),msg);

    }

    @ExceptionHandler(UnauthorizedException.class)
    public DataResult unauthorizedException(UnauthorizedException e){
        log.error("UnauthorizedException:{}",e);
        return DataResult.getResult(BaseResponseCode.NOT_PERMISSION);
    }
}
