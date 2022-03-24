package com.example.SmartHelmet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import org.json.JSONException;
import org.json.JSONObject;

public class PhoneActivity extends AppCompatActivity {

    private EditText et_numbera, et_numberb, et_numberc;
    private Button btn_update;
    private TextView id_view1, number_view, number_view2, number_view3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone);

        et_numbera = findViewById(R.id.et_numbera);
        et_numberb = findViewById(R.id.et_numberb);
        et_numberc = findViewById(R.id.et_numberc);
        btn_update = findViewById(R.id.btn_update);
        id_view1 = findViewById(R.id.id_view1);
        number_view = findViewById(R.id.record_view);
        number_view2 = findViewById(R.id.record_view2);
        number_view3 = findViewById(R.id.record_view8);
        btn_update = findViewById(R.id.btn_update);

        // 회원정보 가져오기
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userPassword = intent.getStringExtra("userPassword");
        String userNumberA = intent.getStringExtra("userNumberA");
        String userNumberB = intent.getStringExtra("userNumberB");
        String userNumberC = intent.getStringExtra("userNumberC");
        id_view1.setText(userID); // id 표시
        number_view.setText(userNumberA);
        number_view2.setText(userNumberB);
        number_view3.setText(userNumberC);

        // 번호설정 버튼 클릭 시 수행
        btn_update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String userNumberA= et_numbera.getText().toString();
                String userNumberB= et_numberb.getText().toString();
                String userNumberC= et_numberc.getText().toString();

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try {
                            JSONObject jsonObject = new JSONObject(response);
                            boolean success = jsonObject.getBoolean("success");
                            if (success) { // 번호번경에 성공한 경우
                                Toast.makeText(getApplicationContext(),"번호변경 완료하였습니다.",Toast.LENGTH_SHORT).show();
                                Intent intent = new Intent(PhoneActivity.this, MainActivity.class);
                                intent.putExtra("userID", userID);
                                intent.putExtra("userNumberA", userNumberA);
                                intent.putExtra("userNumberB", userNumberB);
                                intent.putExtra("userNumberC", userNumberC);
                                startActivity(intent);
                            } else { // 번호변경에 실패한 경우
                                Toast.makeText(getApplicationContext(), "번호변경 실패하였습니다.", Toast.LENGTH_LONG).show();
                                return;
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                };
                // 서버로 Volley를 이용해서 요청을 함.
                PhoneRequest phoneRequest = new PhoneRequest(userNumberA,userNumberB,userNumberC,userID,responseListener);
                RequestQueue queue = Volley.newRequestQueue(PhoneActivity.this);
                queue.add(phoneRequest);
            }
        });
    }
}