/*
 * Copyright (c) 2021 yizzuide All rights Reserved.
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.github.yizzuide.milkomeda.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Objects;
import java.util.function.Function;

/**
 * EncryptUtil
 * 加解密工具类
 *
 * @author yizzuide
 * @since 1.13.0
 * @version 3.10.0
 * <br>
 * Create at 2019/09/21 19:58
 */
@Slf4j
public class EncryptUtil {

    /**
     * 编码格式
     */
    private static final String CHARSET = StandardCharsets.UTF_8.toString();
    /**
     * SHA128
     */
    public static final String SIGN_TYPE_RSA1 = "SHA1WithRSA";
    /**
     * SHA256
     */
    public static final String SIGN_TYPE_RSA2 = "SHA256WithRSA";
    /**
     * DES
     */
    public static final String SECURE_TYPE_DES = "DES";
    /**
     * AES
     */
    public static final String SECURE_TYPE_AES = "AES";

    /**
     * base64编码
     *
     * @param data 编码源
     * @return base64编码
     */
    public static String encode(byte[] data) {
        return Base64.encodeBase64String(data);
    }

    /**
     * base64解码
     *
     * @param data 解码源字符串
     * @return base64编码密文
     */
    public static byte[] decode(String data) {
        return Strings.isEmpty(data) ? null : Base64.decodeBase64(data);
    }

    /**
     * 生成aes秘钥
     *
     * @return aes对称密钥
     */
    public static String generateKey() {
        return generateKey(SECURE_TYPE_AES);
    }

    /**
     * 生成对称密钥
     * @param type  类型
     * @return 对称密钥
     */
    public static String generateKey(String type) {
        KeyGenerator keyGenerator;
        try {
            keyGenerator = KeyGenerator.getInstance(type);
        } catch (NoSuchAlgorithmException e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        keyGenerator.init(128);
        SecretKey secretKey = keyGenerator.generateKey();
        return encode(secretKey.getEncoded());
    }

    /**
     * AES (默认AES/ECB/PKCS5Padding)加密并返回Base64
     * @param aesKey base64密钥
     * @param content 需要加密的文本
     * @return base64编码
     */
    public static String encrypt(String aesKey, String content) {
        Key key = new SecretKeySpec(Objects.requireNonNull(decode(aesKey)), "aes");
        byte[] result;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // 加密模式
            cipher.init(Cipher.ENCRYPT_MODE, key);
            result = cipher.doFinal(content.getBytes(CHARSET));
        } catch (Exception e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return encode(result);
    }

    /**
     * AES (默认AES/ECB/PKCS5Padding)解密
     * @param aesKey base64密钥
     * @param encode64Content base64编码密文
     * @return 解码后的明文
     */
    public static String decrypt(String aesKey, String encode64Content) {
        Key key = new SecretKeySpec(Objects.requireNonNull(decode(aesKey)), "aes");
        String source;
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            // 解密模式
            cipher.init(Cipher.DECRYPT_MODE, key);
            byte[] result = cipher.doFinal(Objects.requireNonNull(decode(encode64Content)));
            source = new String(result, CHARSET);
        } catch (Exception e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return source;
    }

    public static void genKeyPair() {
        KeyPairGenerator keyPairGen;
        try {
            keyPairGen = KeyPairGenerator.getInstance("RSA");
            keyPairGen.initialize(1024, new SecureRandom());
            KeyPair keyPair = keyPairGen.generateKeyPair();
            String priKey = encode(keyPair.getPrivate().getEncoded());
            String pubKey = encode(keyPair.getPublic().getEncoded());
            System.out.println("priKey: " + priKey);
            System.out.println("pubKey: " + pubKey);
        } catch (NoSuchAlgorithmException e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
        }
    }

    /**
     * RAS公钥加密
     *
     * @param pubKey base64公钥
     * @param data   原始数据
     * @return 加密后的base64
     */
    public static String encryptByPublicKey(String pubKey, String data) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Objects.requireNonNull(decode(pubKey)));
        byte[] output;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.ENCRYPT_MODE, publicKey);
            output = blockCodec(cipher, Cipher.ENCRYPT_MODE, data.getBytes(CHARSET));
        } catch (Exception e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return encode(output);
    }

    /**
     * RSA私钥解密
     *
     * @param priKey base64私钥
     * @param data   加密后的base64
     * @return 原始数据
     */
    public static String decryptByPrivateKey(String priKey, String data) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Objects.requireNonNull(decode(priKey)));
        String source;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            cipher.init(Cipher.DECRYPT_MODE, privateKey);
            byte[] output = blockCodec(cipher, Cipher.DECRYPT_MODE, decode(data));
            source = new String(output, CHARSET);
        } catch (Exception e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return source;
    }

    /**
     * 数据分段加解密
     *
     * @param cipher    Cipher
     * @param mode      加解密模式
     * @param dataByte  数据源
     * @return 目标数据
     */
    private static byte[] blockCodec(Cipher cipher, int mode, byte[] dataByte) {
        int maxBlock;
        if (mode == Cipher.DECRYPT_MODE) {
            maxBlock = 128;
        } else {
            maxBlock = 117;
        }
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            int offSet = 0;
            byte[] buff;
            int i = 0;
            while (dataByte.length > offSet) {
                if (dataByte.length - offSet > maxBlock) {
                    buff = cipher.doFinal(dataByte, offSet, maxBlock);
                } else {
                    buff = cipher.doFinal(dataByte, offSet, dataByte.length - offSet);
                }
                out.write(buff, 0, buff.length);
                i++;
                offSet = i * maxBlock;
            }
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("加解密块大小为[" + maxBlock + "]时发生异常", e);
        }
    }

    /**
     * 生成签名
     *
     * @param data      签名数据
     * @param priKey    base64私钥
     * @param signType  签名类型，使用常量：SIGN_TYPE_RSA1、SIGN_TYPE_RSA2
     * @return 签名的字节数组
     * @since 3.10.0
     */
    public static byte[] signRaw(String data, String priKey, String signType) {
        PKCS8EncodedKeySpec pkcs8EncodedKeySpec = new PKCS8EncodedKeySpec(Objects.requireNonNull(decode(priKey)));
        byte[] sign;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(pkcs8EncodedKeySpec);

            Signature signature = Signature.getInstance(signType);
            signature.initSign(privateKey);
            signature.update(data.getBytes(CHARSET));
            sign = signature.sign();
        } catch (Exception e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return sign;
    }

    /**
     * 生成签名
     *
     * @param data      签名数据
     * @param priKey    base64私钥
     * @param signType  签名类型，使用常量：SIGN_TYPE_RSA1、SIGN_TYPE_RSA2
     * @return 签名的base64
     */
    public static String sign(String data, String priKey, String signType) {
        return encode(signRaw(data, priKey, signType));
    }

    /**
     * 验签
     * @param data   签名数据
     * @param sign   签名base64串
     * @param pubKey base64公钥
     * @param signType 签名类型，使用常量：SIGN_TYPE_RSA1、SIGN_TYPE_RSA2
     * @param decoder   base64解码器
     * @return 验证签名结果
     * @since 3.10.0
     */
    public static boolean verify(String data, String sign, String pubKey, String signType, Function<String, byte[]> decoder) {
        X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(Objects.requireNonNull(decode(pubKey)));
        boolean verify;
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = keyFactory.generatePublic(x509EncodedKeySpec);

            Signature signature = Signature.getInstance(signType);
            signature.initVerify(publicKey);
            signature.update(data.getBytes(CHARSET));
            verify = signature.verify(decoder.apply(sign));
        } catch (Exception e) {
            log.info("Encrypt happen exception: {}", e.getMessage(), e);
            throw new RuntimeException(e.getMessage());
        }
        return verify;
    }

    /**
     * 验签
     * @param data   签名数据
     * @param sign   签名base64串
     * @param pubKey base64公钥
     * @param signType 签名类型，使用常量：SIGN_TYPE_RSA1、SIGN_TYPE_RSA2
     * @return 验证签名结果
     */
    public static boolean verify(String data, String sign, String pubKey, String signType) {
        return verify(data, sign, pubKey, signType, EncryptUtil::decode);
    }
}
