package com.goldlion.core.middleware.oss.web;

import com.goldlion.core.common.exception.ExcelException;
import com.goldlion.core.middleware.oss.OssClientSingleton;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * OSS控制器
 */
public abstract class OssController {

    @Operation(summary = "上传公有文件")
    @PostMapping(value = "/upload/publicFiles", produces = MediaType.TEXT_PLAIN_VALUE)
    public String uploadPublicFiles(@RequestParam("file") MultipartFile file,
                                    @RequestParam(name = "isFullUrl", required = false, defaultValue = "false") boolean isFullUrl) throws IOException {
        if (file.isEmpty()) {
            throw new ExcelException("请选择一个文件上传！");
        }

        String objectName = OssClientSingleton.uploadPublicFiles(file.getOriginalFilename(), file.getInputStream());
        if (isFullUrl) {
            return OssClientSingleton.generatePublicBucketUrl(objectName, false);
        } else {
            return objectName;
        }
    }

    @Operation(summary = "上传私有文件")
    @PostMapping(value = "/upload/privateFiles", produces = MediaType.TEXT_PLAIN_VALUE)
    public String uploadPrivateFiles(@RequestParam("file") MultipartFile file,
                                     @RequestParam("fileDirectory") String fileDirectory,
                                     @RequestParam(name = "isFullUrl", required = false, defaultValue = "false") boolean isFullUrl) throws IOException {
        if (file.isEmpty()) {
            throw new ExcelException("请选择一个文件上传！");
        }

        String objectName = OssClientSingleton.uploadPrivateFiles(fileDirectory, file.getOriginalFilename(), file.getInputStream());
        if (isFullUrl) {
            return OssClientSingleton.generatePrivateBucketUrl(objectName, false);
        } else {
            return objectName;
        }
    }

    @Operation(summary = "生成公有存储桶URL")
    @GetMapping(value = "/generate/publicUrl", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generatePublicBucketUrl(@RequestParam("objectName") String objectName,
                                          @RequestParam(name = "isAcl", required = false, defaultValue = "false") boolean isAcl) {
        return OssClientSingleton.generatePublicBucketUrl(objectName, isAcl);
    }

    @Operation(summary = "生成私有存储桶URL")
    @GetMapping(value = "/generate/privateUrl", produces = MediaType.TEXT_PLAIN_VALUE)
    public String generatePrivateBucketUrl(@RequestParam("objectName") String objectName,
                                           @RequestParam(name = "isAcl", required = false, defaultValue = "false") boolean isAcl) {
        return OssClientSingleton.generatePrivateBucketUrl(objectName, isAcl);
    }

}
