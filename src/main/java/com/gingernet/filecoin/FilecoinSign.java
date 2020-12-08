package com.gingernet.filecoin;

import cn.hutool.core.codec.Base64Encoder;
import co.nstant.in.cbor.CborBuilder;
import co.nstant.in.cbor.CborEncoder;
import com.gingernet.filecoin.utils.Base32;
import com.gingernet.filecoin.utils.SignData;
import com.gingernet.filecoin.utils.Transaction;
import com.gingernet.utils.Blake2b;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import org.bitcoinj.crypto.*;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;
import co.nstant.in.cbor.model.ByteString;
import co.nstant.in.cbor.model.UnsignedInteger;
import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.util.List;

public class FilecoinSign {
    public static byte[] CID_PREFIX = new byte[]{0x01, 0x71, (byte) 0xa0, (byte) 0xe4, 0x02, 0x20};
    public static final ChildNumber FIL_HARDENED = new ChildNumber(461, true);


    public static SignData transaction(Transaction tran) {
        //构建交易结构体
        byte[] from = getByte(tran.getFrom());
        byte[] to = getByte(tran.getTo());
        SignData signData = new SignData();
        signData.setVersion(new UnsignedInteger(0));
        signData.setTo(new ByteString(to));
        signData.setFrom(new ByteString(from));
        signData.setNonce(new UnsignedInteger(tran.getNonce()));
        ByteString valueByteString;
        if (new BigInteger(tran.getValue()).toByteArray()[0] != 0) {
            byte[] byte1 = new byte[new BigInteger(tran.getValue()).toByteArray().length + 1];
            byte1[0] = 0;
            System.arraycopy(new BigInteger(tran.getValue()).toByteArray(), 0, byte1, 1, new BigInteger(tran.getValue()).toByteArray().length);
            valueByteString = new ByteString(byte1);
        } else {
            valueByteString = new ByteString(new BigInteger(tran.getValue()).toByteArray());
        }
        signData.setValue(valueByteString);
        signData.setGasLimit(new UnsignedInteger(tran.getGasLimit()));
        ByteString gasFeeCapString;
        if (new BigInteger(tran.getGasFeeCap()).toByteArray()[0] != 0) {
            byte[] byte2 = new byte[new BigInteger(tran.getGasFeeCap()).toByteArray().length + 1];
            byte2[0] = 0;
            System.arraycopy(new BigInteger(tran.getGasFeeCap()).toByteArray(), 0, byte2, 1
                    , new BigInteger(tran.getGasFeeCap()).toByteArray().length);
            gasFeeCapString = new ByteString(byte2);
        } else {
            gasFeeCapString = new ByteString(new BigInteger(tran.getGasFeeCap()).toByteArray());
        }
        signData.setGasFeeCap(gasFeeCapString);
        ByteString gasGasPremium;
        if (new BigInteger(tran.getGasPremium()).toByteArray()[0] != 0) {
            byte[] byte2 = new byte[new BigInteger(tran.getGasPremium()).toByteArray().length + 1];
            byte2[0] = 0;
            System.arraycopy(new BigInteger(tran.getGasPremium()).toByteArray(), 0, byte2, 1
                    ,new BigInteger(tran.getGasPremium()).toByteArray().length);
            gasGasPremium = new ByteString(byte2);
        } else {
            gasGasPremium = new ByteString(new BigInteger(tran.getGasPremium()).toByteArray());
        }
        signData.setGasPremium(gasGasPremium);
        signData.setMethodNum(new UnsignedInteger(0));
        signData.setParams(new ByteString(new byte[0]));
        return signData;
    }

    public static String signTransaction(Transaction tran, List<String> mnemonicWords) {
        SignData signData = transaction(tran);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            new CborEncoder(baos).encode(new CborBuilder()
                    .addArray()
                    .add(signData.getVersion())
                    .add(signData.getTo())
                    .add(signData.getFrom())
                    .add(signData.getNonce())
                    .add(signData.getValue())
                    .add(signData.getGasLimit())
                    .add(signData.getGasFeeCap())
                    .add(signData.getGasPremium())
                    .add(signData.getMethodNum())
                    .add(new ByteString(new byte[]{}))
                    .end()
                    .build());
            byte[] encodedBytes = baos.toByteArray();
            byte[] cidHashBytes = getCidHash(encodedBytes);
            return sign(cidHashBytes,mnemonicWords);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getCidHash(byte[] message) {
        byte[] messageByte = Blake2b.Digest.newInstance(32).digest(message);
        int xlen = CID_PREFIX.length;
        int ylen = messageByte.length;
        byte[] result = new byte[xlen + ylen];
        System.arraycopy(CID_PREFIX, 0, result, 0, xlen);
        System.arraycopy(messageByte, 0, result, xlen, ylen);
        byte[] prefixByte = Blake2b.Digest.newInstance(32).digest(result);
        String prefixByteHex = Numeric.toHexString(prefixByte).substring(2);
        System.out.println("prefixByteHex="+prefixByteHex);
        return prefixByte;
    }

    public static String sign(byte[] cidHash, List<String> mnemonicWords) {
        byte[] seed = MnemonicCode.toSeed(mnemonicWords, "");
        DeterministicKey rootPrivateKey = HDKeyDerivation.createMasterPrivateKey(seed);
        DeterministicHierarchy deterministicHierarchy = new DeterministicHierarchy(rootPrivateKey);
        ImmutableList<ChildNumber> path = ImmutableList.of(new ChildNumber(44, true), FIL_HARDENED, ChildNumber.ZERO_HARDENED);
        DeterministicKey fourpath = deterministicHierarchy.get(path, true, true);
        DeterministicKey fourpathhd = HDKeyDerivation.deriveChildKey(fourpath, 0);
        DeterministicKey fivepathhd = HDKeyDerivation.deriveChildKey(fourpathhd, 0);
        ECKeyPair ecKeyPair = ECKeyPair.create(fivepathhd.getPrivKeyBytes());
        Sign.SignatureData signatureData = Sign.signMessage(cidHash, ecKeyPair);
        byte[] sig = getSignature(signatureData);
        String stringHex = Numeric.toHexString(sig).substring(2);
        String base64 = Base64Encoder.encode(sig);
        System.out.println("stringHex=="+stringHex);
        System.out.println("base64=="+base64);
        return base64;
    }

    private static byte[] getSignature(Sign.SignatureData signatureData) {
        byte[] sig = new byte[65];
        System.arraycopy(signatureData.getR(), 0, sig, 0, 32);
        System.arraycopy(signatureData.getS(), 0, sig, 32, 32);
        sig[64] = (byte) ((signatureData.getV() & 0xFF) - 27);
        return sig;
    }

    public static byte[] getByte(String addressStr) {
        String str = addressStr.substring(2);
        byte[] bytes12 = new byte[21];
        bytes12[0] = 1;
        System.arraycopy(Base32.decode(str), 0, bytes12, 1, 20);
        return bytes12;
    }

}
