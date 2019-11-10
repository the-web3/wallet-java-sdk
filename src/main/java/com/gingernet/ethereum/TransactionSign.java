package com.gingernet.ethereum;

import org.web3j.abi.FunctionEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.Web3j;
import org.web3j.tx.ChainId;
import org.web3j.utils.Convert;
import org.web3j.utils.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Arrays;
import java.util.Collections;

public class TransactionSign {

    public String SignEthTx(BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, String to, Double value,  String privateKey) throws Exception {
        if (privateKey.startsWith("0x")) {
            privateKey = privateKey.substring(2);
        }
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);
        BigDecimal amountInWei = Convert.toWei(value.toString(), Convert.Unit.ETHER);
        RawTransaction rawTransaction = RawTransaction.createEtherTransaction(nonce, gasPrice, gasLimit, to, amountInWei.toBigInteger());
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, ChainId.MAINNET, credentials);
        return Numeric.toHexString(signMessage);
    }

    // 拼接参数的方式实现 ERC20 的币种的签名
    public String SignErc20Tx(String contractAddress, String to, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit, BigDecimal amount, BigDecimal decimal, String privateKey) throws Exception {
        BigDecimal realValue = amount.multiply(decimal);
        String data = "0xa9059cbb" +
                Numeric.toHexStringNoPrefixZeroPadded(Numeric.toBigInt(to), 64) +
                Numeric.toHexStringNoPrefixZeroPadded(realValue.toBigInteger(), 64);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, ChainId.MAINNET, credentials);
        return Numeric.toHexString(signMessage);
    }

    // 通过 Web3j 组织参数的形式对交易所进行签名
    public String SignErc20Web3jTx(String contractAddress, String to, BigInteger nonce, BigInteger gasPrice, BigInteger gasLimit,
                                 BigDecimal amount, BigDecimal decimal, String privateKey) throws Exception {
        BigDecimal realValue = amount.multiply(decimal);
        Function function = new Function("transfer", Arrays.asList(new Address(to), new Uint256(realValue.toBigInteger())), Collections.emptyList());
        String data = FunctionEncoder.encode(function);
        RawTransaction rawTransaction = RawTransaction.createTransaction(nonce, gasPrice, gasLimit, contractAddress, data);
        ECKeyPair ecKeyPair = ECKeyPair.create(new BigInteger(privateKey, 16));
        Credentials credentials = Credentials.create(ecKeyPair);
        byte[] signMessage = TransactionEncoder.signMessage(rawTransaction, ChainId.MAINNET, credentials);
        return Numeric.toHexString(signMessage);
    }

}
