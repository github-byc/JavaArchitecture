package com.goldlion.core.middleware.oss.client;

import com.aliyun.oss.model.PutObjectResult;

import java.io.InputStream;
import java.util.Date;

/**
 * OSS客户端
 */
public interface OssClient {

    /**
     * 获取Endpoint
     */
    String getEndpoint();

    /**
     * 获取公有存储桶名
     */
    String getPublicBucketName();

    /**
     * 获取私有存储桶名
     */
    String getPrivateBucketName();

    /**
     * 获取默认公有目录
     */
    String getDefaultPublicDirectory();

    /**
     * 获取过期时间，单位为毫秒
     */
    long getExpiration();

    /**
     * 上传文件
     *
     * @param bucketName  填写Bucket名称
     * @param objectName  填写Object完整路径，完整路径中不能包含Bucket名称
     * @param inputStream 上传文件流
     */
    PutObjectResult uploadFiles(String bucketName, String objectName, InputStream inputStream);

    /**
     * 生成预签名URL
     *
     * @param bucketName 填写Bucket名称
     * @param objectName 填写Object完整路径，完整路径中不能包含Bucket名称
     * @param expiration 设置预签名URL过期时间，单位为毫秒
     */
    String generatePresignedUrl(String bucketName, String objectName, Date expiration);

    /**
     * 获取存储空间权限
     *
     * @param bucketName 填写Bucket名称
     * @return true=公有存储空间;false=私有存储空间
     */
    boolean getBucketAcl(String bucketName);

    /**
     * 获取文件权限
     *
     * @param bucketName 填写Bucket名称
     * @param objectName 填写Object完整路径，完整路径中不能包含Bucket名称
     * @return true=公有文件;false=私有文件
     */
    boolean getObjectAcl(String bucketName, String objectName);

}
