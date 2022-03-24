package com.example.SmartHelmet;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    private Button btn_ride, btn_logout,btn_phone, btn_record;
    private TextView id_view;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btn_ride = findViewById(R.id.btn_ride);
        btn_logout = findViewById(R.id.btn_logout);
        id_view = findViewById(R.id.id_view);
        btn_phone = findViewById(R.id.btn_phone);
        btn_record = findViewById(R.id.btn_record);

        // 회원정보 가져오기
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userNumberA = intent.getStringExtra("userNumberA");
        String userNumberB = intent.getStringExtra("userNumberB");
        String userNumberC = intent.getStringExtra("userNumberC");
        id_view.setText(userID); // id 표시

        // 주행모드 버튼을 클릭 시 수행
        btn_ride.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RideActivity.class);
                intent.putExtra("userID", userID); //RideActivity로 값 넘김
                intent.putExtra("userNumberA", userNumberA);
                intent.putExtra("userNumberB", userNumberB);
                intent.putExtra("userNumberC", userNumberC);
                startActivity(intent);
            }
        });

        // 사고알림 번호 설정 버튼을 클릭 시 수행
        btn_phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, PhoneActivity.class);
                intent.putExtra("userID", userID); //RideActivity로 값 넘김
                intent.putExtra("userNumberA", userNumberA);
                intent.putExtra("userNumberB", userNumberB);
                intent.putExtra("userNumberC", userNumberC);
                startActivity(intent);
            }
        });

        // 사고 주행기록 조회 버튼을 클릭 시 수행
        btn_record.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, RecordActivity.class);
                intent.putExtra("userID", userID); //RideActivity로 값 넘김
                intent.putExtra("userNumberA", userNumberA);
                intent.putExtra("userNumberB", userNumberB);
                intent.putExtra("userNumberC", userNumberC);
                startActivity(intent);
            }
        });

        // 로그아웃 버튼을 클릭 시 수행
        btn_logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    //뒤로가기 버튼 막기
    @Override
    public void onBackPressed() {
        //super.onBackPressed();
    }
}