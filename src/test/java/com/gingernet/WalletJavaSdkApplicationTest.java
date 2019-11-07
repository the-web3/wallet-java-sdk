package com.gingernet;

import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gingernet.api.po.UnSpentUtxo;
import com.gingernet.bitcoin.Address;
import com.gingernet.bitcoin.TransactionSign;
import com.gingernet.word.WordList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.embedded.LocalServerPort;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

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

    @Test
    public void testCreatWord() throws Exception {
        WordList wordList = new WordList();
        List<String> word = wordList.CreateWord();
        System.out.println(word);
    }

    @Test
    public void TestCreateBtcAddress() throws Exception {
        String wordsList = "one misery space industry hen mistake typical prison plunge yellow disagree arm";
        Address address = new Address();
        String addr = address.CreateAddressByWord(wordsList);
        System.out.println(addr);
    }

    // 莱特币
    @Test
    public void  LtcAddressTest() {
        Address address = new Address();
        System.out.println(address.bitcoinS("30"));
    }

    // 本币
    @Test
    public void WoodAddressTest() {
        Address address = new Address();
        System.out.println(address.bitcoinS("49"));
    }

    // 狗狗币
    @Test
    public void DogeAddressTest() {
        Address address = new Address();
        System.out.println(address.bitcoinS("1e"));
    }

    // 生成dash(达世币)地址4c
    @Test
    public void DashAddressTest() {
        Address address = new Address();
        System.out.println(address.bitcoinS("4c"));
    }

    // 比特黄金
    @Test
    public void BtgAddressTest() {
        Address address = new Address();
        System.out.println(address.bitcoinS("26"));
    }

    //萌奈币（Mona）地址生成32
    @Test
    public void MonaAddressTest() {
        Address address = new Address();
        System.out.println(address.bitcoinS("32"));
    }

    //量子链(qtum)地址生成3A
    @Test
    public void QtumAddressTest() {
        Address address = new Address();
        System.out.println(address.bitcoinS("3A"));
    }

    // 签名
    @Test
    public void signTest() {
        TransactionSign rawTransaction = new TransactionSign();
        List<UnSpentUtxo> us = new ArrayList<UnSpentUtxo>();
        UnSpentUtxo u = new UnSpentUtxo();
        u.setAddress("mifiHFYFPk5cri4oneXVsRZJZKovvdDcjo");
        u.setHash("2bc6ac92468c2b4f1fcd2349822dc4663dfc0705b30131087a20ed8d17de8274");
        u.setHeight(1413239);
        u.setScript("76a914a1806613a51a81966779e2fa1537013cf4cd2b1788ac");
        u.setTxN(1);
        u.setValue(100000);
        UnSpentUtxo u1 = new UnSpentUtxo();
        u1.setAddress("mvEtuEqYPMrLaKjJ5nTZ57vQAoYUtVmMaQ");
        u1.setHash("1893b6ff8ef2bd6f5d652937ffbaed5bb669c5d9ab450066253d6692f2d4d972");
        u1.setHeight(1413334);
        u1.setScript("76a914a1806613a51a81966779e2fa1537013cf4cd2b1788ac");
        u1.setTxN(1);
        u1.setValue(400000);
        us.add(u);
        us.add(u1);
        System.out.println(JSON.toJSONString(us));
        String c = rawTransaction.SignTransaction("cNRE3D1pbPPvGs9wpZd3X9NuLsuUQPzPa7ktQyF1nhqBabraocU9", "mifiHFYFPk5cri4oneXVsRZJZKovvdDcjo", "mvEtuEqYPMrLaKjJ5nTZ57vQAoYUtVmMaQ", 400000, 10000, us);
        System.out.println(c);
    }
}
