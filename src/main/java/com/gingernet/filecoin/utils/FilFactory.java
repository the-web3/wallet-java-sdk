package com.gingernet.filecoin.utils;
import cn.hutool.http.HttpRequest;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Component
public class FilFactory {

    private static final BigDecimal FIL_UNIT = BigDecimal.TEN.pow(18);

    //获取nonce
    private final static String NONCE = "Filecoin.MpoolGetNonce";
    //获取余额
    private final static String BALANCE = "Filecoin.WalletBalance";
    //获取交易费
    private final static String VALID_ADDRESS = "Filecoin.WalletValidateAddress";
    //广播
    private final static String PUSH = "Filecoin.MpoolPush";
    //当前区块高度
    private final static String BLOCK_HEIGHT = "Filecoin.ChainHead";


    public BigDecimal getbalance(String address) {
        String[] addr = {address};
        String body = getBody(BALANCE, addr);
        if (body != null) {
            JSONObject jsonBody = JSONObject.parseObject(body);
            if (jsonBody != null) {
                String balance = jsonBody.getString("result");
                if (balance != null) {

                    return (new BigDecimal(balance)).divide(FIL_UNIT, 8, RoundingMode.HALF_DOWN);
                }
            }
        }
        return BigDecimal.ZERO;
    }

    public String checkAddress(String address) {
        String[] addr = {address};
        String body = getBody(VALID_ADDRESS, addr);
        if (body != null) {
            JSONObject jsonBody = JSONObject.parseObject(body);
            return jsonBody != null ? jsonBody.getString("result") : null;
        }
        return null;
    }

    public BigInteger getNonce(String address) {
        String[] addr = {address};
        String body = getBody(NONCE, addr);
        if (body != null) {
            JSONObject jsonBody = JSONObject.parseObject(body);
            if (jsonBody != null) {
                String balance = jsonBody.getString("result");
                if (balance != null) {
                    System.out.println("nonce=" + new BigDecimal(balance).toBigInteger());
                    return new BigDecimal(balance).toBigInteger();
                }
            }
        }
        return BigInteger.ZERO;
    }

    public Integer getBlockHeight() {
        List<JSONObject> jsonObjects = new ArrayList<>();
        System.out.println("height=");
        String body = getBody(BLOCK_HEIGHT, jsonObjects);
        JSONObject jsonBody = JSONObject.parseObject(body);
        if (jsonBody != null) {
            String result = jsonBody.getString("result");
            if (result != null) {
                JSONObject object = JSONObject.parseObject(result);
                if (object != null) {
                    int height = Integer.parseInt(object.getString("Height"));
                    System.out.println("height=" + height);
                    return height;
                }
            }
        }
        return 0;
    }

    public String push(String message) {
        JSONObject jsonObject = JSONObject.parseObject(message);
        List<JSONObject> jsonObjects = new ArrayList<>();
        jsonObjects.add(jsonObject);
        String body = getBody(PUSH, jsonObjects);
        if (body != null) {
            JSONObject jsonBody = JSONObject.parseObject(body);
            if (jsonBody != null) {
                String result = jsonBody.getString("result");
                System.out.println("result=" + result);
                if (result != null) {
                    JSONObject object = JSONObject.parseObject(result);
                    if (object != null) {
                        String cid = object.getString("/");
                        System.out.println("cid=" + cid);
                        return cid;
                    }
                }
            }
        }
        return "";
    }

    public String getBody(String method, List<JSONObject> object) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "2.0");
        jsonObject.put("method", method);
        jsonObject.put("params", object);
        jsonObject.put("id", 1);
        String json = jsonObject.toJSONString();

        HttpRequest httpRequest = HttpRequest.post("").headerMap(getHeaderMap(), true);
        //httpRequest.setHttpProxy("127.0.0.1",7890);
        return httpRequest.body(json).execute().body();
    }

    public String getBody(String method, String[] params) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("jsonrpc", "2.0");
        jsonObject.put("method", method);
        jsonObject.put("params", params);
        jsonObject.put("id", 1);
        String json = jsonObject.toJSONString();
        return HttpRequest.post("").headerMap(getHeaderMap(), true).body(json).execute().body();
    }


    public Map<String, String> getHeaderMap() {
        Map<String, String> headerMap = new HashMap<>();
        headerMap.put("Content-Type", "application/json");
        headerMap.put("charset", "UTF-8");
        headerMap.put("Authorization", "Bearer " + "");
        return headerMap;
    }
}

