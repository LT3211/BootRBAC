package com.lt.bootrbac.service;

import com.lt.bootrbac.vo.resp.HomeRespVO;

public interface HomeService {
    HomeRespVO getHome(String userId);
}
