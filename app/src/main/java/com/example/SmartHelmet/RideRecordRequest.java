package com.example.SmartHelmet;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import java.util.HashMap;
import java.util.Map;

public class RideRecordRequest extends StringRequest {

    // 서버 URL 설정 ( PHP 파일 연동 )
    final static private String URL = "http://ec2-3-37-88-118.ap-northeast-2.compute.amazonaws.com/Record.php";
    private Map<String, String> map;

    public RideRecordRequest(String DRecordA, String DRecordB, String DRecordC, String DRecordD, String DRecordE, String DRecordF, String DRecordG, String DRecordH, String DRecordI, String DRecordJ, String userID, Response.Listener<String> listener) {
        super(Request.Method.POST, URL, listener, null);

        map = new HashMap<>();
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
        map.put("userID",userID);
    }

    @Override
    public Map<String, String> getParams()  {
        return map;
    }
}