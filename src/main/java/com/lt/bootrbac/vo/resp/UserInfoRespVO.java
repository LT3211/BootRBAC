package com.lt.bootrbac.vo.resp;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class UserInfoRespVO {

    @ApiModelProperty(value = "id")
    private String id;

    @ApiModelProperty(value = "账号")
    private String username;

    @ApiModelProperty(value = "部门id")
    private String deptId;

    @ApiModelProperty(value = "所属部门名称")
    private String deptName;

}
