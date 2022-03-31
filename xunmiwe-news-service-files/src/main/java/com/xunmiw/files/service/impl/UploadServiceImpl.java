package com.xunmiw.files.service.impl;

import com.github.tobato.fastdfs.domain.fdfs.StorePath;
import com.github.tobato.fastdfs.service.FastFileStorageClient;
import com.xunmiw.files.resource.FileResource;
import com.xunmiw.files.service.UploadService;
import com.xunmiw.utils.extend.AliyunCloudResource;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.OSSException;
import java.io.ByteArrayInputStream;

@Service
public class UploadServiceImpl implements UploadService {

    @Autowired
    private FastFileStorageClient fastFileStorageClient;

    @Autowired
    private FileResource fileResource;

    @Autowired
    private AliyunCloudResource aliyunCloudResource;

    @Autowired
    private Sid sid;

    @Override
    public String uploadFdfs(MultipartFile file, String fileExt) throws Exception {
        StorePath storePath = fastFileStorageClient.uploadFile(file.getInputStream(),
                                                                file.getSize(),
                                                                fileExt,
                                                    null);

        return storePath.getFullPath();
    }

    @Override
    public String uploadOss(MultipartFile file, String userId, String fileExt) throws Exception {
        String endpoint = fileResource.getEndpoint();
        // 阿里云账号AccessKey拥有所有API的访问权限，风险很高。强烈建议您创建并使用RAM用户进行API访问或日常运维，请登录RAM控制台创建RAM用户。
        String accessKeyId = aliyunCloudResource.getAccessKeyID();
        String accessKeySecret = aliyunCloudResource.getAccessKeySecret();
        // 填写Bucket名称，例如examplebucket。
        String bucketName = fileResource.getBucketName();
        // 填写Object完整路径，完整路径中不能包含Bucket名称，例如exampledir/exampleobject.txt。
        String objectName = fileResource.getObjectName();

        // 创建OSSClient实例。
        OSS ossClient = new OSSClientBuilder().build(endpoint, accessKeyId, accessKeySecret);

        try {
            // 填写Byte数组。
            byte[] content = file.getBytes();

            // 生成随机ID，作为文件名
            String fileName = sid.nextShort();
            String myObjectName = objectName + "/" + userId + "/" + fileName + "." + fileExt;

            // 创建PutObject请求。
            ossClient.putObject(bucketName, myObjectName, new ByteArrayInputStream(content));
            return myObjectName;
        } catch (OSSException oe) {
            System.out.println("Caught an OSSException, which means your request made it to OSS, "
                    + "but was rejected with an error response for some reason.");
            System.out.println("Error Message:" + oe.getErrorMessage());
            System.out.println("Error Code:" + oe.getErrorCode());
            System.out.println("Request ID:" + oe.getRequestId());
            System.out.println("Host ID:" + oe.getHostId());
        } catch (ClientException ce) {
            System.out.println("Caught an ClientException, which means the client encountered "
                    + "a serious internal problem while trying to communicate with OSS, "
                    + "such as not being able to access the network.");
            System.out.println("Error Message:" + ce.getMessage());
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
        return null;
    }
}
