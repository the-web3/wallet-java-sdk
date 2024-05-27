package com.gingernet.toncoin;

import org.spongycastle.util.encoders.Hex;

import java.math.BigInteger;


public class BitString {

    protected int[] array;
    protected int cursor;
    protected int length;

    public BitString(int length) {
        int ceil = (int) Math.ceil(length / 8.0);
        this.array = new int[ceil];
        this.cursor = 0;
        this.length = length;
    }

    public int getFreeBits() {
        return this.length - this.cursor;
    }

    public int getUsedBits() {
        return this.cursor;
    }

    public int getUsedBytes() {
        return (int) Math.ceil(this.cursor / 8.0);
    }

    public boolean get(int n) {
        return (this.array[(n / 8) | 0] & (1 << (7 - (n % 8)))) > 0;
    }

    public void checkRange(int n) {
        if (n > this.length) {
            throw new IllegalArgumentException("BitString overflow");
        }
    }

    public void on(int n) {
        this.checkRange(n);
        this.array[(n / 8) | 0] |= 1 << (7 - (n % 8));
    }

    public void off(int n) {
        this.checkRange(n);
        this.array[(n / 8) | 0] &= ~(1 << (7 - (n % 8)));
    }

    public void toggle(int n) {
        this.checkRange(n);
        this.array[(n / 8) | 0] ^= 1 << (7 - (n % 8));
    }


    public void writeBit(boolean b) {
        if (b) {
            this.on(this.cursor);
        } else {
            this.off(this.cursor);
        }
        this.cursor = this.cursor + 1;
    }


    public void writeBitArray(int[] ba) {
        for (int i = 0; i < ba.length; i++) {
            this.writeBit(ba[i] > 0);
        }
    }

    public void writeBitArray(boolean[] ba) {
        for (int i = 0; i < ba.length; i++) {
            this.writeBit(ba[i]);
        }
    }

    public void writeUint(long num, long bitLength) {
        BigInteger number = BigInteger.valueOf(num);
        if (bitLength == 0 || (number.toString(2).length() > bitLength)) {
            if (number.longValue() == 0) {
                return;
            }
            throw new IllegalArgumentException("bitLength is too small for number, got number=" + number + ",bitLength=" + bitLength);
        }
        String radix = number.toString(2);
        int length = radix.length();
        for (int i = 0; i < bitLength - length; i++) {
            radix = "0".concat(radix);
        }
        for (int i = 0; i < bitLength; i++) {
            char c = radix.charAt(i);
            this.writeBit(c == '1');
        }
    }

    public void writeInt(long num, int bitLength) {
        BigInteger number = BigInteger.valueOf(num);
        if (bitLength == 1) {
            if (number.longValue() == -1) {
                this.writeBit(true);
                return;
            }
            if (number.longValue() == 0) {
                this.writeBit(false);
                return;
            }
            throw new IllegalArgumentException("Bitlength is too small for number");
        } else {
            int i = number.compareTo(BigInteger.valueOf(0));
            if (i < 0) {
                this.writeBit(true);
                BigInteger b = BigInteger.valueOf(2);
                BigInteger nb = b.pow(bitLength - 1);
                this.writeUint(nb.add(number).longValue(), bitLength - 1);
            } else {
                this.writeBit(false);
                this.writeUint(number.longValue(), bitLength - 1);
            }
        }
    }

    public void writeUint8(long ui8) {
        this.writeUint(ui8, 8);
    }


    public void writeBytes(byte[] ui8) {
        for (int i = 0; i < ui8.length; i++) {
            this.writeUint8(ui8[i] & 0xff);
        }
    }

    public void writeBitString(BitString anotherBitString) {
        int cursor = anotherBitString.cursor;
        for (int i = 0; i < cursor; i++) {
            this.writeBit(anotherBitString.get(i));
        }
    }

    public void writeAddress(Address address) {
        if (address == null) {
            this.writeUint(0, 2);
        } else {
            this.writeUint(2, 2);
            // TODO split addresses (anycast)
            this.writeUint(0, 1);
            this.writeInt(address.workchain, 8);
            this.writeBytes(address.hashPart);
        }
    }

    public String toHex() {
        if (this.cursor % 4 == 0) {
            int ceil = (int) Math.ceil(this.cursor / 8.0);

            int[] arr = new int[ceil];
            System.arraycopy(this.array, 0, arr, 0, ceil);
            String s = Hex.toHexString(Utils.intArrToByteArr(arr)).toUpperCase();

            if (this.cursor % 8 == 0) {
                return s;
            } else {
                return s.substring(0, s.length() - 1);
            }
        } else {
            BitString temp = this.clone();
            temp.writeBit(1 > 0);
            while (temp.cursor % 4 != 0) {
                temp.writeBit(0 > 0);
            }
            String hex = temp.toHex().toUpperCase();
            return hex + '_';
        }
    }

    public byte[] getTopUppedArray() {
        BitString ret = this.clone();
        int tu = (int) Math.ceil(ret.cursor / 8.0) * 8 - ret.cursor;
        if (tu > 0) {
            tu = tu - 1;
            ret.writeBit(true);
            while (tu > 0) {
                tu = tu - 1;
                ret.writeBit(false);
            }
        }
        int ceil = (int) Math.ceil(ret.cursor / 8.0);
        int[] arr = new int[ceil];
        System.arraycopy(ret.array, 0, arr, 0, ceil);
        ret.array = arr;
        return Utils.intArrToByteArr(ret.array);
    }


    @Override
    public BitString clone() {
        BitString result = new BitString(0);
        result.array = this.array;
        result.length = this.length;
        result.cursor = this.cursor;
        return result;
    }

    public void writeGrams(long amount) {
        if (amount == 0) {
            this.writeUint(0, 4);
        } else {
            BigInteger bigInteger = BigInteger.valueOf(amount);

            long l = (long) Math.ceil((bigInteger.toString(16).length()) / 2.0);
            this.writeUint(l, 4);
            this.writeUint(amount, l * 8);
        }
    }


    public void setTopUppedArray(byte[] array, boolean fullfilledBytes) {
        this.length = array.length * 8;
        this.array = Utils.byteArrToIntArr(array);
        this.cursor = this.length;
        if (fullfilledBytes || this.length == 0) {
            return;
        } else {
            boolean foundEndBit = false;
            for (int c = 0; c < 7; c++) {
                this.cursor -= 1;
                if (this.get(this.cursor)) {
                    foundEndBit = true;
                    this.off(this.cursor);
                    break;
                }
            }
            if (!foundEndBit) {
                throw new IllegalArgumentException("Incorrect TopUppedArray");
            }
        }
    }
}