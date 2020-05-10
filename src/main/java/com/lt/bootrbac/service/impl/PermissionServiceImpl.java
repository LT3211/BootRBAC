package com.lt.bootrbac.service.impl;

import com.lt.bootrbac.contants.Constant;
import com.lt.bootrbac.entity.SysPermission;
import com.lt.bootrbac.exception.BusinessException;
import com.lt.bootrbac.exception.code.BaseResponseCode;
import com.lt.bootrbac.mapper.SysPermissionMapper;
import com.lt.bootrbac.service.PermissionService;
import com.lt.bootrbac.service.RedisService;
import com.lt.bootrbac.service.RolePermissionService;
import com.lt.bootrbac.service.UserRoleService;
import com.lt.bootrbac.utils.TokenSetting;
import com.lt.bootrbac.vo.req.PermissionAddReqVO;
import com.lt.bootrbac.vo.req.PermissionUpdateReqVO;
import com.lt.bootrbac.vo.resp.PermissionRespNodeVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    @Autowired
    private SysPermissionMapper sysPermissionMapper;

    @Autowired
    private RolePermissionService rolePermissionService;

    @Autowired
    private UserRoleService userRoleService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private TokenSetting tokenSetting;

    /**
     * 查询所有权限
     *
     * @return
     */
    @Override
    public List<SysPermission> selectAll() {
        List<SysPermission> sysPermissions = sysPermissionMapper.selectAll();
        if (!sysPermissions.isEmpty()) {
            for (SysPermission sysPermission : sysPermissions) {
                //查询出父级对象，设置父级名称
                SysPermission parent = sysPermissionMapper.selectByPrimaryKey(sysPermission.getPid());
                if (parent != null) {
                    sysPermission.setPidName(parent.getName());
                }
            }
        }
        return sysPermissions;
    }

    /**
     * 查询到菜单层级的权限
     *
     * @return 单层级的权限树
     */
    @Override
    public List<PermissionRespNodeVO> selectAllMenuByTree() {
        List<SysPermission> list = sysPermissionMapper.selectAll();
        List<PermissionRespNodeVO> result = new ArrayList<>();
        //创建默认顶级菜单
        PermissionRespNodeVO respNodeVO = new PermissionRespNodeVO();
        respNodeVO.setId("0");
        respNodeVO.setTitle("默认顶级菜单");
        //添加子层级权限，递归遍历到菜单
        respNodeVO.setChildren(getTree(list, true));
        result.add(respNodeVO);
        return result;
    }

    /**
     * 查询所有层级权限
     *
     * @return 所有层级权限树
     */
    @Override
    public List<PermissionRespNodeVO> selectAllTree() {
        //添加子层级权限，递归遍历到按钮
        return getTree(selectAll(), false);
    }

    /**
     * type=true  递归遍历到菜单
     * type=false 递归遍历到按钮
     *
     * @param all
     * @param type
     * @return
     */
    private List<PermissionRespNodeVO> getTree(List<SysPermission> all, boolean type) {
        List<PermissionRespNodeVO> list = new ArrayList<>();
        if (all.isEmpty()) {
            return list;
        }
        for (SysPermission sysPermission : all) {
            //从目录开始遍历
            if (sysPermission.getPid().equals("0")) {
                //新建一个权限节点
                PermissionRespNodeVO respNodeVO = new PermissionRespNodeVO();
                BeanUtils.copyProperties(sysPermission, respNodeVO);
                respNodeVO.setTitle(sysPermission.getName());
                if (type) {
                    //递归遍历到菜单
                    respNodeVO.setChildren(getChildExBtn(sysPermission.getId(), all));
                } else {
                    //递归遍历到按钮
                    respNodeVO.setChildren(getChild(sysPermission.getId(), all));
                }
                list.add(respNodeVO);
            }
        }
        return list;
    }


    /**
     * 只递归到菜单
     *
     * @param id
     * @param all
     * @return
     */
    private List<PermissionRespNodeVO> getChildExBtn(String id, List<SysPermission> all) {
        List<PermissionRespNodeVO> list = new ArrayList<>();
        for (SysPermission s : all) {
            //找出子级权限,排除按钮
            if (s.getPid().equals(id) && s.getType() != 3) {
                PermissionRespNodeVO respNodeVO = new PermissionRespNodeVO();
                BeanUtils.copyProperties(s, respNodeVO);
                respNodeVO.setTitle(s.getName());
                respNodeVO.setChildren(getChildExBtn(s.getId(), all));
                list.add(respNodeVO);
            }
        }
        return list;
    }

    /**
     * 递归遍历所有数据
     *
     * @param id
     * @param all
     * @return
     */
    private List<PermissionRespNodeVO> getChild(String id, List<SysPermission> all) {
        List<PermissionRespNodeVO> list = new ArrayList<>();
        for (SysPermission s : all) {
            if (s.getPid().equals(id)) {
                PermissionRespNodeVO respNodeVO = new PermissionRespNodeVO();
                BeanUtils.copyProperties(s, respNodeVO);
                respNodeVO.setTitle(s.getName());
                respNodeVO.setChildren(getChild(s.getId(), all));
                list.add(respNodeVO);
            }
        }
        return list;
    }


    /**
     * 添加菜单
     *
     * @param vo
     * @return
     */
    @Override
    public SysPermission addPermission(PermissionAddReqVO vo) {
        SysPermission sysPermission = new SysPermission();
        BeanUtils.copyProperties(vo, sysPermission);
        //验证菜单是否合法
        verifyForm(sysPermission);
        sysPermission.setId(UUID.randomUUID().toString());
        sysPermission.setCreateTime(new Date());
        //插入时有判断
        int insert = sysPermissionMapper.insertSelective(sysPermission);
        if (insert != 1) {
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }

        return sysPermission;
    }

    /**
     * 操作后的菜单类型是目录的时候 父级必须为目录
     * 操作后的菜单类型是菜单的时候，父类必须为目录类型
     * 操作后的菜单类型是按钮的时候 父类必须为菜单类型
     *
     * @param sysPermission
     */
    private void verifyForm(SysPermission sysPermission) {
        SysPermission parent = sysPermissionMapper.selectByPrimaryKey(sysPermission.getPid());
        switch (sysPermission.getType()) {
            case 1:
                if (parent != null) {
                    if (parent.getType() != 1) {
                        throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_CATALOG_ERROR);
                    }
                } else if (!sysPermission.getPid().equals("0")) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_CATALOG_ERROR);
                }
                break;
            case 2:
                if (parent == null || parent.getType() != 1) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_MENU_ERROR);
                }
                if (StringUtils.isEmpty(sysPermission.getUrl())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_NOT_NULL);
                }
                break;
            case 3:
                if (parent == null || parent.getType() != 2) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_BTN_ERROR);
                }
                if (StringUtils.isEmpty(sysPermission.getPerms())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_PERMS_NULL);
                }
                if (StringUtils.isEmpty(sysPermission.getUrl())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_NOT_NULL);
                }
                if (StringUtils.isEmpty(sysPermission.getMethod())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_METHOD_NULL);
                }
                if (StringUtils.isEmpty(sysPermission.getCode())) {
                    throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_URL_CODE_NULL);
                }
                break;
        }
    }


    @Override
    public List<PermissionRespNodeVO> permissionTreeList(String userId) {
        return getTree(getPermissions(userId), true);
    }


    /**
     * 更新权限
     * @param vo
     */
    @Override
    public void updatePermission(PermissionUpdateReqVO vo) {
        //校验数据
        SysPermission update = new SysPermission();
        BeanUtils.copyProperties(vo, update);
        verifyForm(update);
        SysPermission sysPermission = sysPermissionMapper.selectByPrimaryKey(vo.getId());
        if (sysPermission == null) {
            log.info("传入的id在数据库中不存在");
            throw new BusinessException(BaseResponseCode.DATA_ERROR);
        }

        if (!sysPermission.getPid().equals(vo.getPid())) {
            //所属菜单发生了变化要校验该权限是否存在子集
            List<SysPermission> sysPermissions = sysPermissionMapper.selectChild(vo.getId());
            if (!sysPermissions.isEmpty()) {
                throw new BusinessException(BaseResponseCode.OPERATION_MENU_PERMISSION_UPDATE);
            }
        }

        update.setUpdateTime(new Date());
        int i = sysPermissionMapper.updateByPrimaryKeySelective(update);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        //判断授权标识是否发生了变化
        if (!sysPermission.getPerms().equals(vo.getPerms())) {
            //查询出所有拥有该权限的角色id
            List<String> roleIdsByPermissionId = rolePermissionService.getRoleIdsByPermissionId(vo.getId());
            if (!roleIdsByPermissionId.isEmpty()) {
                //查询出所有拥有该角色的用户id
                List<String> userIdsByRoleIds = userRoleService.getUserIdsByRoleIds(roleIdsByPermissionId);
                if (!userIdsByRoleIds.isEmpty()) {
                    for (String userId : userIdsByRoleIds) {
                        //设置刷新token
                        redisService.set(Constant.JWT_REFRESH_KEY + userId, userId,
                                tokenSetting.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                        //删除用户缓存信息
                        redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
                    }
                }
            }
        }
    }


    /**
     * 删除权限
     * @param permissionId
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletedPermission(String permissionId) {
        //判断是否有子集关联
        List<SysPermission> sysPermissions = sysPermissionMapper.selectChild(permissionId);
        if (!sysPermissions.isEmpty()) {
            throw new BusinessException(BaseResponseCode.NOT_PERMISSION_DELETED_DEPT);
        }

        //解除相关角色和该菜单权限的关联
        rolePermissionService.removeRoleByPermissionId(permissionId);

        //更新权限数据
        SysPermission sysPermission = new SysPermission();
        sysPermission.setUpdateTime(new Date());
        sysPermission.setDeleted(0);
        sysPermission.setId(permissionId);
        int i = sysPermissionMapper.updateByPrimaryKeySelective(sysPermission);
        if (i != 1) {
            throw new BusinessException(BaseResponseCode.OPERATION_ERROR);
        }

        //判断是否影响角色
        List<String> roleIdsByPermissionId = rolePermissionService.getRoleIdsByPermissionId(permissionId);
        if (!roleIdsByPermissionId.isEmpty()) {
            List<String> userIdsByRoleIds = userRoleService.getUserIdsByRoleIds(roleIdsByPermissionId);
            if (!userIdsByRoleIds.isEmpty()) {
                for (String userId : userIdsByRoleIds) {
                    redisService.set(Constant.JWT_REFRESH_KEY + userId, userId,
                            tokenSetting.getAccessTokenExpireTime().toMillis(), TimeUnit.MILLISECONDS);
                    /**
                     * 删除用户缓存信息
                     */
                    redisService.delete(Constant.IDENTIFY_CACHE_KEY + userId);
                }
            }
        }
    }

    @Override
    public List<String> getPermissionByUserId(String userId) {
        List<SysPermission> permissions = getPermissions(userId);
        if (permissions == null || permissions.isEmpty()) {
            return null;
        }

        List<String> result = new ArrayList<>();

        for (SysPermission s : permissions) {
            if (!StringUtils.isEmpty(s.getPerms())) {
                result.add(s.getPerms());
            }
        }

        return result;
    }

    @Override
    public List<SysPermission> getPermissions(String userId) {
        List<String> roleIdsByUserId = userRoleService.getRoleIdsByUserId(userId);
        if (roleIdsByUserId.isEmpty()) {
            return null;
        }

        List<String> permissionIdsByRoleIds = rolePermissionService.getPermissionIdsByRoleIds(roleIdsByUserId);

        if (permissionIdsByRoleIds.isEmpty()) {
            return null;
        }

        List<SysPermission> sysPermissions = sysPermissionMapper.selectByIds(permissionIdsByRoleIds);

        return sysPermissions;
    }
}
