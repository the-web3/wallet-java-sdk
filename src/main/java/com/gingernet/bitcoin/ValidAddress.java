package com.gingernet.bitcoin;

import org.apache.commons.codec.digest.DigestUtils;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Arrays;

public class ValidAddress {

    private static final String ALPHABET = "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz";
    private static final BigInteger ALPHABET_SIZE = BigInteger.valueOf(ALPHABET.length());

    /**
     * 将 Base58Check 字符串反转为 byte 数组
    */
    static byte[] base58ToRawBytes(String s) {
        // Parse base-58 string
        BigInteger num = BigInteger.ZERO;
        for (int i = 0; i < s.length(); i++) {
            num = num.multiply(ALPHABET_SIZE);
            int digit = ALPHABET.indexOf(s.charAt(i));
            if (digit == -1) {
                throw new IllegalArgumentException("Invalid character for Base58Check");
            }
            num = num.add(BigInteger.valueOf(digit));
        }
        // Strip possible leading zero due to mandatory sign bit
        byte[] b = num.toByteArray();
        if (b[0] == 0) {
            b = Arrays.copyOfRange(b, 1, b.length);
        }
        try {
            // Convert leading '1' characters to leading 0-value bytes
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            for (int i = 0; i < s.length() && s.charAt(i) == ALPHABET.charAt(0); i++) {
                buf.write(0);
            }
            buf.write(b);
            return buf.toByteArray();
        } catch (IOException e) {
            throw new AssertionError(e);
        }
    }

    /**
     * 双重Hash
     */
    public static byte[] doubleHash(byte[] data) {
        return DigestUtils.sha256(DigestUtils.sha256(data));
    }


    /**
     * @Title: base58ToBytes   验证钱包地址
     * @param @param s
     * @param @return    参数
     * @return byte[]    返回类型
     * @throws
     */
    public static boolean base58ToBytes(String s) {
        byte[] concat = base58ToRawBytes(s);
        byte[] data = Arrays.copyOf(concat, concat.length - 4);
        byte[] hash = Arrays.copyOfRange(concat, concat.length - 4, concat.length);
        byte[] rehash = Arrays.copyOf(doubleHash(data), 4);
        if (!Arrays.equals(rehash, hash)) {
            throw new IllegalArgumentException("Checksum mismatch");
        }
        return true;
    }

    /*
     * address: 地址
     * flag: 主链/测试链
     */
    public static boolean validAddress(String address,boolean flag) {
        try {
            NetworkParameters networkParameters = null;
            if (!flag)
                networkParameters = MainNetParams.get();
            else
                networkParameters = TestNet3Params.get();
            org.bitcoinj.core.Address addr = org.bitcoinj.core.Address.fromBase58(networkParameters, address);
            if (addr != null)
                return true;
            else
                return false;
        } catch (Exception e) {
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println(base58ToBytes("bc1q0mpzvyr7wrakn3lanc3lwfl4ey2696kh8pjwru"));
    }
}
