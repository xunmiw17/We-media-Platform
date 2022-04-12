package com.xunmiw.admin.service;

import com.xunmiw.pojo.Category;

import java.util.List;

public interface CategoryMngService {

    /**
     * 查询文章分类列表
     * @return
     */
    List<Category> getCatList();

    /**
     * 新增文章分类
     * @param category
     */
    void createCategory(Category category);

    /**
     * 更新文章分类
     * @param category
     */
    void updateCategory(Category category);

    /**
     * 查询文章分类是否已存在
     * @param catName
     * @return
     */
    boolean categoryNameExist(String catName);
}
