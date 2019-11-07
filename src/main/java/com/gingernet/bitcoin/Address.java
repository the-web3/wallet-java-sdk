package com.gingernet.bitcoin;

import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.params.MainNetParams;

import java.util.HashMap;
import java.util.Map;

public class Address {
    static NetworkParameters params;

    // 生成 WIF 格式的地址
    public Map<String, String> generateBtcAddress() {
        NetworkParameters params = MainNetParams.get();
        ECKey key = new ECKey();
        Map<String, String> btcMap = new HashMap<>();
        btcMap.put("btcWifPk", key.getPrivateKeyAsWiF(params));
        btcMap.put("btcPk", key.getPrivateKeyAsHex());
        btcMap.put("btcPuKey", key.getPublicKeyAsHex());
        btcMap.put("btcAddress", key.toAddress(params).toString());
        return btcMap;
    }

    // 生成隔离验证地址

    // 生成 Bech32 地址

    // 由助记词生成地址流程

}
