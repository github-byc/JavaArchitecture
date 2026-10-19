package com.goldlion.core.middleware.oss.client;

import cn.hutool.core.convert.Convert;
import cn.hutool.extra.spring.SpringUtil;
import com.aliyun.oss.*;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.common.comm.SignVersion;
import com.aliyun.oss.model.*;
import com.goldlion.core.common.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * OSS客户端-阿里云实现
 */
@Slf4j
public class OssClientAliYun implements OssClient {
    private static final String ENDPOINT = SpringUtil.getProperty("goldlion.oss.aliyun.endpoint");
    private static final String REGION = SpringUtil.getProperty("goldlion.oss.aliyun.region");
    private static final String ACCESS_KEY_ID = SpringUtil.getProperty("goldlion.oss.aliyun.accessKeyId");
    private static final String SECRET_ACCESS_KEY = SpringUtil.getProperty("goldlion.oss.aliyun.secretAccessKey");
    private static final String BUCKET_NAME_PUBLIC = SpringUtil.getProperty("goldlion.oss.aliyun.bucketNamePublic");
    private static final String BUCKET_NAME_PRIVATE = SpringUtil.getProperty("goldlion.oss.aliyun.bucketNamePrivate");
    private static final String DEFAULT_PUBLIC_DIRECTORY = SpringUtil.getProperty("goldlion.oss.aliyun.defaultPublicDirectory");
    private static final String EXPIRATION = SpringUtil.getProperty("goldlion.oss.aliyun.expiration");
    private static final OSS INSTANCE = create();

    private static OSS create() {
        try {
            CredentialsProvider credentialsProvider = CredentialsProviderFactory.newDefaultCredentialProvider(ACCESS_KEY_ID, SECRET_ACCESS_KEY);

            ClientBuilderConfiguration clientBuilderConfiguration = new ClientBuilderConfiguration();
            clientBuilderConfiguration.setSignatureVersion(SignVersion.V4);

            // 构建OSS客户端
            return OSSClientBuilder.create()
                    .endpoint(ENDPOINT)
                    .credentialsProvider(credentialsProvider)
                    .clientConfiguration(clientBuilderConfiguration)
                    .region(REGION)
                    .build();
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new BusinessException("OSS客户端初始化失败");
        }
    }

    @Override
    public String getEndpoint() {
        return ENDPOINT;
    }

    @Override
    public String getPublicBucketName() {
        return BUCKET_NAME_PUBLIC;
    }

    @Override
    public String getPrivateBucketName() {
        return BUCKET_NAME_PRIVATE;
    }

    @Override
    public String getDefaultPublicDirectory() {
        return DEFAULT_PUBLIC_DIRECTORY;
    }

    @Override
    public long getExpiration() {
        return Convert.toLong(EXPIRATION);
    }

    /**
     * @see <a href="https://help.aliyun.com/zh/oss/developer-reference/simple-upload-11#p-yqj-z1w-rl2">上传文件流</a>
     */
    @Override
    public PutObjectResult uploadFiles(String bucketName, String objectName, InputStream inputStream) {
        try {
            // 创建PutObjectRequest对象
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, objectName, inputStream);
            // 创建PutObject请求
            return INSTANCE.putObject(putObjectRequest);
        } catch (OSSException oe) {
            log.error(oe.getErrorMessage());
            throw new BusinessException("OSS上传文件失败");
        } catch (ClientException ce) {
            log.error(ce.getErrorMessage());
            throw new BusinessException("OSS上传文件失败");
        }
    }

    /**
     * @see <a href="https://help.aliyun.com/zh/oss/developer-reference/download-using-a-presigned-url#c7461f0d8fu6b">使用预签名URL下载</a>
     */
    @Override
    public String generatePresignedUrl(String bucketName, String objectName, Date expiration) {
        URL url = INSTANCE.generatePresignedUrl(bucketName, objectName, expiration);
        return url.toString();
    }

    /**
     * @see <a href="https://help.aliyun.com/zh/oss/developer-reference/manage-the-acl-of-a-bucket#section-9vy-z0n-kix">获取存储空间读写权限</a>
     */
    @Override
    public boolean getBucketAcl(String bucketName) {
        // 获取存储空间的读写权限。
        AccessControlList accessControlList = INSTANCE.getBucketAcl(bucketName);
        return !CannedAccessControlList.Private.equals(accessControlList.getCannedACL());

    }

    /**
     * @see <a href="https://help.aliyun.com/zh/oss/developer-reference/manage-object-acls-2#section-3uo-9uy-h8c">获取Object访问权限</a>
     */
    @Override
    public boolean getObjectAcl(String bucketName, String objectName) {
        // 获取Object的访问权限。
        ObjectAcl objectAcl = INSTANCE.getObjectAcl(bucketName, objectName);
        if (ObjectPermission.Default.equals(objectAcl.getPermission())) {
            return this.getBucketAcl(bucketName);
        } else {
            return !ObjectPermission.Private.equals(objectAcl.getPermission());
        }
    }

}
