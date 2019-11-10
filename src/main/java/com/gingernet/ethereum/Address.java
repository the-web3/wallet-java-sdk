package com.gingernet.ethereum;

import com.google.common.collect.ImmutableList;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.Test;
import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;

import java.io.IOException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Address {
    private Web3j web3j;
    private Credentials credentials;

    // 直接生成以太坊地址
    public Map<String, String> CreateEthAddress() {
        String seed = UUID.randomUUID().toString();
        String sPrivatekeyInHex ="";
        String sAddress = "";
        Map<String, String> ethMap = new HashMap<>();
        try {
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            BigInteger privateKeyInDec = ecKeyPair.getPrivateKey();
            sPrivatekeyInHex = privateKeyInDec.toString(16);
            WalletFile aWallet = Wallet.createLight(seed, ecKeyPair);
            sAddress = aWallet.getAddress();
        } catch ( Exception e){
            e.printStackTrace();
        }
        ethMap.put("ethPrKey", sPrivatekeyInHex);
        ethMap.put("ethAddress", "0x" + sAddress);
        return ethMap;
    }

    // 使用助记词的形式生成 ETH 地址
    public Map<String, String> CreateEthAddressByMneminic(List<String> str) throws Exception {
        Map<String, String> ethMap = new HashMap<>();
        byte[] seed = MnemonicCode.toSeed(str, "");
        DeterministicKey masterPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(masterPrivateKey);
        ImmutableList<ChildNumber> BIP44_ETH_ACCOUNT_ZERO_PATH =
                ImmutableList.of(new ChildNumber(44, true), new ChildNumber(60, true),
                        ChildNumber.ZERO_HARDENED, ChildNumber.ZERO);
        DeterministicKey deterministicKey = deterministicHierarchy.deriveChild(BIP44_ETH_ACCOUNT_ZERO_PATH, false, true, new ChildNumber(0));
        byte[] bytes = deterministicKey.getPrivKeyBytes();
        ECKeyPair keyPair = ECKeyPair.create(bytes);
        String address = Keys.getAddress(keyPair.getPublicKey());

        ethMap.put("ethPrKey", "0x"+keyPair.getPrivateKey().toString(16));
        ethMap.put("ethAddress", "0x" + address);
        ethMap.put("ethPubKey", keyPair.getPublicKey().toString(16));
        return ethMap;
    }
}
