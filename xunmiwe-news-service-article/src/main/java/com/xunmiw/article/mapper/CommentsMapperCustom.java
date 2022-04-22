package com.xunmiw.article.mapper;

import com.xunmiw.pojo.vo.CommentsVO;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public interface CommentsMapperCustom {

    /**
     * 查询文章评论
     */
    List<CommentsVO> queryComments(@Param("paramMap")Map<String, Object> map);
}