package com.gingernet.filecoin;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.gingernet.filecoin.utils.Transaction;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class FileCoinWallet {
    public String BuildTransaction(String addrfrom, String addrto, BigDecimal amount, BigDecimal mbfee, String ContactAddress) {
        BigInteger nonce = new BigInteger("500000000");
        BigDecimal pow = BigDecimal.TEN.pow(6);
        BigDecimal gasLimit = nonce.compareTo(BigInteger.ZERO)==0?new BigDecimal(3).multiply(pow)
                :new BigDecimal(2).multiply(pow);
        BigDecimal fee = new BigDecimal("0.001");
        Transaction transaction = new Transaction();
        transaction.setFrom(addrfrom);
        transaction.setTo(addrto);
        transaction.setNonce(nonce.longValue());
        transaction.setValue(amount.toString());
        transaction.setGasLimit(gasLimit.longValue());
        transaction.setGasPremium("1000129");
        transaction.setGasFeeCap("500000000");
        return JSON.toJSONString(transaction);
    }

    public String SignTransaction (String inputTransaction, String addr, List<String> mnemonicWords, String passPhrase) {
        Transaction tran = new Transaction();
        JSONObject jsonObject = JSONObject.parseObject(inputTransaction);
        String value = jsonObject.getString("value");
        BigDecimal amount = new BigDecimal(value).multiply(BigDecimal.TEN.pow(18));
        tran.setFrom(jsonObject.getString("from"));
        tran.setTo(jsonObject.getString("to"));
        tran.setValue(amount.toBigInteger().toString());
        tran.setNonce(jsonObject.getLong("nonce"));
        tran.setGasLimit(jsonObject.getLong("gasLimit"));
        tran.setGasFeeCap(jsonObject.getString("gasFeeCap"));
        tran.setGasPremium(jsonObject.getString("gasPremium"));
        tran.setMethod(0L);
        tran.setParams("");
        String sign = FilecoinSign.signTransaction(tran,mnemonicWords);
        JSONObject cid = new JSONObject();
        JSONObject signer = new JSONObject();
        JSONObject message = new JSONObject();
        JSONObject callback = new JSONObject();
        JSONObject ncid = new JSONObject();
        cid.put("/","");
        ncid.put("/","");
        signer.put("Type",1);
        signer.put("Data",sign);
        message.put("To",tran.getTo());
        message.put("From",tran.getFrom());
        message.put("Nonce",tran.getNonce());
        message.put("Value",tran.getValue());
        message.put("GasLimit",tran.getGasLimit());
        message.put("GasFeeCap",tran.getGasFeeCap());
        message.put("GasPremium",tran.getGasPremium());
        message.put("Method",0);
        message.put("Params","");
        message.put("CID",cid);
        callback.put("Message",message);
        callback.put("Signature",signer);
        callback.put("CID",ncid);
        return callback.toJSONString();
    }
}
