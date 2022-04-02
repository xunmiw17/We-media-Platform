package com.xunmiw.files.controller;

import com.xunmiw.api.controller.files.FileUploadControllerApi;
import com.xunmiw.files.resource.FileResource;
import com.xunmiw.files.service.UploadService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import com.xunmiw.utils.extend.AliImageReviewUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
}
