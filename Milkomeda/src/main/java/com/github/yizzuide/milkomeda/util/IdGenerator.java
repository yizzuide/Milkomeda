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

package com.github.yizzuide.milkomeda.util;

import org.apache.commons.lang3.RandomStringUtils;

import java.security.SecureRandom;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * IdGenerator
 *
 * @author yizzuide
 * @since 3.20.0
 * Create at 2024/01/12 23:17
 */
public class IdGenerator {

    private static final char[] CHARS = new char[] {
            'q', 'w', 'e', '8', 'a', 's', '2', 'd', 'z', 'x', '9',
            'c', '7', 'p', '5', 'i', 'k', '3', 'm', 'j', 'u', 'f',
            'r', '4', 'v', 'y', 'l', 't', 'n', '6', 'b', 'g', 'h'};

    private static final String ALPHA_NUMS = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private static final String CAPITAL_ALPHA = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final String RECOGNIZABLE_NUMBERS = "23456789";

    /**
     * Generate invitation code.
     * @param id ID
     * @return  invitation code
     */
    public static String genInviterCodeWithId(long id) {
        char[] buf = new char[32];
        int charPos = 32;
        int binLen = CHARS.length;
        int s = 6;
        char b = 'o';
        while ((id / binLen) > 0) {
            int ind = (int) (id % binLen);
            buf[--charPos] = CHARS[ind];
            id /= binLen;
        }
        buf[--charPos] = CHARS[(int) (id % binLen)];
        String str = new String(buf, charPos, (32 - charPos));
        // 不够长度的自动随机补全
        if (str.length() < s) {
            StringBuilder sb = new StringBuilder();
            sb.append(b);
            Random rnd = new Random();
            for (int i = 1; i < s - str.length(); i++) {
                sb.append(CHARS[rnd.nextInt(binLen)]);
            }
            str += sb.toString();
        }
        return str.toUpperCase();
    }

    /**
     * Decode invitation code.
     * @param code invitation code
     * @return  ID
     */
    public static long decodeInviterCode(String code) {
        code = code.toLowerCase();
        char[] chs = code.toCharArray();
        long res = 0L;
        int binLen = CHARS.length;
        char b = 'o';
        for (int i = 0; i < chs.length; i++) {
            int ind = 0;
            for (int j = 0; j < binLen; j++) {
                if (chs[i] == CHARS[j]) {
                    ind = j;
                    break;
                }
            }
            if (chs[i] == b) {
                break;
            }
            if (i > 0) {
                res = res * binLen + ind;
            } else {
                res = ind;
            }
        }
        return res;
    }

    /**
     * Generate 32-bit ID
     * @return ID
     */
    public static String genNext32ID() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
        String dateStr = sdf.format(new Date());
        String unixTime = String.valueOf(System.currentTimeMillis() / 1000);
        String second = unixTime.substring(unixTime.length() - 6);
        String randNum = RandomStringUtils.randomNumeric(18);
        return String.format("%s%s%s", dateStr, second, randNum);
    }

    /**
     * Generate sequence ID
     * @param seq  sequence
     * @return  ID
     */
    public static String genSeqID(long seq, boolean timeStart) {
        int length = 16;
        String now = String.valueOf(System.currentTimeMillis());
        String first;
        if (timeStart) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
            first = sdf.format(new Date());
        } else {
            first = RandomStringUtils.random(6, RECOGNIZABLE_NUMBERS);
        }
        String last = String.valueOf(seq);
        int fillLen = length - (first.length() + last.length());
        String fill = "";
        if (fillLen > 0) {
            fill = now.substring(now.length() - fillLen);
        }
        return first + fill + last;
    }

    /**
     * Generate nickname
     * @return  nickname
     */
    public static String genNickname() {
        return String.format("%s%s", RandomStringUtils.random(1, CAPITAL_ALPHA), RandomStringUtils.randomAlphanumeric(11));
    }

    /**
     * Generate redeem code
     * @return  redeem code
     */
    public static String genRedeemCode() {
        return String.format("%s%s", RandomStringUtils.randomAlphabetic(4), RandomStringUtils.random(5, RECOGNIZABLE_NUMBERS));
    }

    public static String genRand(int len) {
        StringBuilder sb = new StringBuilder(len);
        SecureRandom random = new SecureRandom();
        for (int i = 0; i < len; i++) {
            int index = random.nextInt(ALPHA_NUMS.length());
            sb.append(ALPHA_NUMS.charAt(index));
        }
        return sb.toString();
    }
}