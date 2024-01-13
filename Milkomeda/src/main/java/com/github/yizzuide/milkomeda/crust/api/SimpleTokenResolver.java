/*
 * Copyright (c) 2024 yizzuide All rights Reserved.
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

package com.github.yizzuide.milkomeda.crust.api;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.codec.EncodingException;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;
import javax.crypto.spec.IvParameterSpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * SimpleTokenProvider
 *
 * @since 4.0.0
 * @author yizzuide
 * Create at 2024/01/05 12:13
 */
@Slf4j
public class SimpleTokenResolver {
    private static final String CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    /**
     * Generate token with login user id and timestamp.
     * @param uid   login user id
     * @param timestamp expire timestamp
     * @return token
     */
    public static String createToken(long uid, long timestamp) {
        return createToken(uid, timestamp, null, 5);
    }

    /**
     * Generate token with login user id and timestamp.
     * @param uid   login user id
     * @param timestamp expire timestamp
     * @param rand  random string
     * @param randLength  if `rand` set is null, random string length must be set.
     * @return token
     */
    public static String createToken(long uid, long timestamp, String rand, int randLength) {
        if (rand == null) {
            rand = generateRandomString(randLength);
        }
        return _3DESEncoder.encode(new TokenData(String.valueOf(uid), timestamp, rand).toString());
    }

    public static TokenData parseToken(String token) {
        String plaintext = _3DESEncoder.decode(token);
        if (plaintext == null) {
            return null;
        }
        String[] parts = plaintext.split(":");
        String userId = parts[0];
        long timestamp = Long.parseLong(parts[1]);
        String rand = parts[2];
        return new TokenData(userId, timestamp, rand);
    }

    private static String generateRandomString(int length) {
        StringBuilder sb = new StringBuilder(length);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < length; i++) {
            int index = random.nextInt(CHARS.length());
            sb.append(CHARS.charAt(index));
        }
        return sb.toString();
    }

    private static final class _3DESEncoder {
        private static final String SECRET_KEY = "&cru#st@~kai--huang0!!%91";
        private static final String IV = "(IV)~%52";

        private static final String ALGORITHM = "DESede";

        private static final String FULL_ALGORITHM = "DESede/CBC/PKCS5Padding";

        public static String encode(String data) {
            try {
                DESedeKeySpec spec = new DESedeKeySpec(SECRET_KEY.getBytes());
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
                SecretKey key = keyFactory.generateSecret(spec);

                Cipher cipher = Cipher.getInstance(FULL_ALGORITHM);
                cipher.init(Cipher.ENCRYPT_MODE, key, new IvParameterSpec(IV.getBytes()));
                byte[] ciphertext = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));
                return Base64.getEncoder().encodeToString(ciphertext);
            } catch (Exception e) {
                throw new EncodingException("gen code error: " + e.getMessage());
            }
        }

        public static String decode(String encodeData) {
            try {
                DESedeKeySpec spec = new DESedeKeySpec(SECRET_KEY.getBytes());
                SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
                SecretKey key = keyFactory.generateSecret(spec);

                Cipher cipher = Cipher.getInstance(FULL_ALGORITHM);
                cipher.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV.getBytes()));
                byte[] ciphertext = Base64.getDecoder().decode(encodeData);
                byte[] plaintextBytes = cipher.doFinal(ciphertext);
                return new String(plaintextBytes, StandardCharsets.UTF_8);
            } catch (Exception e) {
                log.error("3DES decode error: {}", e.getMessage());
                return null;
            }
        }
    }

    @Data
    public static final class TokenData {
        /**
         * Login user id.
         */
        private String userId;

        /**
         * Token expire timestamp.
         */
        private long timestamp;

        /**
         * Random string.
         */
        private String rand;

        public TokenData(String userId, long timestamp, String rand) {
            this.userId = userId;
            this.timestamp = timestamp;
            this.rand = rand;
        }

        public String toString() {
            return userId + ":" + timestamp + ":" + rand;
        }
    }
}
