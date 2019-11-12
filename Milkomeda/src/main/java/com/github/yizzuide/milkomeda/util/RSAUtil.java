package com.github.yizzuide.milkomeda.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

/**
 * RSAUtil
 * RSA非对称加密工具类
 * 基本原理：同时生成两把密钥：私钥和公钥，私钥隐秘保存，公钥可以下发给信任客户端
 *  私钥加密，持有私钥或公钥才可以解密
 *  公钥加密，持有私钥才可解密
 *
 * @author yizzuide
 * @since 1.14.0
 * Create at 2019/11/11 15:05
 */
@Slf4j
public class RSAUtil {
    /**
     * 从文件中读取公钥
     *
     * @param filename 公钥保存路径，相对于classpath
     * @return 公钥对象
     * @throws Exception 获取异常
     */
    public static PublicKey getPublicKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPublicKey(bytes);
    }

    /**
     * 公钥字符串转PublicKey
     * @param base64String 公钥字符串
     * @return 公钥对象
     * @throws Exception 获取异常
     */
    public static PublicKey getPublicKey2(String base64String) throws Exception {
        byte[] bytes = Base64.decodeBase64(base64String);
        return getPublicKey(bytes);
    }

    /**
     * 从文件中读取密钥
     *
     * @param filename 私钥保存路径，相对于classpath
     * @return 私钥对象
     * @throws Exception 获取异常
     */
    public static PrivateKey getPrivateKey(String filename) throws Exception {
        byte[] bytes = readFile(filename);
        return getPrivateKey(bytes);
    }

    /**
     * 私钥字符串转PrivateKey
     * @param base64String 私钥字符串
     * @return 私钥对象
     * @throws Exception 获取异常
     */
    public static PrivateKey getPrivateKey2(String base64String) throws Exception {
        byte[] bytes = Base64.decodeBase64(base64String);
        return getPrivateKey(bytes);
    }

    /**
     * 获取公钥
     *
     * @param bytes 公钥的字节形式
     * @return PublicKey
     * @throws NoSuchAlgorithmException 获取算法异常
     * @throws InvalidKeySpecException 无效键异常
     */
    public static PublicKey getPublicKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec spec = new X509EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePublic(spec);
    }

    /**
     * 获取密钥
     *
     * @param bytes 私钥的字节形式
     * @return PrivateKey
     * @throws NoSuchAlgorithmException 获取算法异常
     * @throws InvalidKeySpecException 无效键异常
     */
    public static PrivateKey getPrivateKey(byte[] bytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(bytes);
        KeyFactory factory = KeyFactory.getInstance("RSA");
        return factory.generatePrivate(spec);
    }

    /**
     * 根据密文，生存rsa公钥和私钥, 并写入指定文件
     *
     * @param publicKeyFilename  公钥文件路径
     * @param privateKeyFilename 私钥文件路径
     * @param secret             生成密钥的密文
     * @throws IOException IO流异常
     * @throws NoSuchAlgorithmException 获取算法异常
     */
    public static void generateKey(String publicKeyFilename, String privateKeyFilename, String secret) throws IOException, NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        SecureRandom secureRandom = new SecureRandom(secret.getBytes());
        keyPairGenerator.initialize(1024, secureRandom);
        KeyPair keyPair = keyPairGenerator.genKeyPair();
        // 获取公钥并写出到文件
        byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        // 公钥转Base64字符串(这种方式可以摆脱文件的麻烦)
        String publicKeyString = Base64.encodeBase64String(publicKeyBytes);
        log.info("生成公钥串：{}", publicKeyString);

        writeFile(publicKeyFilename, publicKeyBytes);
        // 获取私钥并写出
        byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        writeFile(privateKeyFilename, privateKeyBytes);
        // 私钥转Base64字符串(这种方式可以摆脱文件的麻烦)
        String privateKeyString = Base64.encodeBase64String(privateKeyBytes);
        log.info("生成私钥串：{}", privateKeyString);
    }

    private static byte[] readFile(String fileName) throws IOException {
        return Files.readAllBytes(new File(fileName).toPath());
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private static void writeFile(String destPath, byte[] bytes) throws IOException {
        File dest = new File(destPath);
        File dir = dest.getParentFile();
        // 目录不存在就创建
        if (!dir.exists()) {
            dir.mkdirs();
        }
        // 创建新文件
        if (!dest.exists()) {
            dest.createNewFile();
        }
        Files.write(dest.toPath(), bytes);
    }
}
