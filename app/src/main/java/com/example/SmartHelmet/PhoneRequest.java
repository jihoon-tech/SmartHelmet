package com.example.SmartHelmet;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.Map;

public class PhoneRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://ec2-3-37-88-118.ap-northeast-2.compute.amazonaws.com/Phone.php";
    private Map<String, String> map;


    public PhoneRequest(String userNumberA, String userNumberB, String userNumberC, String userID, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("userNumberA", userNumberA);
        map.put("userNumberB", userNumberB);
        map.put("userNumberC", userNumberC);
        map.put("userID",userID);
    }

    @Override
    public Map<String, String> getParams()  {
        return map;
    }
}