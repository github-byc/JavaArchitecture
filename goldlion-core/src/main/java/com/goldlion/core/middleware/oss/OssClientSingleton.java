package com.goldlion.core.middleware.oss;

import cn.hutool.cache.Cache;
import cn.hutool.cache.CacheUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.lang.Singleton;
import cn.hutool.core.net.url.UrlBuilder;
import cn.hutool.core.text.StrPool;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import cn.hutool.extra.spring.SpringUtil;
import com.goldlion.core.common.exception.BusinessException;
import com.goldlion.core.middleware.oss.client.OssClient;
import com.goldlion.core.middleware.oss.client.OssClientAliYun;
import com.goldlion.core.middleware.oss.rediskey.OssDataCacheKey;
import com.goldlion.core.middleware.redis.RedisOperator;
import lombok.extern.slf4j.Slf4j;

import java.io.InputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * OSS单例模式配置
 */
@Slf4j
public class OssClientSingleton {
    private static final Map<String, String> PUBLIC_HOST_LOCAL_CACHE = new HashMap<>();
    private static final Cache<String, String> PRIVATE_URL_LOCAL_CACHE = CacheUtil.newLRUCache(1024);

    private OssClientSingleton() {
    }

    // 获取单例实例
    public static OssClient getInstance() {
        return Singleton.get(OssClientAliYun.class);
    }

    //==================================================================================================================

    /**
     * 上传公有文件
     */
    public static String uploadPublicFiles(String fileName, InputStream inputStream) {
        return uploadFiles(getInstance().getPublicBucketName(), getInstance().getDefaultPublicDirectory(), fileName, inputStream);
    }

    /**
     * 上传私有文件
     */
    public static String uploadPrivateFiles(String fileDirectory, String fileName, InputStream inputStream) {
        return uploadFiles(getInstance().getPrivateBucketName(), fileDirectory, fileName, inputStream);
    }

    /**
     * 生成公有存储桶URL
     */
    public static String generatePublicBucketUrl(String objectName, boolean isAcl) {
        if (isAcl) {
            return generateAclUrl(getInstance().getPublicBucketName(), objectName);
        }
        return generatePublicUrl(getInstance().getPublicBucketName(), objectName);
    }

    /**
     * 生成私有存储桶URL
     */
    public static String generatePrivateBucketUrl(String objectName, boolean isAcl) {
        if (isAcl) {
            return generateAclUrl(getInstance().getPrivateBucketName(), objectName);
        }
        return generatePrivateUrl(getInstance().getPrivateBucketName(), objectName);
    }

    //==================================================================================================================

    private static String uploadFiles(String bucketName, String fileDirectory, String fileName, InputStream inputStream) {
        if (StrUtil.isBlank(bucketName)) {
            throw new BusinessException("OSS存储桶名不能为空");
        }
        if (StrUtil.isBlank(fileDirectory)) {
            throw new BusinessException("OSS文件目录不能为空");
        }
        if (StrUtil.isBlank(fileName)) {
            throw new BusinessException("OSS文件名不能为空");
        }

        String objectName = getObjectName(fileDirectory, fileName);

        getInstance().uploadFiles(bucketName, objectName, inputStream);

        return objectName;
    }

    private static String getObjectName(String fileDirectory, String fileName) {
        String pathStr = UrlBuilder.of().addPath(fileDirectory).addPath(fileName).getPathStr();
        // 去除开头的\或者/
        return pathStr.replaceAll("^[\\\\/]+", StrUtil.EMPTY);
    }

    /**
     * 根据Acl权限生成公有或私有的URL
     */
    private static String generateAclUrl(String bucketName, String objectName) {
        if (getInstance().getObjectAcl(bucketName, objectName)) {
            return generatePublicUrl(bucketName, objectName);
        } else {
            return generatePrivateUrl(bucketName, objectName);
        }
    }

    private static String generatePublicUrl(String bucketName, String objectName) {
        if (!PUBLIC_HOST_LOCAL_CACHE.containsKey(bucketName)) {

            UrlBuilder urlBuilder = UrlBuilder.ofHttp(getInstance().getEndpoint());

            PUBLIC_HOST_LOCAL_CACHE.put(bucketName, urlBuilder
                    .setHost(bucketName + StrPool.DOT + urlBuilder.getHost())
                    .build());
        }

        String url = UrlBuilder.ofHttp(PUBLIC_HOST_LOCAL_CACHE.get(bucketName)).addPath(objectName).build();
        log.info("generatePublicUrl: {}", url);
        return url;
    }

    private static String generatePrivateUrl(String bucketName, String objectName) {
        String key = DigestUtil.md5Hex16(UrlBuilder.of().addPath(bucketName).addPath(objectName).getPathStr());

        // 先查询本地缓存
        String localCache = PRIVATE_URL_LOCAL_CACHE.get(key);
        if (StrUtil.isNotBlank(localCache)) {
            return localCache;
        }

        // 再查Redis缓存
        RedisOperator redisOperator = SpringUtil.getBean(RedisOperator.class);
        Object redisCache = redisOperator.getValue(OssDataCacheKey.PRESIGNED_URL, key);
        if (Objects.nonNull(redisCache)) {
            String url = Convert.toStr(redisCache);
            PRIVATE_URL_LOCAL_CACHE.put(key, url, (60 * 1000));
            return url;
        }

        // 设置预签名URL过期时间，单位为毫秒
        Date expiration = new Date(new Date().getTime() + getInstance().getExpiration());
        String url = getInstance().generatePresignedUrl(bucketName, objectName, expiration);

        redisOperator.setValue(OssDataCacheKey.PRESIGNED_URL, key, url);
        PRIVATE_URL_LOCAL_CACHE.put(key, url, (60 * 1000));

        log.info("generatePrivateUrl: {}", url);
        return url;
    }

}
