package com.xunmiw.files.service;

import org.springframework.web.multipart.MultipartFile;

public interface UploadService {

    public String uploadFdfs(MultipartFile file, String fileExt) throws Exception;
}
