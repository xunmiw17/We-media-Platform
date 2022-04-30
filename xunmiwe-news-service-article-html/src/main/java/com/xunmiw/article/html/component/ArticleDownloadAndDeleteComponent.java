package com.xunmiw.article.html.component;

import com.mongodb.client.gridfs.GridFSBucket;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@Component
public class ArticleDownloadAndDeleteComponent {

    @Autowired
    private GridFSBucket gridFSBucket;

    @Value("${freemarker.html.article}")
    private String articlePath;

    public Integer download(String articleId, String fileId) throws Exception {

        String path = articlePath + File.separator + articleId + ".html";
        File file = new File(path);
        OutputStream os = new FileOutputStream(file);

        gridFSBucket.downloadToStream(new ObjectId(fileId), os);
        return HttpStatus.OK.value();
    }

    public Integer delete(String articleId) {
        String path = articlePath + File.separator + articleId + ".html";
        File file = new File(path);
        file.delete();
        return HttpStatus.OK.value();
    }
}
