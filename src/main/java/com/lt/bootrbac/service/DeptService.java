package com.lt.bootrbac.service;

import com.lt.bootrbac.entity.SysDept;
import com.lt.bootrbac.vo.req.DeptAddReqVO;
import com.lt.bootrbac.vo.req.DeptUpdateReqVO;
import com.lt.bootrbac.vo.resp.DeptRespNodeVO;

import java.util.List;

public interface DeptService {

    List<SysDept> selectAll();

    List<DeptRespNodeVO> deptTreeList(String deptId);

    SysDept addDept(DeptAddReqVO vo);

    void updateDept(DeptUpdateReqVO vo);

    void deletedDept(String id);

}
