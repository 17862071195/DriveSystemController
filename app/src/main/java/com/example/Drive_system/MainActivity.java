package com.example.Drive_system;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;

import com.example.Drive_system.connect.Constant;
import com.example.Drive_system.connect.Data_process;
import com.example.drivesystemcontroller.R;

public class MainActivity extends AppCompatActivity {           /*创建一个自己的activity，extends AppCompatActivity:必须继承系统的activity*/

    Button Confirm;
    int a,b,c;

    @Override
    protected void onCreate(Bundle savedInstanceState) {        /*创建布局资源文件*/
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);                 /*设置这个activity下的布局资源文件为activity_main。括号内要填一个layout文件的ID*/

        Spinner spinnerEngine = findViewById(R.id.engine);
        spinnerEngine.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                a = (int) parent.getItemIdAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spinnerPart = findViewById(R.id.parts);
        spinnerPart.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                b = (int) parent.getItemIdAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Spinner spinnerStages = findViewById(R.id.stages);
        spinnerStages.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                c = (int) parent.getItemIdAtPosition(position);
            }
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        Confirm = findViewById(R.id.button_next);
        Confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            MyData myData = new MyData(getApplicationContext());
            String [] EngineTransRatio = getResources().getStringArray(R.array.engine_trans_ratio);
            int [] NumBladeArray = getResources().getIntArray(R.array.NumBlade);
            String [] EngineModelArray = getResources().getStringArray(R.array.engine_mode);
            String [] EnginePartsArray = getResources().getStringArray(R.array.engine_parts);
            String [] EngineStagesArray = getResources().getStringArray(R.array.engine_stages);

            Constant.MAK_ENGINE_TYPE = EngineModelArray[a];
            Constant.MAK_ENGINE_PART = EnginePartsArray[b];
            Constant.MAK_ENGINE_STAGE = EngineStagesArray[c];
            Constant.Current_BladesOfStage = NumBladeArray[((a+1)*(b+1)*(c+1)-1)];
            Constant.Ratio = 50.0/(Float.parseFloat(EngineTransRatio[a]));

            myData.str = EngineModelArray[a];
            myData.SaveString("EngineModel");
            myData.str = EnginePartsArray[b];
            myData.SaveString("Parts");
            myData.str = EngineStagesArray[c];
            myData.SaveString("Stages");
            myData.number = NumBladeArray[((a+1)*(b+1)*(c+1)-1)];
            myData.SaveInt("NumBlade");
                Engine_Drive_Activity.WriteSingleRegister("0003","0103",50);             //H3A03 设置成 d0103 为内部位置模式
                Engine_Drive_Activity.WriteSingleRegister("0148","0001",50);             //H3d28 0相对模式 ; 1绝对模式
                Engine_Drive_Activity.WriteSingleRegister("0005","0001",50);             //H3A05 设置成内部脉冲运行  H3A05为1，选择内部参数使能，
                Engine_Drive_Activity.WriteSingleRegister("0004","0000",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
            try {
                Thread.sleep(200);
                Data_process.getInstance().sendhex("01060155"+String.format("%04x", 10000));    //36000为发动机转子转一周
                Thread.sleep(200);
                Data_process.getInstance().sendhex("0106014A"+String.format("%04x", 200));        //P3d30 用来确定行走的速度
            }catch (Exception ignored) {  }

            Intent intent = new Intent();
            intent.setClass(MainActivity.this,MenuActivity.class);
            startActivity(intent);
            }
        });
    }
}