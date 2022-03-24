package com.example.SmartHelmet;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

public class RegisterRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://ec2-3-37-88-118.ap-northeast-2.compute.amazonaws.com/Register.php";
    private Map<String, String> map;

    public RegisterRequest(String userID, String userPassword, String userName, String userNumberA, String userNumberB, String userNumberC, String DRecordA, String DRecordB, String DRecordC, String DRecordD, String DRecordE, String DRecordF, String DRecordG, String DRecordH, String DRecordI, String DRecordJ, Response.Listener<String> listener) {
        super(Method.POST, URL, listener, null);

        map = new HashMap<>();
        map.put("userID",userID);
        map.put("userPassword", userPassword);
        map.put("userName", userName);
        map.put("userNumberA", userNumberA);
        map.put("userNumberB", userNumberB);
        map.put("userNumberC", userNumberC);
        map.put("DRecordA", DRecordA);
        map.put("DRecordB", DRecordB);
        map.put("DRecordC", DRecordC);
        map.put("DRecordD", DRecordD);
        map.put("DRecordE", DRecordE);
        map.put("DRecordF", DRecordF);
        map.put("DRecordG", DRecordG);
        map.put("DRecordH", DRecordH);
        map.put("DRecordI", DRecordI);
        map.put("DRecordJ", DRecordJ);
    }

    @Override
    protected Map<String, String> getParams() throws AuthFailureError {
        return map;
    }
}