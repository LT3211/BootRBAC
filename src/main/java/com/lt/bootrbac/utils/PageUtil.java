package com.lt.bootrbac.utils;

import com.github.pagehelper.Page;
import com.lt.bootrbac.vo.resp.PageVO;

import java.util.List;

public class PageUtil {

    private PageUtil(){}

    public static <T> PageVO getPageVO(List<T> list){
        PageVO<T> pageVO=new PageVO<>();
        if (list instanceof Page){
            Page page= (Page) list;
            pageVO.setTotalRows(page.getTotal());
            pageVO.setList(page.getResult());
            pageVO.setTotalPages(page.getPages());
            pageVO.setCurPageSize(page.size());
            pageVO.setPageNum(page.getPageNum());
            pageVO.setPageSize(page.getPageSize());
        }
        return pageVO;
    }
}
