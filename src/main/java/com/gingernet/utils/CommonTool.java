package com.gingernet.utils;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.Map;

public class CommonTool {
    public static String getRqstUrl(String url, Map<String, String> params) {
        StringBuilder builder = new StringBuilder(url);
        boolean isFirst = true;
        for (String key : params.keySet()) {
            if (key != null && params.get(key) != null) {
                if (isFirst) {
                    isFirst = false;
                    builder.append("?");
                } else {
                    builder.append("&");
                }
                builder.append(key)
                        .append("=")
                        .append(params.get(key));
            }
        }
        return builder.toString();
    }

    public static Object GetResultJsonObject(String json) throws Exception {
        Object obj = new Object();
        JSONParser parser = new JSONParser();
        try {
            obj = parser.parse(json);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return obj;
    }

}
