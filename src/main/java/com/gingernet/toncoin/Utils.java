package com.gingernet.toncoin;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Utils {

    public static byte[] crc32c(byte[] bytes) {
        int[] ints = byteArrToIntArr(bytes);
        long int_crc = _crc32c(0, ints);
        int[] out = new int[4];
        uint32ToByteArrayLE(int_crc, out, 0);
        byte[] byteArr = intArrToByteArr(out);
        return byteArr;
    }

    public static void uint32ToByteArrayLE(long val, int[] out, int offset) {
        out[offset] = (int) (0xFF & val);
        out[offset + 1] = (int) (0xFF & (val >> 8));
        out[offset + 2] = (int) (0xFF & (val >> 16));
        out[offset + 3] = (int) (0xFF & (val >> 24));
    }

    private static long _crc32c(int crc, int[] bytes) {
        int POLY = 0x82f63b78;
        crc ^= 0xffffffff;
        for (int n = 0; n < bytes.length; n++) {
            crc ^= bytes[n];
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
            crc = (crc & 1) > 0 ? (crc >>> 1) ^ POLY : crc >>> 1;
        }
        return crc ^ 0xffffffff;
    }

    public static byte[] crc16(byte[] data) {
        int poly = 0x1021;
        int reg = 0;
        byte[] message = new byte[data.length + 2];
        System.arraycopy(data, 0, message, 0, data.length);
        for (byte b : message) {
            int mask = 0x80;
            while (mask > 0) {
                reg <<= 1;
                if ((b & mask) != 0) {
                    reg += 1;
                }
                mask >>= 1;
                if (reg > 0xffff) {
                    reg &= 0xffff;
                    reg ^= poly;
                }
            }
        }
        return new byte[]{(byte) Math.floor(reg / 256), (byte) (reg % 256)};
    }

    public static byte[] concatBytes(byte[] a, byte[] b) {
        byte[] c = new byte[a.length + b.length];
        System.arraycopy(a, 0, c, 0, a.length);
        System.arraycopy(b, 0, c, a.length, b.length);
        return c;
    }

    public static MessageDigest newDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean compareBytes(byte[] a, byte[] b) {
        if (a.length != b.length) {
            return false;
        }
        for (int i = 0; i < a.length; i++) {
            if (a[i] != b[i]) {
                return false;
            }
        }
        return true;
    }

    public static int readNBytesUIntFromArray(int n, byte[] ui8array) {
        int res = 0;
        for (int c = 0; c < n; c++) {
            res *= 256;
            res += ui8array[c];
        }
        return res;
    }

    public static int[] byteArrToIntArr(byte[] bytes) {
        int[] ints = new int[bytes.length];
        for (int i = 0; i < bytes.length; i++) {
            ints[i] = (bytes[i] & 0xff);
        }
        return ints;
    }

    public static byte[] intArrToByteArr(int[] ints) {
        byte[] bytes = new byte[ints.length];
        for (int i = 0; i < ints.length; i++) {
            bytes[i] = (byte) (ints[i] & 0xff);
        }
        return bytes;
    }

    public static byte[] byteSlice(byte[] bytes, int slice) {
        byte[] out = new byte[bytes.length - slice];
        System.arraycopy(bytes, slice, out, 0, bytes.length - slice);
        return out;
    }

    public static byte[] byteSliceRange(byte[] bytes, int from, int length) {
        byte[] out = new byte[length];
        System.arraycopy(bytes, from, out, 0, length);
        return out;
    }
}