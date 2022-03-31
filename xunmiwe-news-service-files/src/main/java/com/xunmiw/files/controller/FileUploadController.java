package com.xunmiw.files.controller;

import com.xunmiw.api.controller.files.FileUploadControllerApi;
import com.xunmiw.files.resource.FileResource;
import com.xunmiw.files.service.UploadService;
import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.grace.result.ResponseStatusEnum;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class FileUploadController implements FileUploadControllerApi {

    final static Logger logger = LoggerFactory.getLogger(FileUploadController.class);

    @Autowired
    private UploadService uploadService;

    @Autowired
    private FileResource fileResource;

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
        return GraceJSONResult.ok(finalPath);
    }
}
