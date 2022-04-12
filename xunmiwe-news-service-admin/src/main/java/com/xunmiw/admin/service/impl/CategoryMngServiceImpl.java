package com.xunmiw.admin.service.impl;

import com.xunmiw.admin.mapper.CategoryMapper;
import com.xunmiw.admin.service.CategoryMngService;
import com.xunmiw.api.service.BaseService;
import com.xunmiw.pojo.Category;
import com.xunmiw.utils.RedisOperator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class CategoryMngServiceImpl extends BaseService implements CategoryMngService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private RedisOperator redisOperator;

    @Override
    public List<Category> getCatList() {
        List<Category> categories = categoryMapper.selectAll();
        return categories;
    }

    @Override
    @Transactional
    public void createCategory(Category category) {
        categoryMapper.insert(category);

        redisOperator.del(REDIS_CATEGORY_LIST);
    }

    @Override
    @Transactional
    public void updateCategory(Category category) {
        categoryMapper.updateByPrimaryKey(category);

        redisOperator.del(REDIS_CATEGORY_LIST);
    }

    @Override
    public boolean categoryNameExist(String catName) {
        Example example = new Example(Category.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("name", catName);
        List<Category> categories = categoryMapper.selectByExample(example);
        if (categories == null || categories.size() == 0)
            return false;
        return true;
    }
}
