package com.lt.bootrbac.controller;

import com.lt.bootrbac.aop.annotation.MyLog;
import com.lt.bootrbac.entity.SysDept;
import com.lt.bootrbac.service.DeptService;
import com.lt.bootrbac.utils.DataResult;
import com.lt.bootrbac.vo.req.DeptAddReqVO;
import com.lt.bootrbac.vo.req.DeptUpdateReqVO;
import com.lt.bootrbac.vo.resp.DeptRespNodeVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModelProperty;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.Logical;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api")
@Api(tags = "组织管理-部门管理", description = "部门管理相关接口")
public class DeptController {

    @Autowired
    private DeptService deptService;

    @GetMapping("/depts")
    @ApiOperation(value = "获取所有部门数据接口")
    @MyLog(title = "组织管理-部门管理", action = "查询所有部门数据")
    @RequiresPermissions("sys:dept:list")
    public DataResult<List<SysDept>> getAllDept() {
        DataResult result = DataResult.success();
        result.setData(deptService.selectAll());
        return result;
    }

    @GetMapping("/dept/tree")
    @ApiModelProperty(value = "部门树形结构列表接口")
    @MyLog(title = "组织管理-部门管理", action = "部门树形结构列表接口")
    @RequiresPermissions(value = {"sys:user:update", "sys:user:add", "sys:dept:update", "sys:dept:add"}, logical = Logical.OR)
    public DataResult<List<DeptRespNodeVO>> getDeptTree(@RequestParam(required = false) String deptId) {
        DataResult result = DataResult.success();
        result.setData(deptService.deptTreeList(deptId));
        return result;
    }

    @PostMapping("/dept")
    @ApiOperation(value = "新增部门数据接口")
    @MyLog(title = "组织管理-部门管理", action = "新增部门数据接口")
    @RequiresPermissions("sys:dept:add")
    public DataResult<SysDept> addDept(@RequestBody @Valid DeptAddReqVO vo) {
        DataResult result = DataResult.success();
        result.setData(deptService.addDept(vo));

        return result;
    }

    @PutMapping("/dept")
    @ApiOperation(value = "更新部门数据接口")
    @MyLog(title = "组织管理-部门管理", action = "更新部门数据接口")
    @RequiresPermissions("sys:dept:put")
    public DataResult updateDept(@RequestBody @Valid DeptUpdateReqVO vo) {
        deptService.updateDept(vo);
        DataResult result = DataResult.success();
        return result;
    }

    @DeleteMapping("/dept/{id}")
    @ApiOperation(value = "删除部门接口")
    @MyLog(title = "组织管理-部门管理", action = "删除部门接口")
    @RequiresPermissions("sys:dept:delete")
    public DataResult deleteDepts(@PathVariable("id") String id) {
        deptService.deletedDept(id);
        DataResult result = DataResult.success();
        return result;
    }

}
