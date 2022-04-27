package com.xunmiw.article.html.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.xunmiw.api.controller.articlehtml.ArticleHTMLControllerApi;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@RestController
public class ArticleHTMLController implements ArticleHTMLControllerApi {

    @Autowired
    private GridFSBucket gridFSBucket;

    @Value("${freemarker.html.article}")
    private String articlePath;

    @Override
    public Integer download(String articleId, String fileId) throws Exception {

        String path = articlePath + File.separator + articleId + ".html";
        File file = new File(path);
        OutputStream os = new FileOutputStream(file);

        gridFSBucket.downloadToStream(new ObjectId(fileId), os);
        return HttpStatus.OK.value();
    }
}
