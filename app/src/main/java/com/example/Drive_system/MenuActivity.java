package com.example.Drive_system;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.Drive_system.connect.Constant;
import com.example.Drive_system.DataBase.DataBaseHelper;
import com.example.drivesystemcontroller.R;

public class MenuActivity extends AppCompatActivity {

    Button btnBluetoothStart;
    Button btnEngineControlStart;
    Button btnModelSelectStart;
    Button btnDataRecordStart;
    Bundle myBaseData = new Bundle();
    private Toast mToast;
    static final String[] LOCATIONGPS = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE};

    private LocationManager lm;
    private static final int BAIDU_READ_PHONE_STATE = 100;//定位权限请求
    private static final int PRIVATE_CODE = 1315;//开启GPS权限

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        showGPSContacts();

        btnBluetoothStart = findViewById(R.id.button_bluetooth_start);
        btnBluetoothStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // /**在这里启动蓝牙控制界面**/
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this,BluetoothActivity.class);
                //Bundle myBaseData = new Bundle();
                myBaseData.putString("speedData","");
                intent.putExtras(myBaseData);
                startActivity(intent);
            }
        });

        btnEngineControlStart = findViewById(R.id.button_engine_control_start);
        btnEngineControlStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Constant.Current_BladesOfStage == 0) {
                    showToast("请先选择发动机型号");
                }else {
                // /**在这里开启驱动控制界面**/
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this, Engine_Drive_Activity.class);
                intent.putExtras(myBaseData);
                startActivity(intent);
                }
            }
        });

        btnModelSelectStart = findViewById(R.id.button_model_select_start);
        btnModelSelectStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // /**在这里启动型号选择界面**/
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this, MainActivity.class);
                startActivity(intent);
            }
        });

        btnDataRecordStart = findViewById(R.id.button_data_record_start);
        btnDataRecordStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // /**在这里启动数据记录界面**/
                Intent intent = new Intent();
                intent.setClass(MenuActivity.this,DataRecordActivity.class);
                startActivity(intent);
            }
        });

        DataBaseHelper helper = new DataBaseHelper(this);
        helper.getWritableDatabase();
    }

    public void showGPSContacts() {
        lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
        boolean ok = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        if (ok) {//开了定位服务
            if (Build.VERSION.SDK_INT >= 23) { //判断是否为android6.0系统版本，如果是，需要动态添加权限
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        != PERMISSION_GRANTED) {// 没有权限，申请权限。
                    ActivityCompat.requestPermissions(this, LOCATIONGPS,
                            BAIDU_READ_PHONE_STATE);
                } else {
                    //getLocation();//getLocation为定位方法
                }
            } else {
                //getLocation();//getLocation为定位方法
            }
        } else {
            Toast.makeText(this, "系统检测到未开启GPS定位服务,请开启", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent();
            intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivityForResult(intent, PRIVATE_CODE);
        }
    }

    private void showToast (String text) {
        if (mToast == null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        else {
            mToast.setText(text);
        }
        mToast.show();
    }
}
