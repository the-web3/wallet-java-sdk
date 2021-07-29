package com.gingernet.chia.proto;

import javax.crypto.Mac;
import javax.crypto.SecretKey;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.spec.KeySpec;

public class EIP2333 {
    public static byte[] derive_master_SK(byte[] seed) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        return HKDF_mod_r(seed, "".getBytes());
    }

    public static byte[] HKDF_mod_r(byte[] IKM, byte[] key_info) throws NoSuchAlgorithmException, UnsupportedEncodingException, InvalidKeyException {
        final int L = 48; // from EIP2333
        final byte[] f = { (byte) 0x73, (byte) 0xED, (byte) 0xA7, (byte) 0x53, (byte) 0x29, (byte) 0x9D, (byte) 0x7D, (byte) 0x48, (byte) 0x33, (byte) 0x39, (byte) 0xD8, (byte) 0x08, (byte) 0x09, (byte) 0xA1, (byte) 0xD8, (byte) 0x05, (byte) 0x53, (byte) 0xBD, (byte) 0xA4, (byte) 0x02, (byte) 0xFF, (byte) 0xFE, (byte) 0x5B, (byte) 0xFE, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01 };
        final BigInteger r = new BigInteger(f);
        byte[] salt = "BLS-SIG-KEYGEN-SALT-".getBytes();
        BigInteger SK = BigInteger.ZERO;
        while(SK.compareTo(BigInteger.ZERO) == 0){
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            salt = digest.digest(salt);

            byte[] i2osp1 = RFC3447.I2OSP(0, 1);
            byte[] intermediate1 = new byte[IKM.length + i2osp1.length];
            System.arraycopy(IKM, 0, intermediate1, 0, IKM.length);
            System.arraycopy(i2osp1, 0, intermediate1, IKM.length, i2osp1.length);
            byte[] PRK = RFC5869.HKDF_Extract_SHA256(salt, intermediate1);

            byte[] i2osp2 = RFC3447.I2OSP(L, 2);
            byte[] intermediate2 = new byte[i2osp2.length + key_info.length];
            System.arraycopy(key_info, 0, intermediate2, 0,  key_info.length);
            System.arraycopy(i2osp2, 0, intermediate2, key_info.length, i2osp2.length);

            //System.out.println("infokey_extended:" + Tester.bytesToHex(intermediate2) + " salt:" + Tester.bytesToHex(salt));
            //System.out.println("IKM extended:" + Tester.bytesToHex(intermediate1));

            byte[] OKM = RFC5869.HKDF_Expand_SHA256(PRK, intermediate2, L);
            BigInteger SKtmp = new BigInteger(1, OKM);
            SK = SKtmp.mod(r);
            //System.out.println("pre-mod-SK:" + SKtmp.toString() + " r:" + r.toString());
            //System.out.println("SK:" + SK.toString());
        }
        //System.out.println(SK.toString());
        return SK.toByteArray();
    }

    private static byte[][] IKM_to_lamport_SK(byte[] IKM, byte[] salt) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        int L = 32 * 255;
        byte[] PRK = RFC5869.HKDF_Extract_SHA256(salt, IKM);
        byte[] OKM = RFC5869.HKDF_Expand_SHA256(PRK, "".getBytes(), L);
        byte[][] SK = new byte[255][];
        for(int i = 0; i < 255; ++i){
            SK[i] = new byte[32];
            System.arraycopy(OKM, 32*i, SK[i], 0, 32);
        }
        return SK;
    }

    private static byte[] parent_SK_to_lamport_PK(byte[] parent_SK, long index) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        //System.out.println(index);
        byte[] salt = RFC3447.I2OSP(index, 4);
        //System.out.println(Tester.bytesToHex(salt));
        byte[] IKM = parent_SK;
        byte[][] lamport_0 = IKM_to_lamport_SK(IKM, salt);
        byte[] not_IKM = IKM.clone();
        for(int i = 0; i < not_IKM.length; ++i) not_IKM[i] = (byte) ~not_IKM[i];
        byte[][] lamport_1 = IKM_to_lamport_SK(not_IKM, salt);
        byte[] lamport_PK = new byte[0];
        for(int i = 0; i < 255; ++i){
            byte[] new_lamport_PK = new byte[lamport_PK.length + 32];
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(lamport_0[i]);
            System.arraycopy(lamport_PK, 0, new_lamport_PK, 0, lamport_PK.length);
            System.arraycopy(hash, 0, new_lamport_PK, lamport_PK.length, hash.length);
            lamport_PK = new_lamport_PK;
        }
        for(int i = 0; i < 255; ++i){
            byte[] new_lamport_PK = new byte[lamport_PK.length + 32];
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(lamport_1[i]);
            System.arraycopy(lamport_PK, 0, new_lamport_PK, 0, lamport_PK.length);
            System.arraycopy(hash, 0, new_lamport_PK, lamport_PK.length, hash.length);
            lamport_PK = new_lamport_PK;
        }
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] compressed_lamport_PK = digest.digest(lamport_PK);
        return compressed_lamport_PK;
    }

    public static byte[] derive_child_SK(byte[] parent_SK, long index) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
        byte[] compressed_lamport_PK = parent_SK_to_lamport_PK(parent_SK, index);
        byte[] SK = HKDF_mod_r(compressed_lamport_PK, "".getBytes());
        return SK;
    }

    public static class RFC5869 {
        public static byte[] HKDF_Extract_SHA256(byte[] salt, byte[] IKM) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
            return HMAC_SHA256(salt, IKM);
        }

        public static byte[] HKDF_Expand_SHA256(byte[] PRK, byte[] info, int L) throws UnsupportedEncodingException, NoSuchAlgorithmException, InvalidKeyException {
            int N = L / 32 + ((L % 32 == 0) ? 0 : 1);
            byte[] T = null;
            byte[] Tprev = new byte[0];
            for(int i = 1; i <= N; ++i) {
                byte[] n = { (byte)i };
                byte[] Tn = new byte[Tprev.length + info.length + 1];
                System.arraycopy(Tprev, 0, Tn, 0, Tprev.length);
                System.arraycopy(info, 0, Tn, Tprev.length, info.length);
                System.arraycopy(n, 0, Tn, Tprev.length + info.length, n.length);
                Tprev = HMAC_SHA256(PRK, Tn);
                if(T != null) {
                    byte[] Tnew = new byte[T.length + 32];
                    System.arraycopy(T, 0, Tnew, 0, T.length);
                    System.arraycopy(Tprev, 0, Tnew, T.length, Tprev.length);
                    T = Tnew;
                }else T = Tprev;
            }
            if(T.length == L) return T;
            byte[] tr = new byte[L];
            System.arraycopy(T, 0, tr, 0, L);
            return tr;
        }

        public static byte[] HMAC_SHA256(byte[] secretKey, byte[] message) throws
                NoSuchAlgorithmException, InvalidKeyException {
            // Prepare hmac sha256 cipher algorithm with provided secretKey
            Mac hmacSha256;
            try {
                hmacSha256 = Mac.getInstance("HmacSHA256");
            } catch (NoSuchAlgorithmException nsae) {
                hmacSha256 = Mac.getInstance("HMAC-SHA-256");
            }
            HmacSHA256Key secretKeySpec = new HmacSHA256Key(secretKey);
            hmacSha256.init(secretKeySpec);
            // Build and return signature
            return hmacSha256.doFinal(message);
        }

        private static byte[] bigintToByteArray(BigInteger bigint, int bytelength){
            byte[] raw = bigint.toByteArray();
            if(raw.length < bytelength){
                byte[] tr = new byte[bytelength];
                int diff = tr.length - raw.length;
                for(int i = diff; i < tr.length; ++i){
                    tr[i] = raw[i - diff];
                }
                return tr;
            }else return raw;
        }

        private static class HmacSHA256Key implements KeySpec, SecretKey {
            private byte[] key;

            public HmacSHA256Key(byte[] key) {
                if (key == null) {
                    throw new IllegalArgumentException("Argument is null");
                }
                this.key = key.clone();
            }

            @Override
            public String getAlgorithm() {
                return "HmacSHA256";
            }

            @Override
            public String getFormat() {
                return "RAW";
            }

            @Override
            public byte[] getEncoded() {
                return this.key.clone();
            }
        }
    }

    public static class RFC3447 {
        public static byte[] I2OSP(long x, int xLen){
            if (x < 0) {
                return null;
            }
            int octL = ceilLog256(x);
            if (octL > xLen)
            {
                throw new ArithmeticException(
                        "Cannot encode given integer into specified number of octets.");
            }
            byte[] result = new byte[xLen];
            for (int i = xLen - 1; i >= xLen - octL; i--)
            {
                result[i] = (byte)(x >>> (8 * (xLen - 1 - i)));
            }
            return result;
        }

        public static long OS2IP(byte[] X) {
            if(X.length == 0) return 0;
            long result = 0;
            for(int i = 0; i < X.length; i++) {
                result |= (X[i] & 0xff) << (8 * (X.length - i - 1 ));
            }
            return result;
        }

        private static int ceilLog256(long n) {
            if (n == 0) return 1;
            long m;
            if (n < 0) m = -n;
            else m = n;
            int tr = 0;
            while (m > 0) {
                tr++;
                m >>>= 8;
            }
            return tr;
        }
    }
}
