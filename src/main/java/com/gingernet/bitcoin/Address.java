package com.gingernet.bitcoin;

import com.gingernet.utils.Utils;
import org.bitcoinj.core.Base58;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.crypto.HDUtils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.wallet.DeterministicKeyChain;
import org.bitcoinj.wallet.DeterministicSeed;
import org.bitcoinj.wallet.UnreadableWalletException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.crypto.digests.RIPEMD160Digest;

import java.math.BigInteger;
import java.security.*;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPoint;
import java.util.HashMap;
import java.util.Map;

public class Address {
    private Logger logger = LoggerFactory.getLogger(getClass());
    static NetworkParameters params;

    // 生成 WIF 格式的地址(中心化钱包使用)
    public Map<String, String> generateBtcAddress() {
        NetworkParameters paramsTest = TestNet3Params.get();
        //NetworkParameters params = MainNetParams.get();
        ECKey key = new ECKey();
        Map<String, String> btcMap = new HashMap<>();
        btcMap.put("btcWifPk", key.getPrivateKeyAsWiF(paramsTest));
        btcMap.put("btcPk", key.getPrivateKeyAsHex());
        btcMap.put("btcPuKey", key.getPublicKeyAsHex());
        btcMap.put("btcAddress", key.toAddress(paramsTest).toString());
        return btcMap;
    }

    // 由助记词生成地址流程（去中心化钱包使用）
    public String CreateAddressByWord(String wordsList) throws Exception {
        NetworkParameters params  = TestNet3Params.get();
        DeterministicSeed deterministicSeed = new DeterministicSeed(wordsList, null, "", 0L);
        DeterministicKeyChain deterministicKeyChain = DeterministicKeyChain.builder().seed(deterministicSeed).build();
        BigInteger privKey = deterministicKeyChain.getKeyByPath(HDUtils.parsePath("44H / 1H / 0H / 0 / 2"), true).getPrivKey();
        ECKey ecKey = ECKey.fromPrivate(privKey);
        org.bitcoinj.core.Address address = ecKey.toAddress(params);
        System.out.println(ecKey.getPrivateKeyAsWiF(params));
        return address.toBase58();
    }

    // 比特币系列地址生成流程
    public String bitcoinS(String version) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("secp256k1");
            keyGen.initialize(ecSpec);
            KeyPair kp = keyGen.generateKeyPair();
            PublicKey pub = kp.getPublic();
            PrivateKey pvt = kp.getPrivate();
            ECPrivateKey epvt = (ECPrivateKey) pvt;
            String sepvt = Utils.adjustTo64(epvt.getS().toString(16)).toUpperCase();
            logger.warn("s[" + sepvt.length() + "]: " + sepvt);
            logger.warn("私钥{}", sepvt);
            ECPublicKey epub = (ECPublicKey) pub;
            ECPoint pt = epub.getW();
            String sx = Utils.adjustTo64(pt.getAffineX().toString(16)).toUpperCase();
            String sy = Utils.adjustTo64(pt.getAffineY().toString(16)).toUpperCase();
            String bcPub = "04" + sx + sy;
            logger.warn("公钥{}", bcPub);
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] s1 = sha.digest(bcPub.getBytes("UTF-8"));
            logger.warn("sha256后{}", Utils.byte2Hex(s1).toUpperCase());
            RIPEMD160Digest digest = new RIPEMD160Digest();
            digest.update(s1, 0, s1.length);
            byte[] ripemd160Bytes = new byte[digest.getDigestSize()];
            digest.doFinal(ripemd160Bytes, 0);
            logger.warn("ripemd160加密后{}", Utils.bytesToHexString(ripemd160Bytes));
            byte[] networkID = new BigInteger(version, 16).toByteArray();
            byte[] extendedRipemd160Bytes = Utils.add(networkID, ripemd160Bytes);
            logger.warn("添加NetworkID后{}", Utils.bytesToHexString(extendedRipemd160Bytes));
            byte[] twiceSha256Bytes = Utils.sha256(Utils.sha256(extendedRipemd160Bytes));
            logger.warn("两次sha256加密后{}", Utils.bytesToHexString(twiceSha256Bytes));
            byte[] checksum = new byte[4];
            System.arraycopy(twiceSha256Bytes, 0, checksum, 0, 4);
            logger.warn("checksum{}", Utils.bytesToHexString(checksum));
            byte[] binaryBitcoinAddressBytes = Utils.add(extendedRipemd160Bytes, checksum);
            logger.warn("添加checksum之后{}", Utils.bytesToHexString(binaryBitcoinAddressBytes));
            String ltccoinAddress = Base58.encode(binaryBitcoinAddressBytes);
            logger.warn("地址{}", ltccoinAddress);
            return ltccoinAddress;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }


}
