package com.gingernet.ethereum;

import org.assertj.core.internal.cglib.asm.$ClassReader;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.Request;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.*;
import org.web3j.protocol.http.HttpService;
import org.web3j.utils.Convert;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;

public class EthRpc {
    private static final String PRO_URL = "https://mainnet.infura.io/PVMw2QL6TZTb2TTgIgrs";

    public String GetEthBalance(String acount) {
        String balance = "";
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        BigInteger bal = null;
        try{
            bal = web3j.ethGetBalance(acount, DefaultBlockParameterName.LATEST).send().getBalance();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BigDecimal value = new BigDecimal(bal);
        BigDecimal ether = Convert.fromWei(value, Convert.Unit.ETHER);
        balance = ether.toString();
        return balance;
    }

    public String GetErc20Balance(String account, String contractAddress) {
        String balance = "";
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        String bal = "";
        String decimals = "";
        try {
            bal = web3j.ethCall(Transaction.createEthCallTransaction(account, contractAddress, "0x70a08231" + "000000000000000000000000" + account.substring(2)), DefaultBlockParameterName.LATEST).send().getResult();
            decimals = web3j.ethCall(Transaction.createEthCallTransaction(account, contractAddress, "0x313ce567"), DefaultBlockParameterName.LATEST).send().getResult();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BigInteger bal_bi = new BigInteger(bal.substring(2, bal.length()), 16);
        BigInteger dec_bi = new BigInteger(decimals.substring(2, decimals.length()), 16);
        BigDecimal bal_bd = new BigDecimal(bal_bi);
        BigDecimal decimals_bd = new BigDecimal(dec_bi);
        BigDecimal ten = new BigDecimal(10);
        BigDecimal fixed = bal_bd.divide(ten.pow(decimals_bd.intValue()), decimals_bd.intValue(), BigDecimal.ROUND_HALF_UP);
        balance = fixed.toString();
        return balance;
    }

    public String GetEthTxFee() throws Exception {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        BigInteger txf_bi = null;
        try {
            txf_bi = web3j.ethGasPrice().send().getGasPrice();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txf_bi.toString(10);
    }

    public BigInteger GetGasLimit(String formAddress, String toAddress, BigInteger value, BigInteger nonce,
                                  BigInteger gasPrice, String data, BigInteger gasLimit) throws Exception {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        Transaction transaction = new Transaction(formAddress,nonce, gasPrice, gasLimit, toAddress, value, data);
        transaction.getFrom();
        transaction.getTo();
        transaction.getValue();
        transaction.getData();
        transaction.getGasPrice();
        transaction.getNonce();
        transaction.getGas();
        EthEstimateGas ethEstimateGas;
        try {
            ethEstimateGas = web3j.ethEstimateGas(transaction).send();
            if (ethEstimateGas.hasError()){
                throw new RuntimeException(ethEstimateGas.getError().getMessage());
            }
        } catch (Exception e) {
            throw new RuntimeException("网络错误");
        }
        return ethEstimateGas.getAmountUsed();
    }

    public String GetEthTxCount(String account) {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        BigInteger txc_bi = null;
        try {
            txc_bi = web3j.ethGetTransactionCount(account, DefaultBlockParameterName.LATEST).send().getTransactionCount();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txc_bi.toString(10);
    }

    public String SendEthRawTx(String data) throws Exception {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        EthSendTransaction ethSendTransaction = new EthSendTransaction();
        String hash = "";
        try {
            ethSendTransaction = web3j.ethSendRawTransaction(data).send();
            System.out.println(ethSendTransaction.getRawResponse());
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (ethSendTransaction.hasError()) {
            System.out.println("Error");
        } else {
            hash = ethSendTransaction.getTransactionHash();
        }
        return hash;
    }

    public String GetLastBlock() throws Exception {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        BigInteger blockNum = null;
        try {
             blockNum = web3j.ethBlockNumber().send().getBlockNumber();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return blockNum.toString();
    }

    public void GetTranactionByBlock(BigInteger blockNumber) throws IOException {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        EthTransaction ethTransaction = null;
        try {
            ethTransaction = web3j.ethGetTransactionByBlockNumberAndIndex(DefaultBlockParameterName.LATEST, blockNumber).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(ethTransaction.getRawResponse());
    }

    public void GetTranactionByHash(String hash) {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        EthTransaction ethTransaction = null;
        try {
            ethTransaction = web3j.ethGetTransactionByHash(hash).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(ethTransaction.getRawResponse());
    }

    public void GetTranactionCountByBlock() {
        Web3j web3j = Web3j.build(new HttpService(PRO_URL, true));
        EthGetBlockTransactionCountByNumber ethGetBlockTransactionCountByNumber = null;
        try {
            ethGetBlockTransactionCountByNumber = web3j.ethGetBlockTransactionCountByNumber(DefaultBlockParameterName.LATEST).send();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(ethGetBlockTransactionCountByNumber.getRawResponse());
    }
}
