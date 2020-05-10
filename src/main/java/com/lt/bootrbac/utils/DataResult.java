package com.lt.bootrbac.utils;


import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.exception.code.ResponseCodeInterface;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class DataResult<T> {

    /**
     * 响应状态码
     */
    @ApiModelProperty(value = "响应状态码")
    private int code;

    /**
     * 响应提示语
     */
    @ApiModelProperty(value = "响应提示语")
    private String msg;

    /**
     * 响应数据
     */
    @ApiModelProperty(value = "响应数据")
    private T data;

    public DataResult(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public DataResult(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    public DataResult(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public DataResult() {
        this.code= BaseResponseCode.SUCCESS.getCode();
        this.msg= BaseResponseCode.SUCCESS.getMsg();
        this.data=null;
    }

    public DataResult(T data) {
        this.code= BaseResponseCode.SUCCESS.getCode();
        this.msg= BaseResponseCode.SUCCESS.getMsg();
        this.data=data;
    }

    public DataResult(ResponseCodeInterface responseCodeInterface) {
        this.code=responseCodeInterface.getCode();
        this.msg=responseCodeInterface.getMsg();
        this.data=null;
    }

    public DataResult(ResponseCodeInterface responseCodeInterface, T data) {
        this.code=responseCodeInterface.getCode();
        this.msg=responseCodeInterface.getMsg();
        this.data=data;
    }

    public static <T> DataResult getResult(int code, String msg, T data) {
        return new DataResult(code, msg, data);
    }

    public static DataResult getResult(int code, String msg) {
        return new DataResult(code, msg);
    }

    public static <T> DataResult getResult(int code, T data) {
        return new DataResult(code, data);
    }

    public static DataResult getResult(ResponseCodeInterface responseCodeInterface){
        return new DataResult(responseCodeInterface);
    }

    public static <T> DataResult getResult(ResponseCodeInterface responseCodeInterface,T data){
        return new DataResult(responseCodeInterface);
    }

    public static DataResult success(){
        return new DataResult();
    }

    public static <T> DataResult success(T data){
        return new DataResult(data);
    }
}
