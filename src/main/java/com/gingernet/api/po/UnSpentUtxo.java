package com.gingernet.api.po;

import java.io.Serializable;

public class UnSpentUtxo implements Serializable {
    private static final long serialVersionUID = -7417428486644921613L;
    private String hash;
    private long txN;
    private long value;
    private int height;
    private String script;
    private String address;

    public String getHash() {
        return hash;
    }
    public void setHash(String hash) {
        this.hash = hash;
    }
    public long getTxN() {
        return txN;
    }
    public void setTxN(long txN) {
        this.txN = txN;
    }
    public long getValue() {
        return value;
    }
    public void setValue(long value) {
        this.value = value;
    }
    public int getHeight() {
        return height;
    }
    public void setHeight(int height) {
        this.height = height;
    }
    public String getScript() {
        return script;
    }
    public void setScript(String script) {
        this.script = script;
    }
    public String getAddress() {
        return address;
    }
    public void setAddress(String address) {
        this.address = address;
    }
}
