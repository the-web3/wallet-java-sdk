package com.gingernet.toncoin;

import com.alibaba.fastjson.JSONObject;
import org.spongycastle.util.encoders.Hex;


public class AddressTest01 {

    public static void main(String[] args) {
        KeyPair keyPair = KeyPair.random();
        String publicKey = Hex.toHexString(keyPair.getPublicKey());
        System.out.println("private key: " + Hex.toHexString(keyPair.getPrivateKey()));
        System.out.println("public key: " + publicKey);

        Contract contract = new WalletV3ContractR2(publicKey);
        JSONObject object = contract.createStateInit();
        Address address = object.getObject("address", Address.class);
        String addressString = address.toAddressString(true, true, true, false);
        System.out.println("address: " + addressString);

    }

}