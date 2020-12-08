package com.gingernet.filecoin;

import cn.hutool.core.codec.Base32;
import com.gingernet.utils.Blake2b;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.crypto.*;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bouncycastle.crypto.digests.Blake2bDigest;
import org.web3j.utils.Numeric;
import javax.management.ImmutableDescriptor;
import java.security.SecureRandom;
import java.util.List;


public class Address {
    public static final ChildNumber FIL_HARDENED = new ChildNumber(461, true);


    public String getBip44Credentials(List<String> mnemonicWords, String passPhrase, int number) {
        byte[] seed = MnemonicCode.toSeed(mnemonicWords, "");
        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(rootPrivateKey);
        ImmutableList<ChildNumber> path = ImmutableList.of(new ChildNumber(44, true), FIL_HARDENED, ChildNumber.ZERO_HARDENED);
        DeterministicKey fourpath = deterministicHierarchy.get(path, true, true);
        DeterministicKey fourpathhd = HDKeyDerivation.deriveChildKey(fourpath, 0);
        DeterministicKey fivepathhd = HDKeyDerivation.deriveChildKey(fourpathhd, number);
        Blake2b.Digest blake2b1 = Blake2b.Digest.newInstance(20);
        ECKey ecKey = ECKey.fromPrivate(fivepathhd.getPrivKey(),false);
        String pulStr = ecKey.getPublicKeyAsHex();
        byte[] bytes = Numeric.hexStringToByteArray(pulStr);
        byte[] black2HashByte = blake2b1.digest(bytes);
        String black2HashStr = Numeric.toHexStringNoPrefix(black2HashByte);
        String black2HashSecond = "0x01"+black2HashStr;
        Blake2b.Digest blake2b2 = Blake2b.Digest.newInstance(4);
        byte[] checksumBytes = blake2b2.digest(Numeric.hexStringToByteArray(black2HashSecond));
        byte[] addressBytes = new byte[black2HashByte.length + checksumBytes.length];
        System.arraycopy(black2HashByte, 0, addressBytes, 0, black2HashByte.length);
        System.arraycopy(checksumBytes, 0, addressBytes, black2HashByte.length, checksumBytes.length);
        // f 正式 t 测试 1 钱包 2 合约
        return "f1"+Base32.encode(addressBytes);
    }
}
