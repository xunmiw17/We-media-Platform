package com.xunmiw.api.controller.files;

import com.xunmiw.grace.result.GraceJSONResult;
import com.xunmiw.pojo.bo.NewAdminBO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Api(value = "文件上传的Controller", tags = {"文件上传的Controller"})
@RequestMapping("fs")
public interface FileUploadControllerApi {

    @ApiOperation(value = "上传用户头像", notes = "上传用户头像", httpMethod = "POST")
    @PostMapping("/uploadFace")
    public GraceJSONResult uploadFace(@RequestParam String userId, MultipartFile file) throws Exception;

    /**
     * 文件上传到MongoDB的GridFS中
     * @param newAdminBO
     * @return
     */
    @PostMapping("/uploadToGridFS")
    public GraceJSONResult uploadToGridFS(@RequestBody NewAdminBO newAdminBO) throws Exception;

    /**
     * 从GridFS中读取图片内容
     * @param faceId
     * @return
     */
    @GetMapping("/readInGridFS")
    public void readInGridFS(@RequestParam String faceId,
                             HttpServletRequest request,
                             HttpServletResponse response) throws Exception;

    /**
     * 给admin模块（而不是前端）调用的接口
     * 从GridFS中读取图片内容，并且返回base64
     * @param faceId
     * @param request
     * @param response
     * @return
     * @throws Exception
     */
    @GetMapping("/readFace64InGridFS")
    public GraceJSONResult readFace64InGridFS(@RequestParam String faceId,
                                              HttpServletRequest request,
                                              HttpServletResponse response) throws Exception;
}
