package com.example.SmartHelmet;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.telephony.SmsManager;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;
import com.skt.Tmap.TMapData;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapView;
import org.json.JSONException;
import org.json.JSONObject;
import android.os.CountDownTimer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.SystemClock;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;


public class RideActivity extends AppCompatActivity implements LocationListener, TMapGpsManager.onLocationChangedCallback  {

    private Context mContext = null;
    private boolean m_bTrackingMode = true;
    private TMapData tmapdata = null;
    private TMapGpsManager tmapgps = null;
    private TMapView tmapview = null;
    private static String mApiKey = "l7xx44f2ee3fcba74d1ca37b081103b46a0f";
    private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();
    private ArrayList<String> mArrayMarkerID = new ArrayList<String>();
    private ArrayList<MapPoint> m_mapPoint = new ArrayList<MapPoint>();
    private String address;
    private Double lat = null;
    private Double lon = null;
    private Button btn_sms, mBtnBluetoothOn, mBtnConnect, mBtnSendData, mBtnSendDataoff;
    private LocationManager locationManager;
    private Location mLastlocation = null;
    private TextView tvGetSpeed, tvGpsEnable;
    private double speed;
    private static Toast mToast;
    private int qcount;
    private String recordOnOff;
    private String popupOnOff = "ON";

    //아두이노 연결
    final static int BT_REQUEST_ENABLE = 1;
    final static int BT_MESSAGE_READ = 2;
    final static int BT_CONNECTING_STATUS = 3;
    final static UUID BT_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"); //아두이노 HC-06 전용 UUID

    //블루투스
    BluetoothAdapter mBluetoothAdapter;
    Set<BluetoothDevice> mPairedDevices;
    List<String> mListPairedDevices;
    Handler mBluetoothHandler;
    ConnectedBluetoothThread mThreadConnectedBluetooth;
    BluetoothDevice mBluetoothDevice;
    BluetoothSocket mBluetoothSocket;


    @Override
    public void onLocationChange(Location location) {
        if (m_bTrackingMode) {
            tmapview.setLocationPoint(location.getLongitude(), location.getLatitude());
        }
    }

    //주행기록 리스트
    private Queue<String> Dqueue = new LinkedList<String>();

    //속도표시 + 위치값 호출
    @Override
    public void onLocationChanged(Location location) {
        //시간표시
        long now = System.currentTimeMillis();
        Date mDate = new Date(now);
        SimpleDateFormat simpleDate = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String getTime = simpleDate.format(mDate);
        tvGpsEnable.setText(": " + getTime);

        //위도, 경도로 주소 검색하기
        lat = location.getLatitude();
        lon = location.getLongitude();
        tmapdata.convertGpsToAddress(lat, lon, new TMapData.ConvertGPSToAddressListenerCallback() {
            @Override
            public void onConvertToGPSToAddress(String strAddress) {
                address = strAddress;
            }
        });

        //속도표시
        double getSpeed = Double.parseDouble(String.format("%.3f", location.getSpeed()));
        tvGetSpeed.setText(": " + Math.round(getSpeed * 3.6) + "km");
        String record = String.valueOf(Math.round(getSpeed * 3.6));

        //주행기록 저장이 ON 일때에만
        if(recordOnOff == "ON") {
            qcount += 1;
            Dqueue.add(getTime + " [위치]" + address + " [속도]" + record + "km");
            if (qcount >= 11) {
                Dqueue.remove();
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ride);

        //헬멧 연동 안내 메시지
        AlertDialog.Builder dlg1 = new AlertDialog.Builder(RideActivity.this);
        dlg1.setTitle("<주행모드 알림 메시지>"); //제목
        dlg1.setMessage("SMART HELMET 의 기능을 사용하실려면 먼저 블루투스 연동을 해주시기 바랍니다.");
        //해제 버튼 동작
        dlg1.setNegativeButton("확인", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        dlg1.show();

        // 회원정보 가져오기
        Intent intent = getIntent();
        String userID = intent.getStringExtra("userID");
        String userNumberA = intent.getStringExtra("userNumberA");
        String userNumberB = intent.getStringExtra("userNumberB");
        String userNumberC = intent.getStringExtra("userNumberC");
        String[] userNumbers = {userNumberA, userNumberB, userNumberC};


        btn_sms = findViewById(R.id.btn_sms); //사고알림 문자전송 버튼
        tvGetSpeed = (TextView)findViewById(R.id.tvGetSpeed);
        tvGpsEnable = (TextView)findViewById(R.id.tvGpsEnable);

        //블루투스
        mBtnBluetoothOn = (Button)findViewById(R.id.btnBluetoothOn);
        mBtnConnect = (Button)findViewById(R.id.btnConnect);
        mBtnSendData = (Button)findViewById(R.id.btnSendData);
        mBtnSendDataoff = (Button)findViewById(R.id.btnSendDataoff);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        mBtnBluetoothOn.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                bluetoothOn();
            }
        });

        mBtnConnect.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                listPairedDevices();
            }
        });

        mBtnSendData.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("1"); //헬멧 ON
                    recordOnOff = "ON"; //주행기록 ON
                }
            }
        });

        mBtnSendDataoff.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mThreadConnectedBluetooth != null) {
                    mThreadConnectedBluetooth.write("0"); //헬멧 OFF
                }
                    //메인으로 이동
                    Toast.makeText(getApplicationContext(), "주행모드 종료", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(RideActivity.this, MainActivity.class);
                    intent.putExtra("userID", userID); //RideActivity로 값 넘김
                    intent.putExtra("userNumberA", userNumberA);
                    intent.putExtra("userNumberB", userNumberB);
                    intent.putExtra("userNumberC", userNumberC);
                    startActivity(intent);
                    finish();

            }
        });





        mContext = this;

        //Tmap 각종 객체 선언
        tmapdata = new TMapData(); //POI검색, 경로검색 등의 지도데이터를 관리하는 클래스
        LinearLayout linearLayout = (LinearLayout) findViewById(R.id.mapview);
        tmapview = new TMapView(this);
        linearLayout.addView(tmapview);
        tmapview.setSKTMapApiKey(mApiKey);

        /* 현재 보는 방향 */
        tmapview.setCompassMode(true);

        /* 현위치 아이콘표시 */
        tmapview.setIconVisibility(true);

        /* 줌레벨 */
        tmapview.setZoomLevel(20);

        /* 지도 타입 */
        tmapview.setMapType(TMapView.MAPTYPE_STANDARD);

        /* 언어 설정 */
        tmapview.setLanguage(TMapView.LANGUAGE_KOREAN);


        tmapgps = new TMapGpsManager(RideActivity.this); //단말의 위치탐색을 위한 클래스
        tmapgps.setMinTime(100); //위치변경 인식 최소시간설정
        tmapgps.setMinDistance(1); //위치변경 인식 최소거리설정
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER); //네트워크 기반의 위치탐색
        tmapgps.setProvider(tmapgps.GPS_PROVIDER); //위성기반의 위치탐색
        tmapgps.OpenGps();

        /*  화면중심을 단말의 현재위치로 이동 */
        tmapview.setTrackingMode(true);
        tmapview.setSightVisible(true);


        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);
        Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        // GPS 사용 가능 여부 확인
        boolean isEnable = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);


        //문자전송 버튼 클릭시
        btn_sms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Handler mHandler = new Handler();
                Runnable mRunnable = new Runnable() {

                    @Override
                    public void run() {
                        mHandler.postDelayed(this, 30000);
                        for (int i = 0; i < userNumbers.length; i++) {
                            if (userNumbers[i].startsWith("010")) {
                                //sms전송
                                Bitmap capture = tmapview.getCaptureImage(); //사고발생시 지도 저장
                                String pathofBmp = MediaStore.Images.Media.insertImage(getContentResolver(), capture,"accident", null);//저장

                                SmsManager smsManager = SmsManager.getDefault();//사고발생 메시지 전송
                                smsManager.sendTextMessage(userNumbers[i], null, "[APP 발신] "+ address + "위치에서 사용자의 긴급구조 요청.", null, null);

                            }
                        }
                        Toast.makeText(getApplicationContext(), "전송 완료", Toast.LENGTH_LONG).show();
                        Intent intent = new Intent(RideActivity.this, MainActivity.class);
                        intent.putExtra("userID", userID); //RideActivity로 값 넘김
                        intent.putExtra("userNumberA", userNumberA);
                        intent.putExtra("userNumberB", userNumberB);
                        intent.putExtra("userNumberC", userNumberC);
                        startActivity(intent);
                        mHandler.removeCallbacksAndMessages(null);
                        if(mThreadConnectedBluetooth != null) {
                            mThreadConnectedBluetooth.write("b"); //연동하기
                        }
                    }
                };

                //팝업 카운트다운
                CountDownTimer countDownTimer = new CountDownTimer(30000, 1000) {
                    public void onTick(long millisUntilFinished) {
                        if(mToast != null) mToast.cancel();
                        mToast = Toast.makeText(RideActivity.this, (millisUntilFinished / 1000) +"초 후에 메시지 전송.", Toast.LENGTH_SHORT);
                        mToast.show();
                    }
                    public void onFinish() {
                    }
                };

                //사고 알림 팝업
                AlertDialog.Builder dlg2 = new AlertDialog.Builder(RideActivity.this);
                dlg2.setTitle("<사고 알림 메시지>"); //제목
                dlg2.setMessage("30초 내에 미해제시 사고 알림 메시지를 전송합니다.");
                //해제 버튼 동작
                dlg2.setNegativeButton("해제", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        popupOnOff = "ON"; //팝업 ON
                        recordOnOff = "ON"; //주행기록 ON
                        //토스트 메시지
                        Toast.makeText(getApplicationContext(),"해제되었습니다.",Toast.LENGTH_SHORT).show();
                        mHandler.removeCallbacksAndMessages(null);
                        countDownTimer.cancel();
                    }
                });


                //주행기록 저장시
                Response.Listener<String> responseListener = new Response.Listener<String>() {
                     @Override
                     public void onResponse(String response) {
                        try {
                        JSONObject jsonObject = new JSONObject(response);
                        boolean success = jsonObject.getBoolean("success");
                        if (success) { // 기록저장에 성공한 경우
                            Toast toast = Toast.makeText(RideActivity.this, "주행기록 저장 완료.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.LEFT, 200, 200);
                        } else { // 기록저장에 실패한 경우
                            Toast toast = Toast.makeText(RideActivity.this, "주행기록 저장 실패.", Toast.LENGTH_SHORT);
                            toast.setGravity(Gravity.TOP|Gravity.LEFT, 200, 200);
                            return;
                        }
                        } catch (JSONException e) {
                                e.printStackTrace();
                        }

                    }
                };

                //주행기록 멈추고 저장
                popupOnOff = "OFF";
                recordOnOff = "OFF";
                String DRecordA = Dqueue.poll();
                String DRecordB = Dqueue.poll();
                String DRecordC = Dqueue.poll();
                String DRecordD = Dqueue.poll();
                String DRecordE = Dqueue.poll();
                String DRecordF = Dqueue.poll();
                String DRecordG = Dqueue.poll();
                String DRecordH = Dqueue.poll();
                String DRecordI = Dqueue.poll();
                String DRecordJ = Dqueue.poll();

                dlg2.show();
                countDownTimer.start();
                mHandler.postDelayed(mRunnable, 30000);

                // 서버로 Volley를 이용해서 요청을 함.
                RideRecordRequest rideRecordRequest = new RideRecordRequest(DRecordA, DRecordB, DRecordC, DRecordD, DRecordE, DRecordF, DRecordG, DRecordH, DRecordI, DRecordJ, userID, responseListener);
                RequestQueue queue = Volley.newRequestQueue(RideActivity.this);
                queue.add(rideRecordRequest);
            }
        });



        mBluetoothHandler = new Handler(){
            public void handleMessage(android.os.Message msg){
                if(msg.what == BT_MESSAGE_READ){
                    String readMessage = null;
                    try {
                        readMessage = new String((byte[]) msg.obj, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    } finally {
                        if(readMessage.contains("start")) {
                            if(popupOnOff == "ON"){
                                btn_sms.performClick(); //
                            }
                        }
                    }
                    }

                }
        };

    }
    //뒤로가기 버튼 막기
    @Override
    public void onBackPressed() {
         //super.onBackPressed();
    }



    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderEnabled(String provider) {
        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 위치정보 업데이트
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        //권한 체크
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        // 위치정보 업데이트
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000,0, this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // 위치정보 가져오기 제거
        locationManager.removeUpdates(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없을 경우 최초 권한 요청 또는 사용자에 의한 재요청 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // 권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            }
        }

    }
    ////////////////////////////////////////////////////////////////////////////블루투스
    void bluetoothOn() {
        if(mBluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "블루투스를 지원하지 않는 기기입니다.", Toast.LENGTH_LONG).show();
        }
        else {
            if (mBluetoothAdapter.isEnabled()) {
                Toast.makeText(getApplicationContext(), "블루투스가 이미 활성화 되어 있습니다.", Toast.LENGTH_LONG).show();
            }
            else {
                Toast.makeText(getApplicationContext(), "블루투스가 활성화 되어 있지 않습니다.", Toast.LENGTH_LONG).show();
                Intent intentBluetoothEnable = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(intentBluetoothEnable, BT_REQUEST_ENABLE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case BT_REQUEST_ENABLE:
                if (resultCode == RESULT_OK) { // 블루투스 활성화를 확인을 클릭하였다면
                    Toast.makeText(getApplicationContext(), "블루투스 활성화", Toast.LENGTH_LONG).show();

                } else if (resultCode == RESULT_CANCELED) { // 블루투스 활성화를 취소를 클릭하였다면
                    Toast.makeText(getApplicationContext(), "취소", Toast.LENGTH_LONG).show();

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }
    void listPairedDevices() {
        if (mBluetoothAdapter.isEnabled()) {
            mPairedDevices = mBluetoothAdapter.getBondedDevices();

            if (mPairedDevices.size() > 0) {
                android.support.v7.app.AlertDialog.Builder builder = new android.support.v7.app.AlertDialog.Builder(this);
                builder.setTitle("장치 선택");

                mListPairedDevices = new ArrayList<String>();
                for (BluetoothDevice device : mPairedDevices) {
                    mListPairedDevices.add(device.getName());
                    //mListPairedDevices.add(device.getName() + "\n" + device.getAddress());
                }
                final CharSequence[] items = mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);
                mListPairedDevices.toArray(new CharSequence[mListPairedDevices.size()]);

                builder.setItems(items, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        connectSelectedDevice(items[item].toString());
                    }
                });
                android.support.v7.app.AlertDialog alert = builder.create();
                alert.show();
            } else {
                Toast.makeText(getApplicationContext(), "페어링된 장치가 없습니다.", Toast.LENGTH_LONG).show();
            }
        }
        else {
            Toast.makeText(getApplicationContext(), "블루투스가 비활성화 되어 있습니다.", Toast.LENGTH_SHORT).show();
        }
    }
    void connectSelectedDevice(String selectedDeviceName) {
        for(BluetoothDevice tempDevice : mPairedDevices) {
            if (selectedDeviceName.equals(tempDevice.getName())) {
                mBluetoothDevice = tempDevice;
                break;
            }
        }
        try {
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(BT_UUID);
            mBluetoothSocket.connect();
            mThreadConnectedBluetooth = new ConnectedBluetoothThread(mBluetoothSocket);
            mThreadConnectedBluetooth.start();
            mBluetoothHandler.obtainMessage(BT_CONNECTING_STATUS, 1, -1).sendToTarget();
        } catch (IOException e) {
            Toast.makeText(getApplicationContext(), "블루투스 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
        }
    }

    private class ConnectedBluetoothThread extends Thread {
        private final BluetoothSocket mmSocket;
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedBluetoothThread(BluetoothSocket socket) {
            mmSocket = socket;
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "소켓 연결 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;

            while (true) {
                try {
                    bytes = mmInStream.available();
                    if (bytes != 0) {
                        SystemClock.sleep(100);
                        bytes = mmInStream.available();
                        bytes = mmInStream.read(buffer, 0, bytes);
                        mBluetoothHandler.obtainMessage(BT_MESSAGE_READ, bytes, -1, buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    break;
                }
            }
        }
        public void write(String str) {
            byte[] bytes = str.getBytes();
            try {
                mmOutStream.write(bytes);
            } catch (IOException e) {
                Toast.makeText(getApplicationContext(), "데이터 전송 중 오류가 발생했습니다.", Toast.LENGTH_LONG).show();
            }
        }

    }
}