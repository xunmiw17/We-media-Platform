package com.xunmiw.admin.controller;

import com.xunmiw.admin.service.CategoryMngService;
import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.admin.CategoryMngControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.bo.CategoryBO;
import com.xunmiw.utils.JsonUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CategoryMngController extends BaseController implements CategoryMngControllerApi {

    @Autowired
    private CategoryMngService categoryMngService;

    @Override
    public GraceJSONResult getCatList() {
        String categoryList = redisOperator.get(REDIS_CATEGORY_LIST);
        List<Category> categories = null;
        if (StringUtils.isNotBlank(categoryList)) {
            categories = JsonUtils.jsonToList(categoryList, Category.class);
        } else {
            categories = categoryMngService.getCatList();
            redisOperator.set(REDIS_CATEGORY_LIST, JsonUtils.objectToJson(categories));
        }
        return GraceJSONResult.ok(categories);
    }

    @Override
    public GraceJSONResult saveOrUpdateCategory(CategoryBO categoryBO, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            Map<String, String> errors = getErrors(bindingResult);
            return GraceJSONResult.errorMap(errors);
        }
        Category category = new Category();
        BeanUtils.copyProperties(categoryBO, category);
        if (category.getId() == null) {
            if (categoryMngService.categoryNameExist(category.getName())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.CATEGORY_EXIST_ERROR);
            }
            categoryMngService.createCategory(category);
        } else {
            if (categoryMngService.categoryNameExist(category.getName())) {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.CATEGORY_EXIST_ERROR);
            }
            categoryMngService.updateCategory(category);
        }
        return GraceJSONResult.ok();
    }
}
