package com.lt.bootrbac.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class SysDept implements Serializable {
    private String id;

    private String deptNo;

    private String name;

    private String pid;

    private Integer status;

    //为了维护更深层级关系(规则：父级关系编码+自己的编码)
    private String relationCode;

    private String deptManagerId;

    private String managerName;

    private String phone;

    private Date createTime;

    private Date updateTime;

    private Integer deleted;

    private String pidName;


}