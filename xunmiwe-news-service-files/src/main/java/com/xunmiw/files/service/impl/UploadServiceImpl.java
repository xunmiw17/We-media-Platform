package com.xunmiw.files.service.impl;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.xunmiw.files.service.UploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Override
    public String uploadFdfs(MultipartFile file, String fileExt) throws Exception {
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(),
                                                                file.getSize(),
                                                                fileExt,
                                                    null);

        return storePath.getFullPath();
    }
}
