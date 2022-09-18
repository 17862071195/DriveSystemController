package com.example.Drive_system;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.bluetooth.BluetoothDevice;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import com.example.Drive_system.DataBase.Dao;
import com.example.Drive_system.connect.Constant;
import com.example.Drive_system.connect.Data_process;
import com.example.drivesystemcontroller.R;

import java.util.ArrayList;
import java.util.List;

public class Engine_Drive_Activity extends AppCompatActivity {

    private Toast mToast;
    Button Step_Rotation;
    Button R_Step_Rotation;
    TextView textViewSpeed;
    TextView textViewState;
    private final BlueToothController mController = new BlueToothController();
    private final Handler mUIHandler = new MyHandler();

    private final List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private final StringBuilder Make_blade = new StringBuilder();
    private TextView bladeinformation;
    public static MutableLiveData<Integer> mySpeed;
    private AlertDialog alertDialog = null;
    private AlertDialog.Builder builder = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_engine_control);

        textViewSpeed = findViewById(R.id.textSpeedDisplay);
        MyData myData = new MyData(getApplicationContext());

        initUI_EngineControl();

        getNumber().observe(this, new Observer<Integer>() {
            @Override
            public void onChanged(Integer integer) {
                textViewSpeed.setText(String.valueOf(integer));
            }
        });

        Make_blade.append("已标记叶片：").append("\n");
        bladeinformation.setText(Make_blade.toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.engine_drive_activity_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.connectBlueTooth:
                Intent intent1 = new Intent();
                intent1.setClass(Engine_Drive_Activity.this,BluetoothActivity.class);
                //Bundle myBaseData = new Bundle();
//                myBaseData.putString("speedData","");
//                intent.putExtras(myBaseData);
                startActivity(intent1);
                break;
            case R.id.selectEngineModel:
                Intent intent2 = new Intent();
                intent2.setClass(Engine_Drive_Activity.this, MainActivity.class);
                startActivity(intent2);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initUI_EngineControl(){

        textViewState = findViewById(R.id.textStateDisplay);
        textViewState.setText(Constant.MAK_ENGINE_TYPE + " " +Constant.MAK_ENGINE_PART + " " + Constant.MAK_ENGINE_STAGE);

        Button N2_Index = findViewById(R.id.DeviceInit);
        N2_Index.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                N2_Index();
            }
        });

        Button myStartRotation = findViewById(R.id.StartTurning);
        myStartRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                 Start_turning();
            }
        });

        bladeinformation = findViewById(R.id.information_window);

        Button mySuspendRotation = findViewById(R.id.SuspendTurning);
        mySuspendRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Suspend_rotation();
            }
        });

        Button myKeepRotation = findViewById(R.id.KeepTurning);
        myKeepRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Continue_rotation();
            }
        });

        Button myReverseRotation = findViewById(R.id.ButtonReverseRotation);
        myReverseRotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Reverse_rotation();
            }
        });

        Button myIncreaseSpeed = findViewById(R.id.ButtonIncrease);
        myIncreaseSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            mySpeed.setValue(mySpeed.getValue()+50);
            if (mySpeed.getValue() >= 1000) {
                mySpeed.setValue(1000);
            }
            WriteSingleRegister("0004","0000",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
            WriteSingleRegister("014A",String.format("%04x", mySpeed.getValue()),250);             //P3d30 用来确定行走的速度
            WriteSingleRegister("0004","0001",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
            WriteSingleRegister("0149","0001",50);             //P3d29 写 1 进行触发
            }
        });

        Button myReduceSpeed = findViewById(R.id.ButtonReduce);
        myReduceSpeed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//            mySpeed -= 1000;
//            if (mySpeed <= 1) {
//                mySpeed = 1;
//            }
            mySpeed.setValue(mySpeed.getValue()-50);
            if (mySpeed.getValue() <= 1) {
                mySpeed.setValue(1);
            }
            WriteSingleRegister("0004","0000",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
            WriteSingleRegister("014A",String.format("%04x", mySpeed.getValue()),250);             //P3d30 用来确定行走的速度
            WriteSingleRegister("0004","0001",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
            WriteSingleRegister("0149","0001",50);             //P3d29 写 1 进行触发
            }
        });

        Step_Rotation = findViewById(R.id.buttonInching);
        Step_Rotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            JOGWork();
            }
        });

        R_Step_Rotation = findViewById(R.id.buttonReverseInching);
        R_Step_Rotation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                JOGReverseWork();            }
        });

        Button mySign = findViewById(R.id.buttonSign);
        mySign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MarkBlade();
            }
        });

        Button mybakeSign = findViewById(R.id.bakeSign);
        mybakeSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setClass(Engine_Drive_Activity.this,DataRecordActivity.class);
                startActivity(intent);
            }
        });

        Button mySpeedButton = findViewById(R.id.setSpeed);
        mySpeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog=null;
                builder=new AlertDialog.Builder(Engine_Drive_Activity.this);
                builder.setTitle("请点击输入速度值");
                final EditText editText = new EditText(Engine_Drive_Activity.this);
                builder.setView(editText);
                builder.setCancelable(false);
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {mySpeed.setValue(Integer.parseInt(editText.getText().toString()));
                            WriteSingleRegister("0004","0000",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
                            WriteSingleRegister("014A",String.format("%04x", mySpeed.getValue()),250);             //P3d30 用来确定行走的速度
                            WriteSingleRegister("0004","0001",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
                            WriteSingleRegister("0149","0001",50);             //P3d29 写 1 进行触发
                        }
                        catch (Exception e1) { showToast("设置失败，请输入数字！"); }
                    }
                });
                alertDialog=builder.create();
                alertDialog.show();
            }
        });
    }

    /**
     * textView数据监控
     */
    public static MutableLiveData<Integer> getNumber() {
        if (mySpeed == null) {
            mySpeed = new MutableLiveData<>();
            mySpeed.setValue(0);
        }
        return mySpeed;
    }

    /**HanDuMing       2021.11.8
     * 驱动器参数初始化
     */
    private void N2_Index(){
        Read_Position();
        if (Constant.Read_Positon_success==0) {
            showToast("位置读取失败，请重试");
        }else{
        Constant.DATA_Zero=Constant.DATA_POSITION;
        //        WriteSingleRegister("014A",String.format("%04x", mySpeed),250);             //H3d30 用来确定行走的速度      ???
        }
    }

    //正向转动到固定脉冲位置
    private void Start_turning(){
        Read_Position();
        if (Constant.Read_Positon_success==0) {
            showToast("位置读取失败，请重试");
        }else{
        int a=Constant.DATA_POSITION+(int)(Constant.Ratio*10000);
        WritePulse(a);
        Continue_rotation();
        }
    }

    private  void Reverse_rotation(){
        Read_Position();
        if (Constant.Read_Positon_success==0) {
           showToast("位置读取失败，请重试");
        }else{
        int a=Constant.DATA_POSITION-(int)(Constant.Ratio*10000);
        WritePulse(a);
        Continue_rotation();
        }
    }

    /* H3E12 通讯方式下内部位置 暂停转动
     */
    private static void Suspend_rotation(){
       WriteSingleRegister("0004","0000",50);             //H3A04 内部参数是否使能，1：使能；0：去使能
       WriteSingleRegister("019C","0013",50);//H3E12 低位(功能值）置13H：对功能19进行操作；高位置0：功能19(内部位置暂停)无效
    }

    /*
     * 通讯方式下内部位置 继续转动
     */
    private static void Continue_rotation(){
        WriteSingleRegister("0004", "0001", 50);             //H3A04 内部参数是否使能，1：使能；0：去使能
        WriteSingleRegister("019C","0113",50);  //P3E12 低位(功能值）置13H：对功能19进行操作；高位置1：功能19(内部位置暂停)有效
        WriteSingleRegister("0149","0001",50); //触发位置指令
    }

    /**
     * 通讯方式下内部位置 继续反转
     */

    private void JOGWork(){
        Read_Position();
        if (Constant.Read_Positon_success==0) {
           showToast("点动失败，请重试");
        }else {
            Constant.MSG_POSITION=0;
            int a = Constant.DATA_POSITION+(int)(Constant.Ratio*1000);
            WritePulse(a);

            Continue_rotation();
        }
    }

    private void JOGReverseWork(){
        Read_Position();
        if (Constant.Read_Positon_success==0) {
            showToast("反向点动失败，请重试");
        }else {
            Constant.MSG_POSITION=0;
            int a = Constant.DATA_POSITION-(int)(Constant.Ratio*1000);
            WritePulse(a);
            Continue_rotation();
        }
    }

    private void MarkBlade() {
        Read_Position();
        if (Constant.Read_Positon_success==0) {
            showToast("标记失败，请检查蓝牙连接后重试！");
        }else {
            if((Constant.DATA_POSITION-Constant.DATA_Zero)>=0) {
                Constant.MAK_NUMBER_BLADE = (int) (((Constant.DATA_POSITION - Constant.DATA_Zero) % (Constant.Ratio * 10000)) / (Constant.Ratio * 10000) * 60);//整圈取余除以一圈的脉冲数，再乘以60等于叶片数
            }else{
                Constant.MAK_NUMBER_BLADE = (int) (((Constant.DATA_POSITION - Constant.DATA_Zero) % (Constant.Ratio * 10000)) / (Constant.Ratio * 10000) * 60)+60;
            }
            Dao dao = new Dao(getApplicationContext());
            Cursor cursor =  dao.readAllData();
            if (cursor.getCount() == 0) {
                showToast("没有数据!");
            }else {
                Constant.DataId=cursor.getCount();
                dao.insert();
                Make_blade.append(Constant.MAK_NUMBER_BLADE+"号叶片").append("\n");
                bladeinformation.setText(Make_blade.toString());
                Continue_rotation();
            }
        }
    }

    public static void BackMarkPosition() {
        WriteSingleRegister("0004", "0000", 50);             //H3A04 内部参数是否使能，1：使能；0：去使能
        int a = Constant.MARKED_POSITION;
        WritePulse(a);
        Continue_rotation();
    }

    private static void WritePulse(int a) {
        String c,c1;
        c = String.format("%08x", a);
        c1=c.substring(4,8)+c.substring(0,4);
        try {Thread.sleep(150);}   catch (Exception e1) {  }
        Data_process.getInstance().sendhex("0110015E000204"+c1);  //P3d50 行走的距离
    }

    private static void ReadSpeed() {
        try {Thread.sleep(200);}   catch (Exception e1) {  }
        Constant.MSG_SPEED = 1;
        Data_process.getInstance().sendhex("010303880001");                    //读取伺服电机转速
    }

    /* 向驱动器写单个寄存器
     * @param address  数据起始地址
     * @param content  数据内容
     * @param delay_ms  连续写多条指令，需延时170ms以上
     */
    public static void WriteSingleRegister(String address, String content, int delay_ms){
        String a="0106";
        a=a+address+content;
        try {Thread.sleep(delay_ms);}   catch (Exception e1) {  }               //延时delay_ms毫秒
        Data_process.getInstance().sendhex(a);
    }

    private static void ReadRegister(String address, int bytes, int delay_ms){
        String a="0103";
        a=a+address+String.format("%04x",bytes);
        try {Thread.sleep(delay_ms);}   catch (Exception e1) {  }               //延时delay_ms毫秒
        Data_process.getInstance().sendhex(a);
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

    private static void Read_Position(){
        Constant.MSG_POSITION = 1;
        WriteSingleRegister("0004", "0000", 50);             //H3A04 内部参数是否使能，1：使能；0：去使能
        int i=0;
        while (Constant.MSG_POSITION == 1) {
            ReadRegister("03BC",2,350);//读956寄存器
            i++;
            if (i>3) break;
        }
        if (i>3) {
            Constant.Read_Positon_success=0;
//            showToast("位置读取失败，请重试");
        }else{
        Constant.Read_Positon_success=1;}
    }
    private class MyHandler extends Handler {

        public void handleMessage(Message message) {
//            MyData myData = new MyData(getApplicationContext());
//            TextView myTextview;
            super.handleMessage(message);
            switch (message.what) {
                case Constant.MSG_ERROR:
                    showToast("error:" + message.obj);
                    break;
                case Constant.MSG_CONNECTED_TO_SERVER:
                    showToast("连接到服务端");
                    break;
                case Constant.MSG_GOT_A_CLINET:
                    showToast("找到服务端");
                    break;
            }
        }
    }
    private final AdapterView.OnItemClickListener bondDeviceClick = new AdapterView.OnItemClickListener() {
        @TargetApi(Build.VERSION_CODES.KITKAT)
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mDeviceList.get(i);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                device.createBond();
            }
/*            mConnectThread = new ConnectThread(device, mController.getAdapter(), mUIHandler);         2021.3.27修改：注释掉
            mConnectThread.start();*/
            Data_process.getInstance().start(device, mController.getAdapter(),mUIHandler);    //  2021.3.27修改：添加此行
        }
    };
}

