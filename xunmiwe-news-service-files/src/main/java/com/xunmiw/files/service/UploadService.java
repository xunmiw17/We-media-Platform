package com.xunmiw.files.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    /**
     * 使用FastDFS上传文件
     * @param file
     * @param fileExt
     * @return
     * @throws Exception
     */
    public String uploadFdfs(MultipartFile file, String fileExt) throws Exception;

    /**
     * 使用OSS上传文件
     * @param file
     * @param fileExt
     * @return
     * @throws Exception
     */
    public String uploadOss(MultipartFile file, String userId, String fileExt) throws Exception;
}
