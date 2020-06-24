package com.github.yizzuide.milkomeda.universe.algorithm.hash;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.function.Consumer;

/**
 * KetamaHash算法：区别于其它Hash算法，针对高效的一致性哈希算法方式，超高负载平衡性，高不变流量
 *
 * @author yizzuide
 * @since 3.8.0
 * Create at 2020/06/18 17:37
 */
public class KetamaHash implements ConsistentHashFunc {

    private static final MessageDigest md5Digest;

    static {
        try {
            md5Digest = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 not supported", e);
        }
    }

    @Override
    public long hash(Object key) {
        return rawHash(key);
    }

    @Override
    public long rawHash(Object key) {
        byte[] bKey = computeMd5(key.toString());
        return ((long) (bKey[3] & 0xFF) << 24) |
                ((long) (bKey[2] & 0xFF) << 16) |
                ((long) (bKey[1] & 0xFF) << 8) |
                ((long) (bKey[0] & 0xFF));
    }

    @Override
    public int balanceFactor() {
        return 4;
    }

    @Override
    public void balanceHash(Object key, Consumer<Long> generator) {
        byte[] digest = computeMd5(key.toString());
        int factor = balanceFactor();
        for (int h = 0; h < factor; h++) {
            long hk = ((long) (digest[3 + h * factor] & 0xFF) << 24) |
                    ((long) (digest[2 + h * factor] & 0xFF)) << 16 |
                    ((long) (digest[1 + h * factor] & 0xFF)) << 8 |
                    (digest[h * factor] & 0xFF);
            generator.accept(hk);
        }
    }

    private static byte[] computeMd5(String k) {
        MessageDigest md5;
        try {
            md5 = (MessageDigest) md5Digest.clone();
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException("clone of MD5 not supported", e);
        }
        md5.update(k.getBytes());
        return md5.digest();
    }
}
