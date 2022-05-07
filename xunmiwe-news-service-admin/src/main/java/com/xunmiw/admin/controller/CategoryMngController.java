package com.xunmiw.admin.controller;

import com.xunmiw.admin.service.CategoryMngService;
import com.xunmiw.api.BaseController;
import com.xunmiw.api.controller.admin.CategoryMngControllerApi;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.Category;
import com.xunmiw.pojo.bo.CategoryBO;
import com.xunmiw.pojo.vo.CategoryVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
public class CategoryMngController extends BaseController implements CategoryMngControllerApi {

    @Autowired
    private CategoryMngService categoryMngService;

    @Override
    public GraceJSONResult getCatList() {
        return GraceJSONResult.ok(categoryMngService.getCatList());
    }

    @Override
    public GraceJSONResult saveOrUpdateCategory(CategoryBO categoryBO) {
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

    @Override
    public GraceJSONResult getCats() {
        List<Category> categories = categoryMngService.getCatList();

        List<CategoryVO> categoryVOs = new ArrayList<>();
        for (Category category : categories) {
            CategoryVO categoryVO = new CategoryVO();
            BeanUtils.copyProperties(category, categoryVO);
            categoryVOs.add(categoryVO);
        }
        return GraceJSONResult.ok(categoryVOs);
    }
}
