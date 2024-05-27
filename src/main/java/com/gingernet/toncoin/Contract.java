package com.gingernet.toncoin;

import com.alibaba.fastjson.JSONObject;

import java.util.Date;

import static net.i2p.crypto.eddsa.Utils.bytesToHex;
import static net.i2p.crypto.eddsa.Utils.hexToBytes;


public class Contract {

    protected byte[] pubkey;
    protected Address address;
    protected Cell code;
    protected byte workchain;
    protected int walletId = 0;

    public Contract(String pubkey) {
        this.pubkey = hexToBytes(pubkey);
    }

    public Contract() {
    }

    Date date = new Date();

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Address getAddress() {
        if (this.address == null) {
            this.address = this.createStateInit().getObject("address", Address.class);
        }
        return this.address;
    }

    public JSONObject createStateInit() {
        Cell codeCell = this.createCodeCell();
        Cell dataCell = this.createDataCell();
        Cell stateInit = Contract.createStateInit(codeCell, dataCell);
        byte[] stateInitHash = stateInit.hash();
        Address address = new Address(this.workchain + ":" + bytesToHex(stateInitHash));

        JSONObject object = new JSONObject();
        object.put("stateInit", stateInit);
        object.put("address", address);
        object.put("code", codeCell);
        object.put("data", dataCell);
        return object;
    }

    public Cell createCodeCell() {
        if (this.code == null) {
            throw new IllegalArgumentException("Contract: options.code is not defined");
        }
        Cell cell = new Cell();
        cell.bits.writeBytes(this.code.bits.getTopUppedArray());
        return cell;
    }

    public Cell createDataCell() {
        return new Cell();
    }

    public Cell createSigningMessage(long seqno) {
        return new Cell();
    }

    public static Cell createStateInit(Cell code, Cell data) {
        boolean library = false;
        boolean splitDepth = false;
        boolean ticktock = false;

        Cell stateInit = new Cell();
        boolean[] bls = new boolean[5];
        bls[0] = splitDepth;
        bls[1] = ticktock;
        bls[2] = code != null;
        bls[3] = data != null;
        bls[4] = library;
        stateInit.bits.writeBitArray(bls);

        if (code != null) {
            stateInit.refs.add(code);
        }
        if (data != null) {
            stateInit.refs.add(data);
        }
        if (library) {
            stateInit.refs.add(library);
        }
        return stateInit;
    }

    public static Cell createExternalMessageHeader(String dest, String src, int importFee) {
        Cell message = new Cell();
        message.bits.writeUint(2, 2);
        message.bits.writeAddress(src != null ? new Address(src) : null);
        message.bits.writeAddress(new Address(dest));
        message.bits.writeGrams(importFee);
        return message;
    }

    public JSONObject createInitExternalMessage(byte[] secretKey) {
        if (this.pubkey == null) {
            KeyPair keyPair = KeyPair.fromSecretSeed(secretKey);
            this.pubkey = keyPair.getPublicKey();
        }
        JSONObject object = this.createStateInit();
        // {stateInit, address, code, data}
        Address address = object.getObject("address", Address.class);
        Cell code = object.getObject("code", Cell.class);
        Cell data = object.getObject("data", Cell.class);
        Cell stateInit = object.getObject("stateInit", Cell.class);

        Cell signingMessage = this.createSigningMessage(0);
        KeyPair keyPair = KeyPair.fromSecretSeed(secretKey);
        byte[] signature = keyPair.sign(signingMessage.hash());
        Cell body = new Cell();
        body.bits.writeBytes(signature);
        body.writeCell(signingMessage);

        Cell header = Contract.createExternalMessageHeader(address.toString(), null, 0);
        Cell externalMessage = Contract.createCommonMsgInfo(header, stateInit, body);

        JSONObject out = new JSONObject();
        out.put("address", this.address);
        out.put("message", externalMessage);
        out.put("body", body);
        out.put("signingMessage", signingMessage);
        out.put("stateInit", stateInit);
        out.put("code", this.code);
        out.put("data", data);
        return out;
    }

    public static Cell createCommonMsgInfo(Cell header, Cell stateInit, Cell body) {
        Cell commonMsgInfo = new Cell();
        commonMsgInfo.writeCell(header);

        if (stateInit != null) {
            commonMsgInfo.bits.writeBit(true);
            if (commonMsgInfo.bits.getFreeBits() - 1 >= stateInit.bits.getUsedBits()) {
                commonMsgInfo.bits.writeBit(false);
                commonMsgInfo.writeCell(stateInit);
            } else {
                commonMsgInfo.bits.writeBit(true);
                commonMsgInfo.refs.add(stateInit);
            }
        } else {
            commonMsgInfo.bits.writeBit(false);
        }

        if (body != null) {
            if (commonMsgInfo.bits.getFreeBits() >= body.bits.getUsedBits()) {
                commonMsgInfo.bits.writeBit(false);
                commonMsgInfo.writeCell(body);
            } else {
                commonMsgInfo.bits.writeBit(true);
                commonMsgInfo.refs.add(body);
            }
        } else {
            commonMsgInfo.bits.writeBit(false);
        }
        return commonMsgInfo;
    }

    public static Cell createInternalMessageHeader(Address dest, long gramValue, boolean ihrDisabled, boolean bounce,
                                                   boolean bounced, Address src, boolean currencyCollection, long ihrFees,
                                                   long fwdFees, long createdLt, long createdAt) {
        Cell message = new Cell();
        message.bits.writeBit(false);
        message.bits.writeBit(ihrDisabled);
        if (bounced) {
            message.bits.writeBit(bounce);
        } else {
            message.bits.writeBit(dest.isBounceable);
        }
        message.bits.writeBit(bounced);
        message.bits.writeAddress(src);
        message.bits.writeAddress(dest);
        message.bits.writeGrams(gramValue);
        if (currencyCollection) {
            throw new IllegalArgumentException("Currency collections are not implemented yet");
        }
        message.bits.writeBit(currencyCollection);
        message.bits.writeGrams(ihrFees);
        message.bits.writeGrams(fwdFees);
        message.bits.writeUint(createdLt, 64);
        message.bits.writeUint(createdAt, 32);
        return message;
    }
}