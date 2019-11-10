package com.gingernet.word;

import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;

import java.security.SecureRandom;
import java.util.List;

public class WordList {

    // 生成助记词
    public List<String> CreateWord() throws Exception {
        SecureRandom secureRandom = new SecureRandom();
        //DEFAULT_SEED_ENTROPY_BITS 生成 12 个助记词；MAX_SEED_ENTROPY_BITS 生成 24 个
        // 根据参数不同生成 12 - 24 个助记词
        byte[] entropy = new byte[DeterministicSeed.MAX_SEED_ENTROPY_BITS / 8];
        secureRandom.nextBytes(entropy);
        List<String> strList = MnemonicCode.INSTANCE.toMnemonic(entropy);
        //byte[] seed = MnemonicCode.toSeed(str, "");
        return strList;
    }

    // 由助记词生成 Seed
    public byte[] GenSeed(List<String> strList) {
        byte[] seed = MnemonicCode.toSeed(strList, "");
        return seed;
    }

    public String WordEcode() {
        return "";
    }

    public String wordDecode() {
        return "";
    }

    public boolean ValidAddress() {
        return true;
    }

}
