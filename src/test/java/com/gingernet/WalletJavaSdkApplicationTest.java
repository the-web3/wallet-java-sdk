package com.gingernet;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gingernet.api.po.UnSpentUtxo;
import com.gingernet.bitcoin.Address;
import com.gingernet.bitcoin.BtcRpc;
import com.gingernet.bitcoin.TransactionSign;
import com.gingernet.ethereum.EthRpc;
import com.gingernet.filecoin.FileCoinWallet;
import com.gingernet.word.WordList;
import org.bitcoinj.crypto.MnemonicCode;
import org.bitcoinj.crypto.MnemonicException;
import org.bitcoinj.wallet.DeterministicSeed;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;
import org.web3j.protocol.core.DefaultBlockParameter;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = WalletJavaSdkApplication.class,webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@Ignore
public class WalletJavaSdkApplicationTest {
    private Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    private WebApplicationContext context;

    @LocalServerPort
    private int port;

    public static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static final char[] codString = new char[]{'q','w','e','r','t','y','u','i','o','p','a','s','d','f','g','h','j','k','l',
            'z','x','c','v','b','n','m','!','@','#','$','%','&','*'};

    private MockMvc mockMvc;
    private RestTemplate restTemplate = new RestTemplate();

    @Before
    public void setupMockMvc() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    @Test
    public void contextLoads() {
    }

    public static String getRandomCodeString(Integer count){
        Random random = new Random();
        StringBuffer stringBuffer= new StringBuffer();
        for (int i=0;i<count;i++){
            stringBuffer.append(codString[random.nextInt(codString.length)]);
        }
        return stringBuffer.toString();
    }

    public static String getRandomCode(Integer count){
        Random random = new Random();
        String result="";
        for (int i=0;i<count;i++){
            result+=random.nextInt(10);
        }
        return result;
    }

//    @Test
//    public void testGenerateBtcAddress() {
//        Address address = new Address();
//        Map<String, String> btcMap = address.generateBtcAddress();
//        System.out.println(btcMap);
//    }
//
//    @Test
//    public void testCreatWord() throws Exception {
//        WordList wordList = new WordList();
//        List<String> word = wordList.CreateWord();
//        System.out.println(word);
//    }
//
//    @Test
//    public void TestCreateBtcAddress() throws Exception {
//        String wordsList = "one misery space industry hen mistake typical prison plunge yellow disagree arm";
//        Address address = new Address();
//        String addr = address.CreateAddressByWord(wordsList);
//        System.out.println(addr);
//    }
//
//    // 莱特币
//    @Test
//    public void  LtcAddressTest() {
//        Address address = new Address();
//        System.out.println(address.bitcoinS("30"));
//    }
//
//    // 本币
//    @Test
//    public void WoodAddressTest() {
//        Address address = new Address();
//        System.out.println(address.bitcoinS("49"));
//    }
//
//    // 狗狗币
//    @Test
//    public void DogeAddressTest() {
//        Address address = new Address();
//        System.out.println(address.bitcoinS("1e"));
//    }
//
//    // 生成dash(达世币)地址4c
//    @Test
//    public void DashAddressTest() {
//        Address address = new Address();
//        System.out.println(address.bitcoinS("4c"));
//    }
//
//    // 比特黄金
//    @Test
//    public void BtgAddressTest() {
//        Address address = new Address();
//        System.out.println(address.bitcoinS("26"));
//    }
//
//    //萌奈币（Mona）地址生成32
//    @Test
//    public void MonaAddressTest() {
//        Address address = new Address();
//        System.out.println(address.bitcoinS("32"));
//    }
//
//    //量子链(qtum)地址生成3A
//    @Test
//    public void QtumAddressTest() {
//        Address address = new Address();
//        System.out.println(address.bitcoinS("3A"));
//    }
//
//    // 签名
//    @Test
//    public void signTest() {
//        TransactionSign rawTransaction = new TransactionSign();
//        List<UnSpentUtxo> us = new ArrayList<UnSpentUtxo>();
//        UnSpentUtxo u = new UnSpentUtxo();
//        u.setAddress("mifiHFYFPk5cri4oneXVsRZJZKovvdDcjo");
//        u.setHash("2bc6ac92468c2b4f1fcd2349822dc4663dfc0705b30131087a20ed8d17de8274");
//        u.setHeight(1413239);
//        u.setScript("76a914a1806613a51a81966779e2fa1537013cf4cd2b1788ac");
//        u.setTxN(1);
//        u.setValue(100000);
//        UnSpentUtxo u1 = new UnSpentUtxo();
//        u1.setAddress("mvEtuEqYPMrLaKjJ5nTZ57vQAoYUtVmMaQ");
//        u1.setHash("1893b6ff8ef2bd6f5d652937ffbaed5bb669c5d9ab450066253d6692f2d4d972");
//        u1.setHeight(1413334);
//        u1.setScript("76a914a1806613a51a81966779e2fa1537013cf4cd2b1788ac");
//        u1.setTxN(1);
//        u1.setValue(400000);
//        us.add(u);
//        us.add(u1);
//        System.out.println(JSON.toJSONString(us));
//        String c = rawTransaction.SignTransaction("cNRE3D1pbPPvGs9wpZd3X9NuLsuUQPzPa7ktQyF1nhqBabraocU9", "mifiHFYFPk5cri4oneXVsRZJZKovvdDcjo", "mvEtuEqYPMrLaKjJ5nTZ57vQAoYUtVmMaQ", 400000, 10000, us);
//        System.out.println(c);
//    }
//
//    @Test
//    public void testCreateEthAddress() {
//        com.gingernet.ethereum.Address address = new com.gingernet.ethereum.Address();
//        Map<String, String> addrMap = address.CreateEthAddress();
//        // 私钥：f3de9b05376ffa7623362b0da701b3c4fd2ecb56653eef754c9f43d02420e78e
//        // 地址：0x527d62278e813e43d15f52bbb09bc2adb1c10357
//        System.out.println(addrMap);
//    }
//
//    @Test
//    public void testCreateEthAddressByWord() throws Exception {
//        Map<String, String> ethTestMap = new HashMap<>();
//        com.gingernet.ethereum.Address address = new com.gingernet.ethereum.Address();
//        SecureRandom secureRandom = new SecureRandom();
//        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
//        secureRandom.nextBytes(entropy);
//        List<String> str = MnemonicCode.INSTANCE.toMnemonic(entropy);
//
//        ethTestMap = address.CreateEthAddressByMneminic(str);
//        System.out.println("助记词：");
//        System.out.println(str);
//        System.out.println("助记词生成地址：");
//        System.out.println(ethTestMap);
//    }
//
//    @Test
//    public void testGetEthTxCount() {
//        EthRpc ethRpc = new EthRpc();
//        String nonce = ethRpc.GetEthTxCount("0xdcdb78957fd7b3338c0122314f36dd34e1a025b2");
//        System.out.println(nonce);
//    }
//
//    @Test
//    public void testGetEthTxFee() throws Exception {
//        EthRpc ethRpc = new EthRpc();
//        String txFee = ethRpc.GetEthTxFee();
//        System.out.println(txFee);
//    }
//
//
//    // ETH
//    @Test
//    public void testSignTx() throws Exception {
//        com.gingernet.ethereum.TransactionSign transactionSign = new com.gingernet.ethereum.TransactionSign();
//        BigInteger nonce = new BigInteger("3");
//        BigInteger gasPrice = new BigInteger("1000000000");
//        BigInteger gasLimit = new BigInteger("90000");
//        String to = "0x527d62278e813e43d15f52bbb09bc2adb1c10357";
//        double value = 0.01;
//        // 3d839b297443eae7a95c340f83a819342934619b70fcfda0468f082b365e606d
//        String privateKey = "3d839b297443eae7a95c340f83a819342934619b70fcfda0468f082b365e606d";
//        String hash = transactionSign.SignEthTx(nonce, gasPrice, gasLimit, to, value, privateKey);
//        System.out.println(hash);
//    }
//
//    // ERC20
//    @Test
//    public void testSignErc20Tx() throws Exception{
//        com.gingernet.ethereum.TransactionSign transactionSign = new com.gingernet.ethereum.TransactionSign();
//        String contractAddress = "0x021ffd6f14a8715321493a2b8cb9ced32f8a7619";
//        String to = "0x527d62278e813e43d15f52bbb09bc2adb1c10357";
//        BigInteger nonce = new BigInteger("5");
//        BigInteger gasPrice = new BigInteger("1000000000");
//        BigInteger gasLimit = new BigInteger("90000");
//        BigDecimal amount =  new BigDecimal("9");
//        BigDecimal decimal = new BigDecimal("100000000");
//        String privateKey = "3d839b297443eae7a95c340f83a819342934619b70fcfda0468f082b365e606d";
//        String hash = transactionSign.SignErc20Web3jTx(contractAddress, to, nonce, gasPrice, gasLimit, amount,  decimal, privateKey);
//        System.out.println(hash);
//    }
//
//    @Test
//    public void testSendEthRawTx() throws Exception {
//        EthRpc ethRpc = new EthRpc();
//        String rawTx = "0xf8a905843b9aca0083015f9094021ffd6f14a8715321493a2b8cb9ced32f8a761980b844a9059cbb000000000000000000000000527d62278e813e43d15f52bbb09bc2adb1c10357000000000000000000000000000000000000000000000000000000002faf080025a0ebbbaa00127cd8d240c4797deda5b7c68a89806ecdae11e8d29bfe58992b800aa05a4e40b7632f352e448a3195fb4b6fbb3000ad65d699e7a939f62a680ae750c0";
//        ethRpc.SendEthRawTx(rawTx);
//    }
//
//
//    @Test
//    public void testGetErc20Balance() {
//        EthRpc ethRpc = new EthRpc();
//        String balance = ethRpc.GetErc20Balance("0xdcdb78957fd7b3338c0122314f36dd34e1a025b2", "0x021ffd6f14a8715321493a2b8cb9ced32f8a7619");
//        System.out.println(balance);
//    }
//
//    @Test
//    public void testGetEthBalance() {
//        EthRpc ethRpc = new EthRpc();
//        String ethBalance = ethRpc.GetEthBalance("0xdcdb78957fd7b3338c0122314f36dd34e1a025b2");
//        System.out.println(ethBalance);
//    }
//
//    @Test
//    public void testGetLastBlock() throws Exception{
//        EthRpc ethRpc = new EthRpc();
//        String num = ethRpc.GetLastBlock();
//        System.out.println(num);
//    }
//
//    @Test
//    public void testGetTranactionByBlock() throws IOException {
//        BigInteger aa = new BigInteger("1");
//        EthRpc ethRpc = new EthRpc();
//        ethRpc.GetTranactionByBlock(aa);
//    }
//
//    @Test
//    public void testGetTranactionByHash() throws IOException {
//        EthRpc ethRpc = new EthRpc();
//        ethRpc.GetTranactionByHash("0x3ade472228a54f6ecc080b0d647e2fc174297e7b6a7763d0ea5c75bd31c6334e");
//    }
//
//    @Test
//    public void testGetTranactionCountByBlock() throws Exception {
//        EthRpc ethRpc = new EthRpc();
//        ethRpc.GetTranactionCountByBlock();
//    }
//
//    @Test
//    public void testGetBtcFee() {
//        BtcRpc btcRpc = new BtcRpc();
//        String fee = btcRpc.GetBitCoinFee();
//        System.out.println(fee);
//    }
//
//    @Test
//    public void testGetUtxo() {
//        BtcRpc btcRpc = new BtcRpc();
//        String utxo = btcRpc.GetBtcUtxo("1LBQJDVKakn1CC86v1oSMydZzT6oBnwuLw");
//        System.out.println(utxo);
//    }
//
//    @Test
//    public void testCreateFilAddressByWord() throws Exception {
//        com.gingernet.filecoin.Address address = new com.gingernet.filecoin.Address();
//        SecureRandom secureRandom = new SecureRandom();
//        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
//        secureRandom.nextBytes(entropy);
//        List<String> str = MnemonicCode.INSTANCE.toMnemonic(entropy);
//        String addr = address.getBip44Credentials(str, "1222222", 10);
//        System.out.println("助记词：");
//        System.out.println(str);
//        System.out.println("助记词生成地址：");
//        System.out.println(addr);
//    }

    @Test
    public void testBuidTransaction() throws Exception {
        FileCoinWallet fileCoinWallet = new FileCoinWallet();
        BigDecimal amount = new BigDecimal("1100000");
        BigDecimal mbfee  = new BigDecimal("1100000");
        String sign_str = fileCoinWallet.BuildTransaction("f12h2dnpnwizgtnxyjo3zzrt5v3q7a3uy5b4stbhy",  "f12h2dnpnwizgtnxyjo3zzrt5v3q7a3uy5b4stbhy", amount, mbfee, "");
        SecureRandom secureRandom = new SecureRandom();
        byte[] entropy = new byte[DeterministicSeed.DEFAULT_SEED_ENTROPY_BITS / 8];
        secureRandom.nextBytes(entropy);
        List<String> str = MnemonicCode.INSTANCE.toMnemonic(entropy);
        String sg_data = fileCoinWallet.SignTransaction(sign_str, "f100000000", str, "1222");
        System.out.println(sg_data);
    }

}
