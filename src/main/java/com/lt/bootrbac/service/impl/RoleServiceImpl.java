package com.lt.bootrbac.service.impl;

import com.github.pagehelper.PageHelper;
import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.entity.SysRole;
import com.lt.bootrbac.entity.SysUser;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.mapper.SysRoleMapper;
import com.lt.bootrbac.service.PermissionService;
import com.lt.bootrbac.service.RedisService;
import com.lt.bootrbac.service.RoleService;
import com.lt.bootrbac.service.UserRoleService;
import com.lt.bootrbac.utils.PageUtil;
import com.lt.bootrbac.utils.TokenSetting;
import com.lt.bootrbac.vo.req.AddRoleReqVO;
import com.lt.bootrbac.vo.req.RolePageReqVO;
import com.lt.bootrbac.vo.req.RolePermissionOperationReqVO;
import com.lt.bootrbac.vo.req.RoleUpdateReqVO;
import com.lt.bootrbac.vo.resp.PageVO;
import com.lt.bootrbac.vo.resp.PermissionRespNodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RoleServiceImpl implements RoleService {

    @Autowired
    private SysRoleMapper sysRoleMapper;

    @Autowired
    private PermissionService permissionService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RolePermissionServiceImpl rolePermissionService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenSetting tokenSetting;

    @Override
    public PageVO<SysRole> pageInfo(RolePageReqVO vo) {
        PageHelper.startPage(vo.getPageNum(), vo.getPageSize());
        List<SysRole> sysRoles = sysRoleMapper.selectAll(vo);
        return PageUtil.getPageVO(sysRoles);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public SysRole addRole(AddRoleReqVO vo) {
        SysRole sysRole = new SysRole();
        BeanUtils.copyProperties(vo, sysRole);
        sysRole.setId(UUID.randomUUID().toString());
        sysRole.setCreateTime(new Date());
        int i = sysRoleMapper.insertSelective(sysRole);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        //如果角色包含权限信息，则维护角色权限表
        if (vo.getPermissions() != null && !vo.getPermissions().isEmpty()) {
            RolePermissionOperationReqVO operationReqVO = new RolePermissionOperationReqVO();
            operationReqVO.setRoleId(sysRole.getId());
            operationReqVO.setPermissionIds(vo.getPermissions());
            rolePermissionService.addRolePermission(operationReqVO);
        }
        return sysRole;
    }

    @Override
    public List<SysRole> selectAll() {
        return sysRoleMapper.selectAll(new RolePageReqVO());
    }

    @Override
    public SysRole detailInfo(String id) {
        SysRole sysRole = sysRoleMapper.selectByPrimaryKey(id);
        if (sysRole == null) {
            log.error("传入的id:{}不合法", id);
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }
        //获取所有菜单权限树
        List<PermissionRespNodeVO> permissionRespNodeVOS = permissionService.selectAllTree();
        //获取该角色拥有的菜单权限
        List<String> permissionIdsByRoleId = rolePermissionService.getPermissionIdsByRoleId(id);

        Set<String> checkList = new HashSet<>(permissionIdsByRoleId);

        //遍历菜单权限树的数据
        setChecked(permissionRespNodeVOS, checkList);
        sysRole.setPermissionRespNodeVO(permissionRespNodeVOS);

        return sysRole;
    }

    private void setChecked(List<PermissionRespNodeVO> list, Set<String> checkList) {
        /**
         * 子集选中从它往上到根目录都被选中，父级选中从他到所有的叶子节点都会被选中
         * 这样我们就直接遍历最底层就可以了
         */
        for (PermissionRespNodeVO node : list) {
            if (checkList.contains(node.getId()) && (node.getChildren() == null || node.getChildren().isEmpty())) {
                node.setChecked(true);
            }
            setChecked((List<PermissionRespNodeVO>) node.getChildren(), checkList);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updateRole(RoleUpdateReqVO vo) {
        //保存角色基本信息
        SysRole sysRole = sysRoleMapper.selectByPrimaryKey(vo.getId());
        if (sysRole == null) {
            log.error("传入的id:{}不合法", vo.getId());
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }

        BeanUtils.copyProperties(vo, sysRole);
        sysRole.setUpdateTime(new Date());
        int i = sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        //修改角色和菜单权限关联数据
        RolePermissionOperationReqVO reqVO = new RolePermissionOperationReqVO();
        reqVO.setRoleId(vo.getId());
        reqVO.setPermissionIds(vo.getPermissions());
        rolePermissionService.addRolePermission(reqVO);

        //标记关联用户
        List<String> userIdsByRoleId = userRoleService.getUserIdsByRoleId(vo.getId());

        if (!userIdsByRoleId.isEmpty()) {
            for (String userId :
                    userIdsByRoleId) {
                /**
                 * 标记用户在用户认证的时候判断这个用户是否主动刷新
                 */
                redisService.set(Constant.JWT_REFRESH_KEY + userId, userId,
                        tokenSetting.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);

                /**
                 * 删除用户缓存信息
                 */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
            }
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deleteRole(String id) {
        //删除角色信息
        SysRole sysRole = new SysRole();
        sysRole.setId(id);
        sysRole.setDeleted(0);
        sysRole.setUpdateTime(new Date());
        int i = sysRoleMapper.updateByPrimaryKeySelective(sysRole);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        //删除角色和菜单权限关联数据
        rolePermissionService.removeByRoleId(id);

        //查询和该角色关联的数据
        List<String> userIdsByRoleId = userRoleService.getUserIdsByRoleId(id);

        //删除角色和用户关联的数据
        userRoleService.removeByRoleId(id);

        if (!userIdsByRoleId.isEmpty()) {
            for (String userId :
                    userIdsByRoleId) {
                /**
                 * 标记用户在用户认证的时候判断这个用户是否主动刷新
                 */
                redisService.set(Constant.JWT_REFRESH_KEY + userId, userId,
                        tokenSetting.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);

                /**
                 * 删除用户缓存信息
                 */
                redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
            }
        }

    }

    @Override
    public List<String> getNamesByUserId(String userId) {
        List<String> roleIdsByUserId = userRoleService.getRoleIdsByUserId(userId);
        if (roleIdsByUserId.isEmpty()) {
            return null;
        }

        return sysRoleMapper.getNamesByUserId(roleIdsByUserId);
    }
}