package com.example.SmartHelmet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;


public class RecordActivity extends AppCompatActivity {

    private TextView id_view, record_view, record_view2, record_view3, record_view4, record_view5, record_view6, record_view7, record_view8, record_view9, record_view10;
    private Button btn_select;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        btn_select = findViewById(R.id.btn_select);
        id_view = findViewById(R.id.id_view);
        record_view = findViewById(R.id.record_view);
        record_view2 = findViewById(R.id.record_view2);
        record_view3 = findViewById(R.id.record_view3);
        record_view4 = findViewById(R.id.record_view4);
        record_view5 = findViewById(R.id.record_view5);
        record_view6 = findViewById(R.id.record_view6);
        record_view7 = findViewById(R.id.record_view7);
        record_view8 = findViewById(R.id.record_view8);
        record_view9 = findViewById(R.id.record_view9);
        record_view10 = findViewById(R.id.record_view10);

        // 회원정보 가져오기
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userPassword = intent.getStringExtra("userPassword");
        String userNumberA = intent.getStringExtra("userNumberA");
        String userNumberB = intent.getStringExtra("userNumberB");
        String userNumberC = intent.getStringExtra("userNumberC");
        id_view.setText(userID); // id 표시


        // 번호설정 버튼 클릭 시 수행
        btn_select.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 조회에 성공한 경우
                                String DRecordA = jsonObject.getString("DRecordA");
                                String DRecordB = jsonObject.getString("DRecordB");
                                String DRecordC = jsonObject.getString("DRecordC");
                                String DRecordD = jsonObject.getString("DRecordD");
                                String DRecordE = jsonObject.getString("DRecordE");
                                String DRecordF = jsonObject.getString("DRecordF");
                                String DRecordG = jsonObject.getString("DRecordG");
                                String DRecordH = jsonObject.getString("DRecordH");
                                String DRecordI = jsonObject.getString("DRecordI");
                                String DRecordJ = jsonObject.getString("DRecordJ");
                                record_view.setText(DRecordA);
                                record_view2.setText(DRecordB);
                                record_view3.setText(DRecordC);
                                record_view4.setText(DRecordD);
                                record_view5.setText(DRecordE);
                                record_view6.setText(DRecordF);
                                record_view7.setText(DRecordG);
                                record_view8.setText(DRecordH);
                                record_view9.setText(DRecordI);
                                record_view10.setText(DRecordJ);
                                Toast.makeText(getApplicationContext(),"조회 완료하였습니다.",Toast.LENGTH_SHORT).show();

                            } else { // 조회에 실패한 경우
                                Toast.makeText(getApplicationContext(), "조회 실패하였습니다.", Toast.LENGTH_SHORT).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                };
                RecordRequest recordRequest = new RecordRequest(userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RecordActivity.this);
                queue.add(recordRequest);

            }
        });

    }
}