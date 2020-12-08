package com.gingernet.filecoin.utils;

import lombok.Data;


@Data
public class Transaction {
    private String to;
    private String from;
    private Long nonce;
    private String value;
    private Long gasLimit;
    private String gasFeeCap;
    private String gasPremium;
    private Long method;
    private String params;
    private Integer version;
    private String CID;

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Long getNonce() {
        return nonce;
    }

    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public Long getGasLimit() {
        return gasLimit;
    }

    public void setGasLimit(Long gasLimit) {
        this.gasLimit = gasLimit;
    }

    public String getGasFeeCap() {
        return gasFeeCap;
    }

    public void setGasFeeCap(String gasFeeCap) {
        this.gasFeeCap = gasFeeCap;
    }

    public String getGasPremium() {
        return gasPremium;
    }

    public void setGasPremium(String gasPremium) {
        this.gasPremium = gasPremium;
    }

    public Long getMethod() {
        return method;
    }

    public void setMethod(Long method) {
        this.method = method;
    }

    public String getParams() {
        return params;
    }

    public void setParams(String params) {
        this.params = params;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getCID() {
        return CID;
    }

    public void setCID(String CID) {
        this.CID = CID;
    }
}
