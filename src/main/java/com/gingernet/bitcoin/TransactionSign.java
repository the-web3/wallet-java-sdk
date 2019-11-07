package com.gingernet.bitcoin;

import java.text.Collator;
import java.util.ArrayList;
import java.util.List;

import com.gingernet.api.po.UnSpentUtxo;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.configuration2.Configuration;
import org.bitcoinj.core.Address;
import org.bitcoinj.core.Coin;
import org.bitcoinj.core.Context;
import org.bitcoinj.core.DumpedPrivateKey;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.NetworkParameters;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Transaction;
import org.bitcoinj.core.TransactionOutPoint;
import org.bitcoinj.core.UTXO;
import org.bitcoinj.core.Utils;
import org.bitcoinj.params.MainNetParams;
import org.bitcoinj.params.TestNet3Params;
import org.bitcoinj.script.Script;
import org.omg.CORBA.UNKNOWN;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;

import org.bitcoinj.core.TransactionConfidence;

public class TransactionSign {
    private static Logger LOG = LoggerFactory.getLogger(TransactionSign.class);
    static NetworkParameters params;

    static {
        try {
            params = TestNet3Params.get(); //: MainNetParams.get();
            LOG.info("=== [BTC] bitcoin  client networkID：{} ===", params.getId());
        } catch (Exception e) {
            LOG.info("=== [BTC] com.bscoin.coldwallet.cointype.btc.rawtransaction:{} ===", e.getMessage(), e);
        }
    }

    public String SignTransaction(String privKey, String recevieAddr, String formAddr, long amount, long fee, List<UnSpentUtxo> unUtxos) {

        if(!unUtxos.isEmpty() && null != unUtxos){
            List<UTXO> utxos = new ArrayList<UTXO>();
            DumpedPrivateKey dumpedPrivateKey = DumpedPrivateKey.fromBase58(params, privKey);
            ECKey key = dumpedPrivateKey.getKey();
            // 接收地址
            Address receiveAddress = Address.fromBase58(params, recevieAddr);
            // 构建交易
            Transaction tx = new Transaction(params);
            tx.addOutput(Coin.valueOf(amount), receiveAddress);
            // 如果需要找零 消费列表总金额 - 已经转账的金额 - 手续费
            long value = unUtxos.stream().mapToLong(UnSpentUtxo::getValue).sum();
            Address toAddress = Address.fromBase58(params, formAddr);
            long leave  = value - amount - fee;
            if(leave > 0){
                tx.addOutput(Coin.valueOf(leave), toAddress);
            }
            // utxos is an array of inputs from my wallet
            for (UnSpentUtxo unUtxo : unUtxos) {
                utxos.add(new UTXO(Sha256Hash.wrap(unUtxo.getHash()),
                        unUtxo.getTxN(),
                        Coin.valueOf(unUtxo.getValue()),
                        unUtxo.getHeight(),
                        false,
                        new Script(Utils.HEX.decode(unUtxo.getScript())),
                        unUtxo.getAddress()));
            }
            for (UTXO utxo : utxos) {
                TransactionOutPoint outPoint = new TransactionOutPoint(params, utxo.getIndex(), utxo.getHash());
                tx.addSignedInput(outPoint, utxo.getScript(), key, Transaction.SigHash.ALL, true);
            }
            Context context = new Context(params);
            tx.getConfidence().setSource(TransactionConfidence.Source.NETWORK);
            tx.setPurpose(Transaction.Purpose.USER_PAYMENT);
            LOG.info("Bitcoin Sign Success :{} ===",tx.getHashAsString());
            return new String(Hex.encodeHex(tx.bitcoinSerialize()));
        }
        return null;
    }
}