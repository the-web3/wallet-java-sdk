package com.gingernet.utils;

import java.io.PrintStream;
import java.security.Key;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Arrays;

public interface Blake2b {
    void update(byte[] var1);

    void update(byte var1);

    void update(byte[] var1, int var2, int var3);

    byte[] digest();

    byte[] digest(byte[] var1);

    void digest(byte[] var1, int var2, int var3);

    void reset();

    public static class Param implements AlgorithmParameterSpec {
        static final byte[] default_bytes = new byte[64];
        static final long[] default_h;
        private boolean hasKey = false;
        private byte[] key_bytes = null;
        private byte[] bytes = null;
        private final long[] h = new long[8];

        public Param() {
            System.arraycopy(default_h, 0, this.h, 0, 8);
        }

        public long[] initialized_H() {
            return this.h;
        }

        public byte[] getBytes() {
            this.lazyInitBytes();
            byte[] copy = new byte[this.bytes.length];
            System.arraycopy(this.bytes, 0, copy, 0, this.bytes.length);
            return copy;
        }

        final byte getByteParam(int xoffset) {
            byte[] _bytes = this.bytes;
            if (_bytes == null) {
                _bytes = default_bytes;
            }

            return _bytes[xoffset];
        }

        final int getIntParam(int xoffset) {
            byte[] _bytes = this.bytes;
            if (_bytes == null) {
                _bytes = default_bytes;
            }

            return Blake2b.Engine.LittleEndian.readInt(_bytes, xoffset);
        }

        final long getLongParam(int xoffset) {
            byte[] _bytes = this.bytes;
            if (_bytes == null) {
                _bytes = default_bytes;
            }

            return Blake2b.Engine.LittleEndian.readLong(_bytes, xoffset);
        }

        public final int getDigestLength() {
            return this.getByteParam(0);
        }

        public final int getKeyLength() {
            return this.getByteParam(1);
        }

        public final int getFanout() {
            return this.getByteParam(2);
        }

        public final int getDepth() {
            return this.getByteParam(3);
        }

        public final int getLeafLength() {
            return this.getIntParam(4);
        }

        public final long getNodeOffset() {
            return this.getLongParam(8);
        }

        public final int getNodeDepth() {
            return this.getByteParam(16);
        }

        public final int getInnerLength() {
            return this.getByteParam(17);
        }

        public final boolean hasKey() {
            return this.hasKey;
        }

        public Blake2b.Param clone() {
            Blake2b.Param clone = new Blake2b.Param();
            System.arraycopy(this.h, 0, clone.h, 0, this.h.length);
            clone.lazyInitBytes();
            System.arraycopy(this.bytes, 0, clone.bytes, 0, this.bytes.length);
            if (this.hasKey) {
                clone.hasKey = this.hasKey;
                clone.key_bytes = new byte[128];
                System.arraycopy(this.key_bytes, 0, clone.key_bytes, 0, this.key_bytes.length);
            }

            return clone;
        }

        final void lazyInitBytes() {
            if (this.bytes == null) {
                this.bytes = new byte[64];
                System.arraycopy(default_bytes, 0, this.bytes, 0, 64);
            }

        }

        public final Blake2b.Param setDigestLength(int len) {
            assert len > 0 : Blake2b.Engine.Assert.assertFail("len", len, "'%s' %d is <= %d", 0);

            assert len <= 64 : Blake2b.Engine.Assert.assertFail("len", len, "'%s' %d is > %d", 64);

            this.lazyInitBytes();
            this.bytes[0] = (byte)len;
            this.h[0] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 0);
            long[] var10000 = this.h;
            var10000[0] ^= Blake2b.Spec.IV[0];
            return this;
        }

        public final Blake2b.Param setKey(Key key) {
            assert key != null : "key is null";

            byte[] keybytes = key.getEncoded();

            assert keybytes != null : "key.encoded() is null";

            return this.setKey(keybytes);
        }

        public final Blake2b.Param setKey(byte[] key) {
            assert key != null : "key is null";

            assert key.length >= 0 : Blake2b.Engine.Assert.assertFail("key.length", key.length, "'%s' %d is > %d", 0);

            assert key.length <= 64 : Blake2b.Engine.Assert.assertFail("key.length", key.length, "'%s' %d is > %d", 64);

            this.key_bytes = new byte[128];
            System.arraycopy(key, 0, this.key_bytes, 0, key.length);
            this.lazyInitBytes();
            this.bytes[1] = (byte)key.length;
            this.h[0] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 0);
            long[] var10000 = this.h;
            var10000[0] ^= Blake2b.Spec.IV[0];
            this.hasKey = true;
            return this;
        }

        public final Blake2b.Param setFanout(int fanout) {
            assert fanout > 0 : Blake2b.Engine.Assert.assertFail("fanout", fanout, "'%s' %d is <= %d", 0);

            this.lazyInitBytes();
            this.bytes[2] = (byte)fanout;
            this.h[0] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 0);
            long[] var10000 = this.h;
            var10000[0] ^= Blake2b.Spec.IV[0];
            return this;
        }

        public final Blake2b.Param setDepth(int depth) {
            assert depth > 0 : Blake2b.Engine.Assert.assertFail("depth", depth, "'%s' %d is <= %d", 0);

            this.lazyInitBytes();
            this.bytes[3] = (byte)depth;
            this.h[0] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 0);
            long[] var10000 = this.h;
            var10000[0] ^= Blake2b.Spec.IV[0];
            return this;
        }

        public final Blake2b.Param setLeafLength(int leaf_length) {
            assert leaf_length >= 0 : Blake2b.Engine.Assert.assertFail("leaf_length", leaf_length, "'%s' %d is < %d", 0);

            this.lazyInitBytes();
            Blake2b.Engine.LittleEndian.writeInt(leaf_length, this.bytes, 4);
            this.h[0] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 0);
            long[] var10000 = this.h;
            var10000[0] ^= Blake2b.Spec.IV[0];
            return this;
        }

        public final Blake2b.Param setNodeOffset(long node_offset) {
            assert node_offset >= 0L : Blake2b.Engine.Assert.assertFail("node_offset", node_offset, "'%s' %d is < %d", 0);

            this.lazyInitBytes();
            Blake2b.Engine.LittleEndian.writeLong(node_offset, this.bytes, 8);
            this.h[1] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 8);
            long[] var10000 = this.h;
            var10000[1] ^= Blake2b.Spec.IV[1];
            return this;
        }

        public final Blake2b.Param setNodeDepth(int node_depth) {
            assert node_depth >= 0 : Blake2b.Engine.Assert.assertFail("node_depth", node_depth, "'%s' %d is < %d", 0);

            this.lazyInitBytes();
            this.bytes[16] = (byte)node_depth;
            this.h[2] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 16);
            long[] var10000 = this.h;
            var10000[2] ^= Blake2b.Spec.IV[2];
            this.h[3] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 24);
            var10000 = this.h;
            var10000[3] ^= Blake2b.Spec.IV[3];
            return this;
        }

        public final Blake2b.Param setInnerLength(int inner_length) {
            assert inner_length >= 0 : Blake2b.Engine.Assert.assertFail("inner_length", inner_length, "'%s' %d is < %d", 0);

            this.lazyInitBytes();
            this.bytes[17] = (byte)inner_length;
            this.h[2] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 16);
            long[] var10000 = this.h;
            var10000[2] ^= Blake2b.Spec.IV[2];
            this.h[3] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 24);
            var10000 = this.h;
            var10000[3] ^= Blake2b.Spec.IV[3];
            return this;
        }

        public final Blake2b.Param setSalt(byte[] salt) {
            assert salt != null : "salt is null";

            assert salt.length <= 16 : Blake2b.Engine.Assert.assertFail("salt.length", salt.length, "'%s' %d is > %d", 16);

            this.lazyInitBytes();
            Arrays.fill(this.bytes, 32, 48, (byte)0);
            System.arraycopy(salt, 0, this.bytes, 32, salt.length);
            this.h[4] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 32);
            long[] var10000 = this.h;
            var10000[4] ^= Blake2b.Spec.IV[4];
            this.h[5] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 40);
            var10000 = this.h;
            var10000[5] ^= Blake2b.Spec.IV[5];
            return this;
        }

        public final Blake2b.Param setPersonal(byte[] personal) {
            assert personal != null : "personal is null";

            assert personal.length <= 16 : Blake2b.Engine.Assert.assertFail("personal.length", personal.length, "'%s' %d is > %d", 16);

            this.lazyInitBytes();
            Arrays.fill(this.bytes, 48, 64, (byte)0);
            System.arraycopy(personal, 0, this.bytes, 48, personal.length);
            this.h[6] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 48);
            long[] var10000 = this.h;
            var10000[6] ^= Blake2b.Spec.IV[6];
            this.h[7] = Blake2b.Engine.LittleEndian.readLong(this.bytes, 56);
            var10000 = this.h;
            var10000[7] ^= Blake2b.Spec.IV[7];
            return this;
        }

        static {
            default_bytes[0] = 64;
            default_bytes[1] = 0;
            default_bytes[2] = 1;
            default_bytes[3] = 1;
            default_bytes[16] = 0;
            default_bytes[17] = 0;
            default_h = new long[8];
            default_h[0] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 0);
            default_h[1] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 8);
            default_h[2] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 16);
            default_h[3] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 24);
            default_h[4] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 32);
            default_h[5] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 40);
            default_h[6] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 48);
            default_h[7] = Blake2b.Engine.LittleEndian.readLong(default_bytes, 56);
            long[] var10000 = default_h;
            var10000[0] ^= Blake2b.Spec.IV[0];
            var10000 = default_h;
            var10000[1] ^= Blake2b.Spec.IV[1];
            var10000 = default_h;
            var10000[2] ^= Blake2b.Spec.IV[2];
            var10000 = default_h;
            var10000[3] ^= Blake2b.Spec.IV[3];
            var10000 = default_h;
            var10000[4] ^= Blake2b.Spec.IV[4];
            var10000 = default_h;
            var10000[5] ^= Blake2b.Spec.IV[5];
            var10000 = default_h;
            var10000[6] ^= Blake2b.Spec.IV[6];
            var10000 = default_h;
            var10000[7] ^= Blake2b.Spec.IV[7];
        }

        public interface Default {
            byte digest_length = 64;
            byte key_length = 0;
            byte fanout = 1;
            byte depth = 1;
            int leaf_length = 0;
            long node_offset = 0L;
            byte node_depth = 0;
            byte inner_length = 0;
        }

        interface Xoff {
            int digest_length = 0;
            int key_length = 1;
            int fanout = 2;
            int depth = 3;
            int leaf_length = 4;
            int node_offset = 8;
            int node_depth = 16;
            int inner_length = 17;
            int reserved = 18;
            int salt = 32;
            int personal = 48;
        }
    }

    public static class Engine implements Blake2b {
        static final int[] sig_g00 = new int[]{0, 14, 11, 7, 9, 2, 12, 13, 6, 10, 0, 14};
        static final int[] sig_g01 = new int[]{1, 10, 8, 9, 0, 12, 5, 11, 15, 2, 1, 10};
        static final int[] sig_g10 = new int[]{2, 4, 12, 3, 5, 6, 1, 7, 14, 8, 2, 4};
        static final int[] sig_g11 = new int[]{3, 8, 0, 1, 7, 10, 15, 14, 9, 4, 3, 8};
        static final int[] sig_g20 = new int[]{4, 9, 5, 13, 2, 0, 14, 12, 11, 7, 4, 9};
        static final int[] sig_g21 = new int[]{5, 15, 2, 12, 4, 11, 13, 1, 3, 6, 5, 15};
        static final int[] sig_g30 = new int[]{6, 13, 15, 11, 10, 8, 4, 3, 0, 1, 6, 13};
        static final int[] sig_g31 = new int[]{7, 6, 13, 14, 15, 3, 10, 9, 8, 5, 7, 6};
        static final int[] sig_g40 = new int[]{8, 1, 10, 2, 14, 4, 0, 5, 12, 15, 8, 1};
        static final int[] sig_g41 = new int[]{9, 12, 14, 6, 1, 13, 7, 0, 2, 11, 9, 12};
        static final int[] sig_g50 = new int[]{10, 0, 3, 5, 11, 7, 6, 15, 13, 9, 10, 0};
        static final int[] sig_g51 = new int[]{11, 2, 6, 10, 12, 5, 3, 4, 7, 14, 11, 2};
        static final int[] sig_g60 = new int[]{12, 11, 7, 4, 6, 15, 9, 8, 1, 3, 12, 11};
        static final int[] sig_g61 = new int[]{13, 7, 1, 0, 8, 14, 2, 6, 4, 12, 13, 7};
        static final int[] sig_g70 = new int[]{14, 5, 9, 15, 3, 1, 8, 2, 10, 13, 14, 5};
        static final int[] sig_g71 = new int[]{15, 3, 4, 8, 13, 9, 11, 10, 5, 0, 15, 3};
        private final long[] h;
        private final long[] t;
        private final long[] f;
        private boolean last_node;
        private final long[] m;
        private final long[] v;
        private final byte[] buffer;
        private int buflen;
        private final Blake2b.Param param;
        private final int outlen;
        private byte[] oneByte;
        private static byte[] zeropad = new byte[128];

        Engine() {
            this(new Blake2b.Param());
        }

        Engine(Blake2b.Param param) {
            this.h = new long[8];
            this.t = new long[2];
            this.f = new long[2];
            this.last_node = false;
            this.m = new long[16];
            this.v = new long[16];

            assert param != null : "param is null";

            this.param = param;
            this.buffer = new byte[128];
            this.oneByte = new byte[1];
            this.outlen = param.getDigestLength();
            if (param.getDepth() > 1) {
                int ndepth = param.getNodeDepth();
                long nxoff = param.getNodeOffset();
                if (ndepth == param.getDepth() - 1) {
                    this.last_node = true;

                    assert param.getNodeOffset() == 0L : "root must have offset of zero";
                } else if (param.getNodeOffset() == (long)(param.getFanout() - 1)) {
                    this.last_node = true;
                }
            }

            this.initialize();
        }

        private void initialize() {
            System.arraycopy(this.param.initialized_H(), 0, this.h, 0, 8);
            if (this.param.hasKey) {
                this.update(this.param.key_bytes, 0, 128);
            }

        }

        public static void main(String... args) {
            Blake2b mac = Blake2b.Mac.newInstance("LOVE".getBytes());
            byte[] hash = mac.digest("Salaam!".getBytes());
        }

        public final void reset() {
            this.buflen = 0;

            for(int i = 0; i < this.buffer.length; ++i) {
                this.buffer[i] = 0;
            }

            this.f[0] = 0L;
            this.f[1] = 0L;
            this.t[0] = 0L;
            this.t[1] = 0L;
            this.initialize();
        }

        public final void update(byte[] b, int off, int len) {
            if (b == null) {
                throw new IllegalArgumentException("input buffer (b) is null");
            } else {
                while(true) {
                    while(len > 0) {
                        long[] var10000;
                        if (this.buflen == 0) {
                            while(len > 128) {
                                var10000 = this.t;
                                var10000[0] += 128L;
                                var10000 = this.t;
                                var10000[1] += this.t[0] == 0L ? 1L : 0L;
                                this.compress(b, off);
                                len -= 128;
                                off += 128;
                            }
                        } else if (this.buflen == 128) {
                            var10000 = this.t;
                            var10000[0] += 128L;
                            var10000 = this.t;
                            var10000[1] += this.t[0] == 0L ? 1L : 0L;
                            this.compress(this.buffer, 0);
                            this.buflen = 0;
                            continue;
                        }

                        if (len == 0) {
                            return;
                        }

                        int cap = 128 - this.buflen;
                        int fill = len > cap ? cap : len;
                        System.arraycopy(b, off, this.buffer, this.buflen, fill);
                        this.buflen += fill;
                        len -= fill;
                        off += fill;
                    }

                    return;
                }
            }
        }

        public final void update(byte b) {
            this.oneByte[0] = b;
            this.update(this.oneByte, 0, 1);
        }

        public final void update(byte[] input) {
            this.update(input, 0, input.length);
        }

        public final void digest(byte[] output, int off, int len) {
            System.arraycopy(zeropad, 0, this.buffer, this.buflen, 128 - this.buflen);
            if (this.buflen > 0) {
                long[] var10000 = this.t;
                var10000[0] += (long)this.buflen;
                var10000 = this.t;
                var10000[1] += this.t[0] == 0L ? 1L : 0L;
            }

            this.f[0] = -1L;
            this.f[1] = this.last_node ? -1L : 0L;
            this.compress(this.buffer, 0);
            this.hashout(output, off, len);
            this.reset();
        }

        public final byte[] digest() throws IllegalArgumentException {
            byte[] out = new byte[this.outlen];
            this.digest(out, 0, this.outlen);
            return out;
        }

        public final byte[] digest(byte[] input) {
            this.update(input, 0, input.length);
            return this.digest();
        }

        private void hashout(byte[] out, int offset, int hashlen) {
            int lcnt = hashlen >>> 3;
            long v = 0L;
            int i = offset;

            for(int w = 0; w < lcnt; ++w) {
                v = this.h[w];
                out[i++] = (byte)((int)v);
                v >>>= 8;
                out[i++] = (byte)((int)v);
                v >>>= 8;
                out[i++] = (byte)((int)v);
                v >>>= 8;
                out[i++] = (byte)((int)v);
                v >>>= 8;
                out[i++] = (byte)((int)v);
                v >>>= 8;
                out[i++] = (byte)((int)v);
                v >>>= 8;
                out[i++] = (byte)((int)v);
                v >>>= 8;
                out[i++] = (byte)((int)v);
            }

            if (hashlen != 64) {
                v = this.h[lcnt];

                for(i = lcnt << 3; i < hashlen; ++i) {
                    out[offset + i] = (byte)((int)v);
                    v >>>= 8;
                }

            }
        }

        private void compress(byte[] b, int offset) {
            this.m[0] = (long)b[offset] & 255L;
            long[] var10000 = this.m;
            var10000[0] |= ((long)b[offset + 1] & 255L) << 8;
            var10000 = this.m;
            var10000[0] |= ((long)b[offset + 2] & 255L) << 16;
            var10000 = this.m;
            var10000[0] |= ((long)b[offset + 3] & 255L) << 24;
            var10000 = this.m;
            var10000[0] |= ((long)b[offset + 4] & 255L) << 32;
            var10000 = this.m;
            var10000[0] |= ((long)b[offset + 5] & 255L) << 40;
            var10000 = this.m;
            var10000[0] |= ((long)b[offset + 6] & 255L) << 48;
            var10000 = this.m;
            var10000[0] |= (long)b[offset + 7] << 56;
            this.m[1] = (long)b[offset + 8] & 255L;
            var10000 = this.m;
            var10000[1] |= ((long)b[offset + 9] & 255L) << 8;
            var10000 = this.m;
            var10000[1] |= ((long)b[offset + 10] & 255L) << 16;
            var10000 = this.m;
            var10000[1] |= ((long)b[offset + 11] & 255L) << 24;
            var10000 = this.m;
            var10000[1] |= ((long)b[offset + 12] & 255L) << 32;
            var10000 = this.m;
            var10000[1] |= ((long)b[offset + 13] & 255L) << 40;
            var10000 = this.m;
            var10000[1] |= ((long)b[offset + 14] & 255L) << 48;
            var10000 = this.m;
            var10000[1] |= (long)b[offset + 15] << 56;
            this.m[2] = (long)b[offset + 16] & 255L;
            var10000 = this.m;
            var10000[2] |= ((long)b[offset + 17] & 255L) << 8;
            var10000 = this.m;
            var10000[2] |= ((long)b[offset + 18] & 255L) << 16;
            var10000 = this.m;
            var10000[2] |= ((long)b[offset + 19] & 255L) << 24;
            var10000 = this.m;
            var10000[2] |= ((long)b[offset + 20] & 255L) << 32;
            var10000 = this.m;
            var10000[2] |= ((long)b[offset + 21] & 255L) << 40;
            var10000 = this.m;
            var10000[2] |= ((long)b[offset + 22] & 255L) << 48;
            var10000 = this.m;
            var10000[2] |= (long)b[offset + 23] << 56;
            this.m[3] = (long)b[offset + 24] & 255L;
            var10000 = this.m;
            var10000[3] |= ((long)b[offset + 25] & 255L) << 8;
            var10000 = this.m;
            var10000[3] |= ((long)b[offset + 26] & 255L) << 16;
            var10000 = this.m;
            var10000[3] |= ((long)b[offset + 27] & 255L) << 24;
            var10000 = this.m;
            var10000[3] |= ((long)b[offset + 28] & 255L) << 32;
            var10000 = this.m;
            var10000[3] |= ((long)b[offset + 29] & 255L) << 40;
            var10000 = this.m;
            var10000[3] |= ((long)b[offset + 30] & 255L) << 48;
            var10000 = this.m;
            var10000[3] |= (long)b[offset + 31] << 56;
            this.m[4] = (long)b[offset + 32] & 255L;
            var10000 = this.m;
            var10000[4] |= ((long)b[offset + 33] & 255L) << 8;
            var10000 = this.m;
            var10000[4] |= ((long)b[offset + 34] & 255L) << 16;
            var10000 = this.m;
            var10000[4] |= ((long)b[offset + 35] & 255L) << 24;
            var10000 = this.m;
            var10000[4] |= ((long)b[offset + 36] & 255L) << 32;
            var10000 = this.m;
            var10000[4] |= ((long)b[offset + 37] & 255L) << 40;
            var10000 = this.m;
            var10000[4] |= ((long)b[offset + 38] & 255L) << 48;
            var10000 = this.m;
            var10000[4] |= (long)b[offset + 39] << 56;
            this.m[5] = (long)b[offset + 40] & 255L;
            var10000 = this.m;
            var10000[5] |= ((long)b[offset + 41] & 255L) << 8;
            var10000 = this.m;
            var10000[5] |= ((long)b[offset + 42] & 255L) << 16;
            var10000 = this.m;
            var10000[5] |= ((long)b[offset + 43] & 255L) << 24;
            var10000 = this.m;
            var10000[5] |= ((long)b[offset + 44] & 255L) << 32;
            var10000 = this.m;
            var10000[5] |= ((long)b[offset + 45] & 255L) << 40;
            var10000 = this.m;
            var10000[5] |= ((long)b[offset + 46] & 255L) << 48;
            var10000 = this.m;
            var10000[5] |= (long)b[offset + 47] << 56;
            this.m[6] = (long)b[offset + 48] & 255L;
            var10000 = this.m;
            var10000[6] |= ((long)b[offset + 49] & 255L) << 8;
            var10000 = this.m;
            var10000[6] |= ((long)b[offset + 50] & 255L) << 16;
            var10000 = this.m;
            var10000[6] |= ((long)b[offset + 51] & 255L) << 24;
            var10000 = this.m;
            var10000[6] |= ((long)b[offset + 52] & 255L) << 32;
            var10000 = this.m;
            var10000[6] |= ((long)b[offset + 53] & 255L) << 40;
            var10000 = this.m;
            var10000[6] |= ((long)b[offset + 54] & 255L) << 48;
            var10000 = this.m;
            var10000[6] |= (long)b[offset + 55] << 56;
            this.m[7] = (long)b[offset + 56] & 255L;
            var10000 = this.m;
            var10000[7] |= ((long)b[offset + 57] & 255L) << 8;
            var10000 = this.m;
            var10000[7] |= ((long)b[offset + 58] & 255L) << 16;
            var10000 = this.m;
            var10000[7] |= ((long)b[offset + 59] & 255L) << 24;
            var10000 = this.m;
            var10000[7] |= ((long)b[offset + 60] & 255L) << 32;
            var10000 = this.m;
            var10000[7] |= ((long)b[offset + 61] & 255L) << 40;
            var10000 = this.m;
            var10000[7] |= ((long)b[offset + 62] & 255L) << 48;
            var10000 = this.m;
            var10000[7] |= (long)b[offset + 63] << 56;
            this.m[8] = (long)b[offset + 64] & 255L;
            var10000 = this.m;
            var10000[8] |= ((long)b[offset + 65] & 255L) << 8;
            var10000 = this.m;
            var10000[8] |= ((long)b[offset + 66] & 255L) << 16;
            var10000 = this.m;
            var10000[8] |= ((long)b[offset + 67] & 255L) << 24;
            var10000 = this.m;
            var10000[8] |= ((long)b[offset + 68] & 255L) << 32;
            var10000 = this.m;
            var10000[8] |= ((long)b[offset + 69] & 255L) << 40;
            var10000 = this.m;
            var10000[8] |= ((long)b[offset + 70] & 255L) << 48;
            var10000 = this.m;
            var10000[8] |= (long)b[offset + 71] << 56;
            this.m[9] = (long)b[offset + 72] & 255L;
            var10000 = this.m;
            var10000[9] |= ((long)b[offset + 73] & 255L) << 8;
            var10000 = this.m;
            var10000[9] |= ((long)b[offset + 74] & 255L) << 16;
            var10000 = this.m;
            var10000[9] |= ((long)b[offset + 75] & 255L) << 24;
            var10000 = this.m;
            var10000[9] |= ((long)b[offset + 76] & 255L) << 32;
            var10000 = this.m;
            var10000[9] |= ((long)b[offset + 77] & 255L) << 40;
            var10000 = this.m;
            var10000[9] |= ((long)b[offset + 78] & 255L) << 48;
            var10000 = this.m;
            var10000[9] |= (long)b[offset + 79] << 56;
            this.m[10] = (long)b[offset + 80] & 255L;
            var10000 = this.m;
            var10000[10] |= ((long)b[offset + 81] & 255L) << 8;
            var10000 = this.m;
            var10000[10] |= ((long)b[offset + 82] & 255L) << 16;
            var10000 = this.m;
            var10000[10] |= ((long)b[offset + 83] & 255L) << 24;
            var10000 = this.m;
            var10000[10] |= ((long)b[offset + 84] & 255L) << 32;
            var10000 = this.m;
            var10000[10] |= ((long)b[offset + 85] & 255L) << 40;
            var10000 = this.m;
            var10000[10] |= ((long)b[offset + 86] & 255L) << 48;
            var10000 = this.m;
            var10000[10] |= (long)b[offset + 87] << 56;
            this.m[11] = (long)b[offset + 88] & 255L;
            var10000 = this.m;
            var10000[11] |= ((long)b[offset + 89] & 255L) << 8;
            var10000 = this.m;
            var10000[11] |= ((long)b[offset + 90] & 255L) << 16;
            var10000 = this.m;
            var10000[11] |= ((long)b[offset + 91] & 255L) << 24;
            var10000 = this.m;
            var10000[11] |= ((long)b[offset + 92] & 255L) << 32;
            var10000 = this.m;
            var10000[11] |= ((long)b[offset + 93] & 255L) << 40;
            var10000 = this.m;
            var10000[11] |= ((long)b[offset + 94] & 255L) << 48;
            var10000 = this.m;
            var10000[11] |= (long)b[offset + 95] << 56;
            this.m[12] = (long)b[offset + 96] & 255L;
            var10000 = this.m;
            var10000[12] |= ((long)b[offset + 97] & 255L) << 8;
            var10000 = this.m;
            var10000[12] |= ((long)b[offset + 98] & 255L) << 16;
            var10000 = this.m;
            var10000[12] |= ((long)b[offset + 99] & 255L) << 24;
            var10000 = this.m;
            var10000[12] |= ((long)b[offset + 100] & 255L) << 32;
            var10000 = this.m;
            var10000[12] |= ((long)b[offset + 101] & 255L) << 40;
            var10000 = this.m;
            var10000[12] |= ((long)b[offset + 102] & 255L) << 48;
            var10000 = this.m;
            var10000[12] |= (long)b[offset + 103] << 56;
            this.m[13] = (long)b[offset + 104] & 255L;
            var10000 = this.m;
            var10000[13] |= ((long)b[offset + 105] & 255L) << 8;
            var10000 = this.m;
            var10000[13] |= ((long)b[offset + 106] & 255L) << 16;
            var10000 = this.m;
            var10000[13] |= ((long)b[offset + 107] & 255L) << 24;
            var10000 = this.m;
            var10000[13] |= ((long)b[offset + 108] & 255L) << 32;
            var10000 = this.m;
            var10000[13] |= ((long)b[offset + 109] & 255L) << 40;
            var10000 = this.m;
            var10000[13] |= ((long)b[offset + 110] & 255L) << 48;
            var10000 = this.m;
            var10000[13] |= (long)b[offset + 111] << 56;
            this.m[14] = (long)b[offset + 112] & 255L;
            var10000 = this.m;
            var10000[14] |= ((long)b[offset + 113] & 255L) << 8;
            var10000 = this.m;
            var10000[14] |= ((long)b[offset + 114] & 255L) << 16;
            var10000 = this.m;
            var10000[14] |= ((long)b[offset + 115] & 255L) << 24;
            var10000 = this.m;
            var10000[14] |= ((long)b[offset + 116] & 255L) << 32;
            var10000 = this.m;
            var10000[14] |= ((long)b[offset + 117] & 255L) << 40;
            var10000 = this.m;
            var10000[14] |= ((long)b[offset + 118] & 255L) << 48;
            var10000 = this.m;
            var10000[14] |= (long)b[offset + 119] << 56;
            this.m[15] = (long)b[offset + 120] & 255L;
            var10000 = this.m;
            var10000[15] |= ((long)b[offset + 121] & 255L) << 8;
            var10000 = this.m;
            var10000[15] |= ((long)b[offset + 122] & 255L) << 16;
            var10000 = this.m;
            var10000[15] |= ((long)b[offset + 123] & 255L) << 24;
            var10000 = this.m;
            var10000[15] |= ((long)b[offset + 124] & 255L) << 32;
            var10000 = this.m;
            var10000[15] |= ((long)b[offset + 125] & 255L) << 40;
            var10000 = this.m;
            var10000[15] |= ((long)b[offset + 126] & 255L) << 48;
            var10000 = this.m;
            var10000[15] |= (long)b[offset + 127] << 56;
            this.v[0] = this.h[0];
            this.v[1] = this.h[1];
            this.v[2] = this.h[2];
            this.v[3] = this.h[3];
            this.v[4] = this.h[4];
            this.v[5] = this.h[5];
            this.v[6] = this.h[6];
            this.v[7] = this.h[7];
            this.v[8] = 7640891576956012808L;
            this.v[9] = -4942790177534073029L;
            this.v[10] = 4354685564936845355L;
            this.v[11] = -6534734903238641935L;
            this.v[12] = this.t[0] ^ 5840696475078001361L;
            this.v[13] = this.t[1] ^ -7276294671716946913L;
            this.v[14] = this.f[0] ^ 2270897969802886507L;
            this.v[15] = this.f[1] ^ 6620516959819538809L;

            for(int r = 0; r < 12; ++r) {
                this.v[0] = this.v[0] + this.v[4] + this.m[sig_g00[r]];
                var10000 = this.v;
                var10000[12] ^= this.v[0];
                this.v[12] = this.v[12] << 32 | this.v[12] >>> 32;
                this.v[8] += this.v[12];
                var10000 = this.v;
                var10000[4] ^= this.v[8];
                this.v[4] = this.v[4] >>> 24 | this.v[4] << 40;
                this.v[0] = this.v[0] + this.v[4] + this.m[sig_g01[r]];
                var10000 = this.v;
                var10000[12] ^= this.v[0];
                this.v[12] = this.v[12] >>> 16 | this.v[12] << 48;
                this.v[8] += this.v[12];
                var10000 = this.v;
                var10000[4] ^= this.v[8];
                this.v[4] = this.v[4] << 1 | this.v[4] >>> 63;
                this.v[1] = this.v[1] + this.v[5] + this.m[sig_g10[r]];
                var10000 = this.v;
                var10000[13] ^= this.v[1];
                this.v[13] = this.v[13] << 32 | this.v[13] >>> 32;
                this.v[9] += this.v[13];
                var10000 = this.v;
                var10000[5] ^= this.v[9];
                this.v[5] = this.v[5] >>> 24 | this.v[5] << 40;
                this.v[1] = this.v[1] + this.v[5] + this.m[sig_g11[r]];
                var10000 = this.v;
                var10000[13] ^= this.v[1];
                this.v[13] = this.v[13] >>> 16 | this.v[13] << 48;
                this.v[9] += this.v[13];
                var10000 = this.v;
                var10000[5] ^= this.v[9];
                this.v[5] = this.v[5] << 1 | this.v[5] >>> 63;
                this.v[2] = this.v[2] + this.v[6] + this.m[sig_g20[r]];
                var10000 = this.v;
                var10000[14] ^= this.v[2];
                this.v[14] = this.v[14] << 32 | this.v[14] >>> 32;
                this.v[10] += this.v[14];
                var10000 = this.v;
                var10000[6] ^= this.v[10];
                this.v[6] = this.v[6] >>> 24 | this.v[6] << 40;
                this.v[2] = this.v[2] + this.v[6] + this.m[sig_g21[r]];
                var10000 = this.v;
                var10000[14] ^= this.v[2];
                this.v[14] = this.v[14] >>> 16 | this.v[14] << 48;
                this.v[10] += this.v[14];
                var10000 = this.v;
                var10000[6] ^= this.v[10];
                this.v[6] = this.v[6] << 1 | this.v[6] >>> 63;
                this.v[3] = this.v[3] + this.v[7] + this.m[sig_g30[r]];
                var10000 = this.v;
                var10000[15] ^= this.v[3];
                this.v[15] = this.v[15] << 32 | this.v[15] >>> 32;
                this.v[11] += this.v[15];
                var10000 = this.v;
                var10000[7] ^= this.v[11];
                this.v[7] = this.v[7] >>> 24 | this.v[7] << 40;
                this.v[3] = this.v[3] + this.v[7] + this.m[sig_g31[r]];
                var10000 = this.v;
                var10000[15] ^= this.v[3];
                this.v[15] = this.v[15] >>> 16 | this.v[15] << 48;
                this.v[11] += this.v[15];
                var10000 = this.v;
                var10000[7] ^= this.v[11];
                this.v[7] = this.v[7] << 1 | this.v[7] >>> 63;
                this.v[0] = this.v[0] + this.v[5] + this.m[sig_g40[r]];
                var10000 = this.v;
                var10000[15] ^= this.v[0];
                this.v[15] = this.v[15] << 32 | this.v[15] >>> 32;
                this.v[10] += this.v[15];
                var10000 = this.v;
                var10000[5] ^= this.v[10];
                this.v[5] = this.v[5] >>> 24 | this.v[5] << 40;
                this.v[0] = this.v[0] + this.v[5] + this.m[sig_g41[r]];
                var10000 = this.v;
                var10000[15] ^= this.v[0];
                this.v[15] = this.v[15] >>> 16 | this.v[15] << 48;
                this.v[10] += this.v[15];
                var10000 = this.v;
                var10000[5] ^= this.v[10];
                this.v[5] = this.v[5] << 1 | this.v[5] >>> 63;
                this.v[1] = this.v[1] + this.v[6] + this.m[sig_g50[r]];
                var10000 = this.v;
                var10000[12] ^= this.v[1];
                this.v[12] = this.v[12] << 32 | this.v[12] >>> 32;
                this.v[11] += this.v[12];
                var10000 = this.v;
                var10000[6] ^= this.v[11];
                this.v[6] = this.v[6] >>> 24 | this.v[6] << 40;
                this.v[1] = this.v[1] + this.v[6] + this.m[sig_g51[r]];
                var10000 = this.v;
                var10000[12] ^= this.v[1];
                this.v[12] = this.v[12] >>> 16 | this.v[12] << 48;
                this.v[11] += this.v[12];
                var10000 = this.v;
                var10000[6] ^= this.v[11];
                this.v[6] = this.v[6] << 1 | this.v[6] >>> 63;
                this.v[2] = this.v[2] + this.v[7] + this.m[sig_g60[r]];
                var10000 = this.v;
                var10000[13] ^= this.v[2];
                this.v[13] = this.v[13] << 32 | this.v[13] >>> 32;
                this.v[8] += this.v[13];
                var10000 = this.v;
                var10000[7] ^= this.v[8];
                this.v[7] = this.v[7] >>> 24 | this.v[7] << 40;
                this.v[2] = this.v[2] + this.v[7] + this.m[sig_g61[r]];
                var10000 = this.v;
                var10000[13] ^= this.v[2];
                this.v[13] = this.v[13] >>> 16 | this.v[13] << 48;
                this.v[8] += this.v[13];
                var10000 = this.v;
                var10000[7] ^= this.v[8];
                this.v[7] = this.v[7] << 1 | this.v[7] >>> 63;
                this.v[3] = this.v[3] + this.v[4] + this.m[sig_g70[r]];
                var10000 = this.v;
                var10000[14] ^= this.v[3];
                this.v[14] = this.v[14] << 32 | this.v[14] >>> 32;
                this.v[9] += this.v[14];
                var10000 = this.v;
                var10000[4] ^= this.v[9];
                this.v[4] = this.v[4] >>> 24 | this.v[4] << 40;
                this.v[3] = this.v[3] + this.v[4] + this.m[sig_g71[r]];
                var10000 = this.v;
                var10000[14] ^= this.v[3];
                this.v[14] = this.v[14] >>> 16 | this.v[14] << 48;
                this.v[9] += this.v[14];
                var10000 = this.v;
                var10000[4] ^= this.v[9];
                this.v[4] = this.v[4] << 1 | this.v[4] >>> 63;
            }

            var10000 = this.h;
            var10000[0] ^= this.v[0] ^ this.v[8];
            var10000 = this.h;
            var10000[1] ^= this.v[1] ^ this.v[9];
            var10000 = this.h;
            var10000[2] ^= this.v[2] ^ this.v[10];
            var10000 = this.h;
            var10000[3] ^= this.v[3] ^ this.v[11];
            var10000 = this.h;
            var10000[4] ^= this.v[4] ^ this.v[12];
            var10000 = this.h;
            var10000[5] ^= this.v[5] ^ this.v[13];
            var10000 = this.h;
            var10000[6] ^= this.v[6] ^ this.v[14];
            var10000 = this.h;
            var10000[7] ^= this.v[7] ^ this.v[15];
        }

        public static class LittleEndian {
            private static final byte[] hex_digits = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98, 99, 100, 101, 102};
            private static final byte[] HEX_digits = new byte[]{48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 65, 66, 67, 68, 69, 70};

            public LittleEndian() {
            }

            public static String toHexStr(byte[] b) {
                return toHexStr(b, false);
            }

            public static String toHexStr(byte[] b, boolean upperCase) {
                int len = b.length;
                byte[] digits = new byte[len * 2];
                byte[] hex_rep = upperCase ? HEX_digits : hex_digits;

                for(int i = 0; i < len; ++i) {
                    digits[i * 2] = hex_rep[(byte)(b[i] >> 4 & 15)];
                    digits[i * 2 + 1] = hex_rep[(byte)(b[i] & 15)];
                }

                return new String(digits);
            }

            public static int readInt(byte[] b, int off) {
                int v0 = b[off++] & 255;
                v0 |= (b[off++] & 255) << 8;
                v0 |= (b[off++] & 255) << 16;
                v0 |= b[off] << 24;
                return v0;
            }

            public static long readLong(byte[] b, int off) {
                long v0 = (long)b[off++] & 255L;
                v0 |= ((long)b[off++] & 255L) << 8;
                v0 |= ((long)b[off++] & 255L) << 16;
                v0 |= ((long)b[off++] & 255L) << 24;
                v0 |= ((long)b[off++] & 255L) << 32;
                v0 |= ((long)b[off++] & 255L) << 40;
                v0 |= ((long)b[off++] & 255L) << 48;
                v0 |= (long)b[off] << 56;
                return v0;
            }

            public static void writeLong(long v, byte[] b, int off) {
                b[off] = (byte)((int)v);
                v >>>= 8;
                b[off + 1] = (byte)((int)v);
                v >>>= 8;
                b[off + 2] = (byte)((int)v);
                v >>>= 8;
                b[off + 3] = (byte)((int)v);
                v >>>= 8;
                b[off + 4] = (byte)((int)v);
                v >>>= 8;
                b[off + 5] = (byte)((int)v);
                v >>>= 8;
                b[off + 6] = (byte)((int)v);
                v >>>= 8;
                b[off + 7] = (byte)((int)v);
            }

            public static void writeInt(int v, byte[] b, int off) {
                b[off] = (byte)v;
                v >>>= 8;
                b[off + 1] = (byte)v;
                v >>>= 8;
                b[off + 2] = (byte)v;
                v >>>= 8;
                b[off + 3] = (byte)v;
            }
        }

        public static final class Assert {
            public static final String exclusiveUpperBound = "'%s' %d is >= %d";
            public static final String inclusiveUpperBound = "'%s' %d is > %d";
            public static final String exclusiveLowerBound = "'%s' %d is <= %d";
            public static final String inclusiveLowerBound = "'%s' %d is < %d";

            public Assert() {
            }

            static <T extends Number> String assertFail(String name, T v, String err, T spec) {
                (new Exception()).printStackTrace();
                return String.format(err, name, v, spec);
            }
        }

        public static class Debug {
            public Debug() {
            }

            public static void dumpState(Blake2b.Engine e, String mark) {
                System.out.format("-- MARK == @ %s @ ===========\n", mark);
                dumpArray("register t", e.t);
                dumpArray("register h", e.h);
                dumpArray("register f", e.f);
                dumpArray("register offset", new long[]{(long)e.buflen});
                System.out.format("-- END MARK =================\n");
            }

            public static void dumpArray(String label, long[] b) {
                System.out.format("-- %s -- :\n{\n", label);

                for(int j = 0; j < b.length; ++j) {
                    System.out.format("    [%2d] : %016X\n", j, b[j]);
                }

                System.out.format("}\n");
            }

            public static void dumpBuffer(PrintStream out, String label, byte[] b) {
                dumpBuffer(out, label, b, 0, b.length);
            }

            public static void dumpBuffer(PrintStream out, byte[] b) {
                dumpBuffer(out, (String)null, b, 0, b.length);
            }

            public static void dumpBuffer(PrintStream out, byte[] b, int offset, int len) {
                dumpBuffer(out, (String)null, b, offset, len);
            }

            public static void dumpBuffer(PrintStream out, String label, byte[] b, int offset, int len) {
                if (label != null) {
                    out.format("-- %s -- :\n", label);
                }

                out.format("{\n    ", label);

                for(int j = 0; j < len; ++j) {
                    out.format("%02X", b[j + offset]);
                    if (j + 1 < len) {
                        if ((j + 1) % 8 == 0) {
                            out.print("\n    ");
                        } else {
                            out.print(' ');
                        }
                    }
                }

                out.format("\n}\n");
            }
        }

        interface flag {
            int last_block = 0;
            int last_node = 1;
        }
    }

    public static class Tree {
        final int depth;
        final int fanout;
        final int leaf_length;
        final int inner_length;
        final int digest_length;

        public Tree(int depth, int fanout, int leaf_length, int inner_length, int digest_length) {
            this.fanout = fanout;
            this.depth = depth;
            this.leaf_length = leaf_length;
            this.inner_length = inner_length;
            this.digest_length = digest_length;
        }

        private Blake2b.Param treeParam() {
            return (new Blake2b.Param()).setDepth(this.depth).setFanout(this.fanout).setLeafLength(this.leaf_length).setInnerLength(this.inner_length);
        }

        public final Blake2b.Digest getNode(int depth, int offset) {
            Blake2b.Param nodeParam = this.treeParam().setNodeDepth(depth).setNodeOffset((long)offset).setDigestLength(this.inner_length);
            return Blake2b.Digest.newInstance(nodeParam);
        }

        public final Blake2b.Digest getRoot() {
            int depth = this.depth - 1;
            Blake2b.Param rootParam = this.treeParam().setNodeDepth(depth).setNodeOffset(0L).setDigestLength(this.digest_length);
            return Blake2b.Digest.newInstance(rootParam);
        }
    }

    public static class Mac extends Blake2b.Engine implements Blake2b {
        private Mac(Blake2b.Param p) {
            super(p);
        }

        private Mac() {
        }

        public static Blake2b.Mac newInstance(byte[] key) {
            return new Blake2b.Mac((new Blake2b.Param()).setKey(key));
        }

        public static Blake2b.Mac newInstance(byte[] key, int digestLength) {
            return new Blake2b.Mac((new Blake2b.Param()).setKey(key).setDigestLength(digestLength));
        }

        public static Blake2b.Mac newInstance(Key key, int digestLength) {
            return new Blake2b.Mac((new Blake2b.Param()).setKey(key).setDigestLength(digestLength));
        }

        public static Blake2b.Mac newInstance(Blake2b.Param p) {
            assert p != null : "Param (p) is null";

            assert p.hasKey() : "Param (p) not configured with a key";

            return new Blake2b.Mac(p);
        }
    }

    public static class Digest extends Blake2b.Engine implements Blake2b {
        private Digest(Blake2b.Param p) {
            super(p);
        }

        private Digest() {
        }

        public static Blake2b.Digest newInstance() {
            return new Blake2b.Digest();
        }

        public static Blake2b.Digest newInstance(int digestLength) {
            return new Blake2b.Digest((new Blake2b.Param()).setDigestLength(digestLength));
        }

        public static Blake2b.Digest newInstance(Blake2b.Param p) {
            return new Blake2b.Digest(p);
        }
    }

    public interface Spec {
        int param_bytes = 64;
        int block_bytes = 128;
        int max_digest_bytes = 64;
        int max_key_bytes = 64;
        int max_salt_bytes = 16;
        int max_personalization_bytes = 16;
        int state_space_len = 8;
        int max_tree_fantout = 255;
        int max_tree_depth = 255;
        int max_tree_leaf_length = -1;
        long max_node_offset = -1L;
        int max_tree_inner_length = 255;
        long[] IV = new long[]{7640891576956012808L, -4942790177534073029L, 4354685564936845355L, -6534734903238641935L, 5840696475078001361L, -7276294671716946913L, 2270897969802886507L, 6620516959819538809L};
        byte[][] sigma = new byte[][]{{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3}, {11, 8, 12, 0, 5, 2, 15, 13, 10, 14, 3, 6, 7, 1, 9, 4}, {7, 9, 3, 1, 13, 12, 11, 14, 2, 6, 5, 10, 4, 0, 15, 8}, {9, 0, 5, 7, 2, 4, 10, 15, 14, 1, 11, 12, 6, 8, 3, 13}, {2, 12, 6, 10, 0, 11, 8, 3, 4, 13, 7, 5, 15, 14, 1, 9}, {12, 5, 1, 15, 14, 13, 4, 10, 0, 7, 6, 3, 9, 2, 8, 11}, {13, 11, 7, 14, 12, 1, 3, 9, 5, 0, 15, 4, 8, 6, 2, 10}, {6, 15, 14, 9, 11, 3, 0, 8, 12, 2, 13, 7, 1, 4, 10, 5}, {10, 2, 8, 4, 7, 6, 1, 5, 15, 11, 9, 14, 3, 12, 13, 0}, {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15}, {14, 10, 4, 8, 9, 15, 13, 6, 1, 12, 0, 2, 11, 7, 5, 3}};
    }
}

