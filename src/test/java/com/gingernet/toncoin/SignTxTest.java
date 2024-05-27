package com.gingernet.toncoin;

import com.alibaba.fastjson.JSONObject;
import org.spongycastle.util.encoders.Base64;
import org.spongycastle.util.encoders.Hex;

import java.math.BigDecimal;
import java.util.Date;


public class SignTxTest {

    public static void main(String[] args) {
        byte[] seed = Hex.decode("1091a79e7d50246479ef1ae489b33e6aba7077f64463376597fd8e3fe838d660");
        KeyPair keyPair = KeyPair.fromSecretSeed(seed);
        String publicKey = Hex.toHexString(keyPair.getPublicKey());

        long seqno = 0L;
        BigDecimal value = BigDecimal.valueOf(0.01);
        long amount = value.multiply(BigDecimal.valueOf(Math.pow(10, 9))).longValue();
        // to address
        String to = "EQC9n2Gvzo3DqokN6w8sSK_lAO7iWH8sYjewkENqN8q_BS4H";
        Boolean bounced = false;
        to = new Address(to).toAddressString(true, true, bounced, false);
        Address toAddress = new Address(to);

        WalletContract contract = new WalletV3ContractR2(publicKey);
        contract.setDate(new Date());   // Set the transaction time, the default is 10min valid
        JSONObject object = contract.createTransferMessage(seed, toAddress, amount, seqno, "Comment", 3, false);
        Cell message = object.getObject("message", Cell.class);
        byte[] bytes = message.toBoc(false, true, false, 0);
        System.out.println(Base64.toBase64String(bytes));
    }

}