package com.gingernet.bitcoin;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gingernet.utils.CommonTool;
import jdk.nashorn.internal.parser.JSONParser;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;

public class BtcRpc {

    public static Object GetResultJsonObject(String json) throws Exception {
        Object obj = new Object();
        org.json.simple.parser.JSONParser parser = new org.json.simple.parser.JSONParser ();
        obj = parser.parse(json);
        return obj;
    }

    // 获取比特币手续费
    public String GetBitCoinFee() {
        String Url = "https://bitcoinfees.earn.com/api/v1/fees/recommended";
        ObjectMapper MAPPER = new ObjectMapper();
        String bitCoinFee = "";
        try {
            CommonTool commonTool = new CommonTool();
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            Object response = restTemplate.exchange(Url, HttpMethod.GET, entity, Object.class);
            String retArg = MAPPER.writeValueAsString(response);
            Object BalObj = commonTool.GetResultJsonObject(retArg);
            JSONObject jsonObject = (JSONObject) BalObj;
            bitCoinFee = jsonObject.get("body").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitCoinFee;
    }

    // 获取比特币的 utxo
    public String GetBtcUtxo(String address) {
        String BCI_URL = "https://blockchain.info/";
        CommonTool commonTool = new CommonTool();
        ObjectMapper MAPPER = new ObjectMapper();
        String utxoStr = "";
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Arrays.asList(MediaType.APPLICATION_JSON));
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            HttpEntity<String> entity = new HttpEntity<String>("parameters", headers);
            Object response = restTemplate.exchange(BCI_URL + "unspent?active=" + address, HttpMethod.GET, entity, Object.class);
            String retArg = MAPPER.writeValueAsString(response);
            Object BalObj = commonTool.GetResultJsonObject(retArg);
            JSONObject jsonObject = (JSONObject) BalObj;
            String body = jsonObject.get("body").toString();
            Object utxoObj = commonTool.GetResultJsonObject(body);
            JSONObject jsonObjects = (JSONObject) utxoObj;
            JSONArray unspent_outputs = (JSONArray) jsonObjects.get("unspent_outputs");
            utxoStr = unspent_outputs.toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return utxoStr;
    }

    // 通过第三方接口发送交易到 BTC 网络
    public String SendBtcRawTx(String data) throws Exception {
        String txid = "";
        try {
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.99 Safari/537.36");
            headers.setContentType(MediaType.APPLICATION_JSON_UTF8);
            String requestBody = "{\"tx\":\"" + data + "\"}";
            HttpEntity request = new HttpEntity(requestBody, headers);
            ResponseEntity<String> response = restTemplate.exchange("https://api.blockcypher.com/v1/btc/main/txs/push?token=5f4eb6f1fa3d4f6fa519ee93a5a3fb2e", HttpMethod.POST, request, String.class);
            System.out.println(response.getBody());
            String rsp = response.getBody();
            JSONObject jsonObject = (JSONObject) JSONValue.parse(rsp);
            JSONObject txObj = (JSONObject)jsonObject.get("tx");
            txid = txObj.get("hash").toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return txid;
    }

    // BTC 交易记录
    public String GetBtcTxLog(String query) throws Exception {
        String rsp = "";
        RestTemplate restTemplate = new RestTemplate();
        rsp = restTemplate.getForObject(query, String.class);
        Object balObj = GetResultJsonObject(rsp);
        JSONObject jsonObject = (JSONObject) balObj;
        System.out.println("bitcoin" + jsonObject);
        JSONArray txs = (JSONArray)jsonObject.get("txs");
        return txs.toString();
    }
}
