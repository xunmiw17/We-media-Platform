package com.xunmiw.files.controller;

import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSFindIterable;
import com.mongodb.client.gridfs.model.GridFSFile;
import com.mongodb.client.model.Filters;
import com.xunmiw.api.controller.files.FileUploadControllerApi;
import com.xunmiw.exception.GraceException;
import com.xunmiw.files.resource.FileResource;
import com.xunmiw.files.service.UploadService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.pojo.bo.NewAdminBO;
import com.xunmiw.utils.FileUtils;
import com.xunmiw.utils.extend.AliImageReviewUtils;
import org.apache.commons.lang3.StringUtils;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import sun.misc.BASE64Decoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

@RestController
public class FileUploadController implements FileUploadControllerApi {

    final static Logger logger = LoggerFactory.getLogger(FileUploadController.class);
    public static final String FAILED_IMAGE_URL = "https://xunmiwe-news.oss-us-west-1.aliyuncs.com/images/1/220323FTK63TGXP0/220331BTNMH209S8.jpeg";

    @Autowired
    private UploadService uploadService;

    @Autowired
    private FileResource fileResource;

    @Autowired
    private AliImageReviewUtils aliImageReviewUtils;

    @Autowired
    private GridFSBucket gridFSBucket;

    @Override
    public GraceJSONResult uploadFace(String userId, MultipartFile file) throws Exception {
        String path = "";
        if (file != null) {
            // 获得文件上传的名称
            String fileName = file.getOriginalFilename();

            // 判断文件名不能为空
            if (StringUtils.isNotBlank(fileName)) {
                // 获得文件名后缀，以作为参数传入service
                String[] fileNameArr = fileName.split("\\.");
                String suffix = fileNameArr[fileNameArr.length - 1];

                // 判断后缀符合我们的预定义规范，只能是png、jpg、jpeg三个格式之一
                if (!suffix.equals("png") &&
                    !suffix.equals("jpg") &&
                        !suffix.equals("jpeg")
                ) {
                    return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_FORMATTER_FAILD);
                }

                // 执行上传
                //path = uploadService.uploadFdfs(file, suffix);
                path = uploadService.uploadOss(file, userId, suffix);

            } else {
                return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
            }
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_NULL_ERROR);
        }

        logger.info("path = " + path);

        String finalPath = "";
        if (StringUtils.isNotBlank(path)) {
            finalPath = fileResource.getOssHost() + path;
        } else {
            return GraceJSONResult.errorCustom(ResponseStatusEnum.FILE_UPLOAD_FAILD);
        }
        // 文件审核代码，因为未开通阿里云内容安全服务无法使用
        // return GraceJSONResult.ok(doAliImageReview(finalPath));
        return GraceJSONResult.ok(finalPath);
    }

    @Override
    public GraceJSONResult uploadToGridFS(NewAdminBO newAdminBO) throws Exception {
        // 获得图片的base64字符串
        String base64 = newAdminBO.getImg64();
        // 将base64字符串转换为byte数组
        byte[] bytes = new BASE64Decoder().decodeBuffer(base64.trim());
        // 转换为输入流
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(bytes);
        // 将admin用户名（唯一）.png作为文件名上传至GridFS Bucket中
        ObjectId fileId = gridFSBucket.uploadFromStream(newAdminBO.getUsername() + ".png", byteArrayInputStream);
        // 获得文件在GridFS的主键id
        String fileIdStr = fileId.toString();
        // 返回文件在GridFS的主键id
        return GraceJSONResult.ok(fileIdStr);
    }

    @Override
    public void readInGridFS(String faceId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception {
        // 0. 判断参数
        if (StringUtils.isBlank(faceId) || faceId.equals("null")) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        // 1. 从GridFS中读取
        File adminFace = readGridFSByFaceId(faceId);

        // 2. 把人脸图片输出到浏览器
        FileUtils.downloadFileByStream(response, adminFace);
    }

    private String doAliImageReview(String pendingImageUrl) {
        boolean result = false;

        /**
         * 自动检测文件
         */
        try {
            result = aliImageReviewUtils.reviewImage(pendingImageUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }

        /**
         * 如检测失败，返回一个自定义的错误文件
         */
        if (!result) {
            return FAILED_IMAGE_URL;
        }

        return pendingImageUrl;
    }

    private File readGridFSByFaceId(String faceId) throws Exception {
        GridFSFindIterable gridFSFiles = gridFSBucket.find(Filters.eq("_id", new ObjectId(faceId)));
        GridFSFile gridFSFile = gridFSFiles.first();
        if (gridFSFile == null) {
            GraceException.display(ResponseStatusEnum.FILE_NOT_EXIST_ERROR);
        }

        String fileName = gridFSFile.getFilename();

        // 获取文件流，保存文件到本地或者服务器的临时目录
        File fileTemp = new File("/Users/wuxunmin/Documents/xunmiwe-news/temp_face");
        if (!fileTemp.exists()) {
            fileTemp.mkdirs();
        }
        File myFile = new File("/Users/wuxunmin/Documents/xunmiwe-news/temp_face/" + fileName);

        // 创建文件输出流
        OutputStream os = new FileOutputStream(myFile);
        // 下载到服务器或者本地
        gridFSBucket.downloadToStream(new ObjectId(faceId), os);
        return myFile;
    }
}
