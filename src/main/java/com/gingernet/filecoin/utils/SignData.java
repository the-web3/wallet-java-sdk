package com.gingernet.filecoin.utils;

import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.UnsignedInteger;
import lombok.Data;

@Data
public class SignData {
    private UnsignedInteger version;
    private ByteString from;
    private ByteString to;
    private UnsignedInteger nonce;
    private ByteString value;
    private ByteString gasFeeCap;
    private ByteString gasPremium;
    private UnsignedInteger gasLimit;
    private UnsignedInteger methodNum;
    private ByteString params; //空数组

    public UnsignedInteger getVersion() {
        return version;
    }

    public void setVersion(UnsignedInteger version) {
        this.version = version;
    }

    public ByteString getFrom() {
        return from;
    }

    public void setFrom(ByteString from) {
        this.from = from;
    }

    public ByteString getTo() {
        return to;
    }

    public void setTo(ByteString to) {
        this.to = to;
    }

    public UnsignedInteger getNonce() {
        return nonce;
    }

    public void setNonce(UnsignedInteger nonce) {
        this.nonce = nonce;
    }

    public ByteString getValue() {
        return value;
    }

    public void setValue(ByteString value) {
        this.value = value;
    }

    public ByteString getGasFeeCap() {
        return gasFeeCap;
    }

    public void setGasFeeCap(ByteString gasFeeCap) {
        this.gasFeeCap = gasFeeCap;
    }

    public ByteString getGasPremium() {
        return gasPremium;
    }

    public void setGasPremium(ByteString gasPremium) {
        this.gasPremium = gasPremium;
    }

    public UnsignedInteger getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(UnsignedInteger gasLimit) {
        this.gasLimit = gasLimit;
    }

    public UnsignedInteger getMethodNum() {
        return methodNum;
    }

    public void setMethodNum(UnsignedInteger methodNum) {
        this.methodNum = methodNum;
    }

    public ByteString getParams() {
        return params;
    }

    public void setParams(ByteString params) {
        this.params = params;
    }
}

