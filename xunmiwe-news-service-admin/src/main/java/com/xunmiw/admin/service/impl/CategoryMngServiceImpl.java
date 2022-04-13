package com.xunmiw.admin.service.impl;

import com.xunmiw.admin.mapper.CategoryMapper;
import com.xunmiw.admin.service.CategoryMngService;
import com.xunmiw.api.service.BaseService;
import com.xunmiw.pojo.Category;
import com.xunmiw.utils.JsonUtils;
import com.xunmiw.utils.RedisOperator;
import org.apache.commons.lang3.StringUtils;
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
        List<Category> categories = null;

        // 先从Redis中查询文章列表，如果有则直接返回，如果为空则查询数据后后放入缓存再返回
        String categoryJson = redisOperator.get(REDIS_CATEGORY_LIST);
        if (StringUtils.isNotBlank(categoryJson)) {
            categories = JsonUtils.jsonToList(categoryJson, Category.class);
        } else {
            categories = categoryMapper.selectAll();
            redisOperator.set(REDIS_CATEGORY_LIST, JsonUtils.objectToJson(categories));
        }
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
