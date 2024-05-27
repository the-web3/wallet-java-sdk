package com.gingernet.toncoin;

import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import static net.i2p.crypto.eddsa.Utils.hexToBytes;


public class Address {

    static int bounceable_tag = 0x11;
    static int non_bounceable_tag = 0x51;
    static int test_flag = 0x80;

    private boolean isTestOnly;
    protected boolean isBounceable;
    protected int workchain;
    protected byte[] hashPart;
    private boolean isUrlSafe;
    private boolean isUserFriendly;

    public Address(boolean isTestOnly, boolean isBounceable, byte workchain, byte[] hashPart) {
        this.isTestOnly = isTestOnly;
        this.isBounceable = isBounceable;
        this.workchain = workchain;
        this.hashPart = hashPart;
    }

    public Address(String address) {
        if (address.split("-").length > 1 || address.split("_").length > 1) {
            this.isUrlSafe = true;
            byte[] decode = java.util.Base64.getUrlDecoder().decode(address);
            address = Base64.toBase64String(decode);
        } else {
            this.isUrlSafe = false;
        }
        if (address.split(":").length > 1) {
            this.isUserFriendly = false;
            this.workchain = Integer.parseInt(address.split(":")[0]);
            this.hashPart = hexToBytes(address.split(":")[1]);
            this.isTestOnly = false;
            this.isBounceable = false;
        } else {
            this.isUserFriendly = true;
            Address parseResult = parseFriendlyAddress(address);
            this.workchain = parseResult.workchain;
            this.hashPart = parseResult.hashPart;
            this.isTestOnly = parseResult.isTestOnly;
            this.isBounceable = parseResult.isBounceable;
        }
    }

    public static Address parseFriendlyAddress(String address) {
        byte[] data = Base64.decode(address);
        if (data.length != 36) {
            throw new IllegalArgumentException("Unknown address type: byte length is not equal to 36");
        }
        byte[] addr = Utils.byteSliceRange(data, 0, 34);
        byte[] crc = Utils.byteSlice(data, 34);
        byte[] calcedCrc = Utils.crc16(addr);
        if (!(calcedCrc[0] == crc[0] && calcedCrc[1] == crc[1])) {
            throw new IllegalArgumentException("Wrong crc16 hashsum");
        }

        byte tag = addr[0];
        boolean isTestOnly = false;
        boolean isBounceable = false;
        if ((tag & test_flag) != 0) {
            isTestOnly = true;
            tag = (byte) (tag ^ test_flag);
        }
        if ((tag != bounceable_tag) && (tag != non_bounceable_tag)) {
            throw new IllegalArgumentException("Unknown address tag");
        }

        isBounceable = tag == bounceable_tag;
        byte workchain;
        if (addr[1] == 0xff) {
            workchain = -1;
        } else {
            workchain = addr[1];
        }
        byte[] hashPart = Utils.byteSliceRange(addr, 2, 32);
        return new Address(isTestOnly, isBounceable, workchain, hashPart);
    }


    public String toAddressString(boolean isUserFriendly, boolean isUrlSafe, boolean isBounceable, boolean isTestOnly) {
        if (!isUserFriendly) {
            return this.workchain + ":" + Hex.toHexString(this.hashPart);
        } else {
            int tag = isBounceable ? bounceable_tag : non_bounceable_tag;
            if (isTestOnly) {
                tag |= test_flag;
            }

            byte[] addr = new byte[34];
            addr[0] = (byte) tag;
            addr[1] = (byte) this.workchain;
            System.arraycopy(this.hashPart, 0, addr, 2, 32);

            byte[] crc16 = Utils.crc16(addr);
            byte[] addressWithChecksum = new byte[36];
            System.arraycopy(addr, 0, addressWithChecksum, 0, 34);
            System.arraycopy(crc16, 0, addressWithChecksum, 34, 2);

            String address = Base64.toBase64String(addressWithChecksum);
            if (isUrlSafe) {
                address = java.util.Base64.getUrlEncoder().encodeToString(addressWithChecksum);
            }
            return address;
        }

    }

    @Override
    public String toString() {
        return toAddressString(isUserFriendly, isUrlSafe, isBounceable, isTestOnly);
    }


    public static boolean validAddress(String address) {
        try {
            new Address(address);
        } catch (Exception e) {
            return false;
        }
        return true;
    }
}