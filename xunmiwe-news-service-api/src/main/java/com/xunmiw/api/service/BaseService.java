package com.xunmiw.api.service;

import com.github.pagehelper.PageInfo;
import com.xunmiw.utils.PagedGridResult;

import java.util.List;

public class BaseService {

    public PagedGridResult setPagedGrid(List<?> list, Integer page) {
        PageInfo<?> pageInfo = new PageInfo<>(list);

        PagedGridResult pagedGridResult = new PagedGridResult();
        pagedGridResult.setRows(list);
        pagedGridResult.setPage(page);
        pagedGridResult.setRecords(pageInfo.getTotal());
        pagedGridResult.setTotal(pageInfo.getPages());

        return pagedGridResult;
    }
}
