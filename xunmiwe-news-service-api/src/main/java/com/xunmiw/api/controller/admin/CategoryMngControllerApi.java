package com.xunmiw.api.controller.admin;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.CategoryBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.validation.Valid;

@Api(value = "管理员维护文章分类", tags = {"管理员维护文章分类的Controller"})
@RequestMapping("categoryMng")
public interface CategoryMngControllerApi {

    @ApiOperation(value = "查询文章分类列表", notes = "查询文章分类列表", httpMethod = "POST")
    @PostMapping("getCatList")
    public GraceJSONResult getCatList();

    @ApiOperation(value = "更新/新增文章分类", notes = "更新/新增文章分类", httpMethod = "POST")
    @PostMapping("saveOrUpdateCategory")
    public GraceJSONResult saveOrUpdateCategory(@RequestBody @Valid CategoryBO categoryBO);

    @ApiOperation(value = "查询文章分类列表 (用于文章发布)", notes = "查询文章分类列表 (用于文章发布)", httpMethod = "GET")
    @GetMapping("getCats")
    public GraceJSONResult getCats();
}
