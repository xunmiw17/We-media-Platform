package com.xunmiw.article.mapper;

import com.xunmiw.my.mapper.MyMapper;
import com.xunmiw.pojo.Article;
import org.springframework.stereotype.Repository;

@Repository
public interface ArticleMapperCustom extends MyMapper<Article> {

    public void publishAppointedArticles();
}