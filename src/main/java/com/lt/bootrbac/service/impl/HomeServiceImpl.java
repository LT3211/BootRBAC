package com.lt.bootrbac.service.impl;

import com.alibaba.fastjson.JSON;
import com.lt.bootrbac.entity.SysUser;
import com.lt.bootrbac.mapper.SysUserMapper;
import com.lt.bootrbac.service.HomeService;
import com.lt.bootrbac.vo.resp.HomeRespVO;
import com.lt.bootrbac.vo.resp.PermissionRespNodeVO;
import com.lt.bootrbac.vo.resp.UserInfoRespVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class HomeServiceImpl implements HomeService {

    @Autowired
    private SysUserMapper sysUserMapper;

    @Autowired
    private PermissionServiceImpl permissionService;

    @Override
    public HomeRespVO getHome(String userId) {

        HomeRespVO homeRespVO = new HomeRespVO();
        /**
         * mock导航菜单数据后期直接从DB获取
         */
        //String home = "[{\"children\":[{\"children\":[{\"children\":[{\"children\":[{\"children\":[],\"id\":\"6\",\"title\":\"五级类目5-6\",\"url\":\"string\"}],\"id\":\"5\",\"title\":\"四级类目4-5\",\"url\":\"string\"}],\"id\":\"4\",\"title\":\"三级类目3-4\",\"url\":\"string\"}],\"id\":\"3\",\"title\":\"二级类目2-3\",\"url\":\"string\"}],\"id\":\"1\",\"title\":\"类目1\",\"url\":\"string\"},{\"children\":[],\"id\":\"2\",\"title\":\"类目2\",\"url\":\"string\"}]";
//        String home="[\n" +
//                "    {\n" +
//                "        \"children\": [\n" +
//                "            {\n" +
//                "                \"children\": [],\n" +
//                "                \"id\": \"3\",\n" +
//                "                \"title\": \"菜单权限管理\",\n" +
//                "                \"url\": \"/index/menus\"\n" +
//                "            }\n" +
//                "        ],\n" +
//                "        \"id\": \"1\",\n" +
//                "        \"title\": \"组织管理\",\n" +
//                "        \"url\": \"string\"\n" +
//                "    },\n" +
//                "    {\n" +
//                "        \"children\": [],\n" +
//                "        \"id\": \"2\",\n" +
//                "        \"title\": \"类目2\",\n" +
//                "        \"url\": \"string\"\n" +
//                "    }\n" +
//                "]";
        //根据用户id获取所拥有的权限列表树，只递归到菜单
        List<PermissionRespNodeVO> list = permissionService.permissionTreeList(userId);
        homeRespVO.setMenus(list);
        SysUser sysUser = sysUserMapper.selectByPrimaryKey(userId);
        UserInfoRespVO vo = new UserInfoRespVO();
        if (sysUser != null) {
            BeanUtils.copyProperties(sysUser, vo);
            vo.setDeptName("603实验室");
        }
        homeRespVO.setUserInfoVO(vo);
        return homeRespVO;
    }
}
