package com.gingernet.toncoin;

import com.alibaba.fastjson.JSONObject;
import org.spongycastle.util.encoders.Hex;


public class AddressTest02 {

    public static void main(String[] args) {
        KeyPair keyPair = KeyPair.fromSecretSeed(Hex.decode("1091a79e7d50246479ef1ae489b33e6aba7077f64463376597fd8e3fe838d660"));
        String publicKey = Hex.toHexString(keyPair.getPublicKey());

        Contract contract = new WalletV3ContractR2(publicKey);
        JSONObject object = contract.createStateInit();
        Address address = object.getObject("address", Address.class);
        String string = address.toAddressString(true, true, true, false);
        System.out.println(string);
    }

}