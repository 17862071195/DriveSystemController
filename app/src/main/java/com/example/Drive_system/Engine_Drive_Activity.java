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

/**********************************************************************************************************************************************************************************************/

//package control;
//        import org.eclipse.swt.widgets.Display;
//        import org.eclipse.swt.widgets.Shell;
//        import org.eclipse.swt.widgets.Text;
//        import org.eclipse.swt.SWT;
//        import org.eclipse.swt.widgets.Button;
//        import org.eclipse.swt.events.SelectionAdapter;
//        import org.eclipse.swt.events.SelectionEvent;
//        import org.eclipse.swt.layout.GridData;
//        import org.eclipse.swt.widgets.Label;
//        import org.eclipse.swt.widgets.Composite;
//        import org.eclipse.swt.widgets.Combo;
//        import java.io.IOException;
//        import java.io.InputStream;
//        import java.io.OutputStream;
//        import java.util.TooManyListenersException;
//        import gnu.io.*;
//        import org.eclipse.swt.events.MouseAdapter;
//        import org.eclipse.swt.events.MouseEvent;
//        import org.eclipse.wb.swt.SWTResourceManager;
//        import org.eclipse.swt.layout.GridLayout;
//        import org.eclipse.swt.layout.FormLayout;
//        import org.eclipse.swt.layout.FormData;
//        import org.eclipse.swt.layout.FormAttachment;
//        import org.eclipse.swt.graphics.Point;
//public class My_project11 implements SerialPortEventListener {
//    protected Shell shell;OutputStream BufferedoutputStream = null;InputStream BufferedinputStream = null;
//    private Text statusLb;Text Defect_B_Status;
//    protected SerialPort serialPort;String t;
//    protected int sendCount=0, reciveCount=0;
//    Display display= Display.getDefault();
//    int a[]=new int[13];//将输出数据数组设置为全局数组
//    int b[]=new int[10];//b[]数组用来存放读取的位置脉冲数
//    int c[]=new int[9];//用来存取标记的压气机叶片在哪一级（总共有9级）
//    int d[]=new int[1];//用来存取标记的涡轮叶片
//    int ha[]=new int[9];//用来存取标记的叶片号
//    int m=0;//返回到标       记还需要走过的脉冲数（不改变原来电机的转动方向）
//    int RS=0,BE=0,DBcount=0,et=0,d_b_num=0,re=0,No1_B=0,x=0,y=0,z=0;
//    int p;//当前位置脉冲值和所要返回的标       记位置脉冲值的差
//    double I=1.014;//I为从电机到发动机高压转子的传动比，通过发动机类型的下拉列表来选择,默认值为cfm56-7b发动机的传动比
//    int k=0;//返回到标       记时需要发送到寄存器Pn121的脉冲值
//    int j=0;//返回到标       记时需要发送到寄存器Pn120的脉冲值
//    int ye=68;//某一级的叶片数,初始值默认为高压压气机第四级叶片数68
//    int st=0;//级数。默认为是第四级
//    int W_data,W_data1,W_data2,key,stc;
//    byte[] readBuffer = new byte[9];
//
//    //Open the window.
//    public void open() {
//        createContents();
//        shell.open();
//        shell.layout();
//        while (!shell.isDisposed()) {
//            if (!display.readAndDispatch()) {
//                display.sleep();
//            }
//        }
//        closeSerialPort();
//        shell.dispose();// 关闭主窗口
//        display.dispose();// 关闭显示swt功能
//    }
//    // Create contents of the window.
//    /**
//     * @wbp.parser.entryPoint
//     */
//    protected void createContents() {
//        shell = new Shell();
//        shell.setModified(true);
//        shell.setBackground(SWTResourceManager.getColor(224, 255, 255));
//        shell.setSize(961, 584);
//        shell.setMinimumSize(new Point(160, 50));
//        shell.setText("                                                             航空发动机转动工具");
//        shell.setLayout(new FormLayout());
//
//        Composite Index_panel = new Composite(shell, SWT.NONE);
//        Index_panel.setForeground(SWTResourceManager.getColor(0, 0, 0));
//        Index_panel.setBackground(SWTResourceManager.getColor(0, 191, 255));
//        Index_panel.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        GridLayout gl_Index_panel = new GridLayout(3, true);
//        gl_Index_panel.marginWidth = 10;
//        gl_Index_panel.verticalSpacing = 20;
//        gl_Index_panel.horizontalSpacing = 15;
//        Index_panel.setLayout(gl_Index_panel);
//        FormData fd_Index_panel = new FormData();
//        fd_Index_panel.left = new FormAttachment(0, 10);
//        Index_panel.setLayoutData(fd_Index_panel);
//        Button Bld1_mark = new Button(Index_panel, SWT.CENTER);
//        Bld1_mark.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Bld1_mark.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Bld1_mark.addSelectionListener(new ButtonClickListener());
//        Bld1_mark.setText("转子定位");
//
//        Button D_Bposition = new Button(Index_panel, SWT.CENTER);
//        D_Bposition.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        D_Bposition.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        D_Bposition.setText("标       记");
//
//        Button clear = new Button(Index_panel, SWT.CENTER);
//        clear.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        clear.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//
//        Label Label = new Label(Index_panel, SWT.NONE);
//        Label.setBackground(SWTResourceManager.getColor(0, 191, 255));
//        Label.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Label.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Label.setText("选择标记");
//
//        Combo returnCombox = new Combo(Index_panel, SWT.NONE);
//        returnCombox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        //returnCombox.setEditable(false);
//        returnCombox.setToolTipText("选择标记");
//        returnCombox.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//
//        clear.addMouseListener(new MouseAdapter() {
//            @Override
//            public void mouseDown(MouseEvent e) {
//                returnCombox.removeAll();
//                DBcount=0;
//            }
//        });
//        clear.setText("清除标记");
//
//        Button Return_M = new Button(Index_panel, SWT.CENTER);
//        Return_M.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Return_M.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Return_M.addSelectionListener(new ButtonClickListener());
//        Return_M.setText("返回标记");
//        Return_M.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                RS=25;
//                re= returnCombox.getSelectionIndex();//用于取出所要返回的标记的位置脉冲值
//                a[3]=71;//要返回到标       记必须先暂停当前运动
//                System.out.println("re="+re);
//                W_data=2044;
//                write();
//            }
//        });
//
//        D_Bposition.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                RS=22;
//                DBcount++;
//                String t=String.valueOf(DBcount);
//                returnCombox.add("标记  "+t);
//                a[3]=123;
//                Read();
//            }
//        });
//
//        Composite control_panel = new Composite(shell, SWT.NONE);
//        control_panel.setBackground(SWTResourceManager.getColor(0, 206, 209));
//        GridLayout gl_control_panel = new GridLayout(1, false);
//        gl_control_panel.verticalSpacing = 30;
//        control_panel.setLayout(gl_control_panel);
//        FormData fd_control_panel = new FormData();
//        fd_control_panel.top = new FormAttachment(0, 10);
//        control_panel.setLayoutData(fd_control_panel);
//
//        Button openPortBtn = new Button(control_panel, SWT.CENTER);
//        openPortBtn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        openPortBtn.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        openPortBtn.addSelectionListener(new ButtonClickListener());
//        openPortBtn.setText("初始化");
//
//        Button Pause = new Button(control_panel, SWT.NONE);
//        Pause.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Pause.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Pause.addSelectionListener(new ButtonClickListener());
//        Pause.setText("暂       停");
//
//        Button Continue = new Button(control_panel, SWT.CENTER);
//        Continue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Continue.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Continue.addSelectionListener(new ButtonClickListener());
//        Continue.setText("继      续");
//
//        Button Stop = new Button(control_panel, SWT.CENTER);
//        Stop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Stop.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Stop.addSelectionListener(new ButtonClickListener());
//        Stop.setText("停      止");
//
//        Composite Engine_Panel = new Composite(shell, SWT.NONE);
//        fd_Index_panel.top = new FormAttachment(0, 309);
//        Engine_Panel.setBackground(SWTResourceManager.getColor(152, 251, 152));
//        FormData fd_Engine_Panel = new FormData();
//        fd_Engine_Panel.bottom = new FormAttachment(Index_panel, -82);
//        fd_Engine_Panel.right = new FormAttachment(Index_panel, 0, SWT.RIGHT);
//        fd_Engine_Panel.top = new FormAttachment(0, 10);
//        fd_Engine_Panel.left = new FormAttachment(0, 10);
//        Engine_Panel.setLayoutData(fd_Engine_Panel);
//        Engine_Panel.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        GridLayout gl_Engine_Panel = new GridLayout(2, false);
//        gl_Engine_Panel.marginWidth = 10;
//        gl_Engine_Panel.horizontalSpacing = 60;
//        gl_Engine_Panel.verticalSpacing = 60;
//        Engine_Panel.setLayout(gl_Engine_Panel);
//
//        Label EngineType_LB = new Label(Engine_Panel, SWT.CENTER);
//        EngineType_LB.setBackground(SWTResourceManager.getColor(152, 251, 152));
//        EngineType_LB.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        EngineType_LB.setText("发动机型号");
//
//        Combo EnginetypeCombox = new Combo(Engine_Panel, SWT.NONE);
//        EnginetypeCombox.setToolTipText("选择发动机型号");
//        EnginetypeCombox.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        EnginetypeCombox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//        EnginetypeCombox.add("CFM56-7B");
//        EnginetypeCombox.add("CFM56-5B");
//        EnginetypeCombox.add("CFM56-3");
//        EnginetypeCombox.select(0);
//        EnginetypeCombox.addSelectionListener(new SelectionAdapter() {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                et=EnginetypeCombox.getSelectionIndex();
//                switch(et){
//                    case 0:I=1.014;break;//（CFM56-7B)
//                    case 1:I=1.2;break;//此处的传动比的值还不确定，先随便设置为1
//                    case 2:I=0.9803;break;//（CFM56-3)
//                }
//            }
//        });
//
//        Label Component_LB = new Label(Engine_Panel, SWT.NONE);
//        Component_LB.setBackground(SWTResourceManager.getColor(152, 251, 152));
//        Component_LB.setAlignment(SWT.CENTER);
//        Component_LB.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Component_LB.setText("发动机部件");
//
//        Combo Component = new Combo(Engine_Panel, SWT.NONE);
//        Component.setToolTipText("");
//        Component.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Component.add("高压压气机");
//        Component.add("高压涡轮");
//        Component.select(0);
//        GridData gd_Component = new GridData(SWT.FILL, SWT.CENTER, true, false);
//        gd_Component.minimumHeight = 20;
//        Component.setLayoutData(gd_Component);
//        Label stageCombox_LB = new Label(Engine_Panel, SWT.CENTER);
//        stageCombox_LB.setBackground(SWTResourceManager.getColor(152, 251, 152));
//        stageCombox_LB.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
//        stageCombox_LB.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        stageCombox_LB.setAlignment(SWT.CENTER);
//        stageCombox_LB.setText("级          数");
//
//        Combo stageCombox = new Combo(Engine_Panel, SWT.NONE);
//        stageCombox.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        stageCombox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
//        Component.addSelectionListener(new SelectionAdapter()
//        {
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                key = Component.getSelectionIndex();
//                switch(key){
//                    case 0:
//                    {stageCombox.removeAll();
//                        for (int i=1;i<10;i++){
//                            stageCombox.add("高压压气机第"+i+"级");}
//                    }break;
//                    case 1:
//                    {stageCombox.removeAll();
//                        for (int i=1;i<3;i++){
//                            stageCombox.add("高压涡轮第"+i+"级");}}break;
//                }
//            }
//        });
//        stageCombox.addSelectionListener(new SelectionAdapter(){
//            @Override
//            public void widgetSelected(SelectionEvent e) {
//                stc=stageCombox.getSelectionIndex();
//                switch(key){
//                    case 0:
//                        switch(stc)
//                        {
//                            case 0:{ye=38;st=1;}break;
//                            case 1:{ye=53;st=2;}break;
//                            case 2:{ye=60;st=3;}break;
//                            case 3:{ye=68;st=4;}break;
//                            case 4:{ye=75;st=5;}break;//第5级高压压气机叶片数
//                            case 5:{ye=82;st=6;}break;
//                            case 6:{ye=82;st=7;}break;
//                            case 7:{ye=80;st=8;}break;
//                            case 8:{ye=76;st=9;}break;
//                        }break;
//                    case 1: {
//                        switch(stc){
//                            case 0:{ye=80;st=1;}break;
//                            case 1:{ye=76;st=2;}break;
//                        }break;
//                    }
//                }
//            }
//        });
//
//        Composite Start_Panel = new Composite(shell, SWT.NONE);
//        fd_control_panel.right = new FormAttachment(Start_Panel, -94);
//        Start_Panel.setBackground(SWTResourceManager.getColor(64, 224, 208));
//        GridLayout gl_Start_Panel = new GridLayout(2, false);
//        gl_Start_Panel.verticalSpacing = 30;
//        gl_Start_Panel.horizontalSpacing = 40;
//        Start_Panel.setLayout(gl_Start_Panel);
//        FormData fd_Start_Panel = new FormData();
//        fd_Start_Panel.top = new FormAttachment(0, 10);
//        fd_Start_Panel.right = new FormAttachment(100, -10);
//        Start_Panel.setLayoutData(fd_Start_Panel);
//
//        Button Forward = new Button(Start_Panel, SWT.CENTER);
//        Forward.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Forward.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Forward.addSelectionListener(new ButtonClickListener());
//        Forward.setText("开始转动");
//
//        Button C_Constant_turn = new Button(Start_Panel, SWT.CENTER);
//        C_Constant_turn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        C_Constant_turn.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        C_Constant_turn.addSelectionListener(new ButtonClickListener());
//        C_Constant_turn.setText("连续转动");
//
//        Button Reverse = new Button(Start_Panel, SWT.CENTER);
//        Reverse.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Reverse.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Reverse.addSelectionListener(new ButtonClickListener());
//        Reverse.setText("反向转动");
//
//        Button C_C_Constant_turn = new Button(Start_Panel, SWT.CENTER);
//        C_C_Constant_turn.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        C_C_Constant_turn.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        C_C_Constant_turn.addSelectionListener(new ButtonClickListener());
//        C_C_Constant_turn.setText("连续反转");
//
//        Button Forward_step = new Button(Start_Panel, SWT.CENTER);
//        Forward_step.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Forward_step.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Forward_step.addSelectionListener(new ButtonClickListener());
//        Forward_step.setText("点       动");
//
//        Button Reversestep = new Button(Start_Panel, SWT.NONE);
//        Reversestep.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Reversestep.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Reversestep.addSelectionListener(new ButtonClickListener());
//        Reversestep.setText("反向点动");
//
//        Button Acceleration = new Button(Start_Panel, SWT.CENTER);
//        Acceleration.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Acceleration.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Acceleration.addSelectionListener(new ButtonClickListener());
//        Acceleration.setText("加       速");
//
//        Button btnNewButton = new Button(shell, SWT.CENTER);
//        btnNewButton.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        FormData fd_btnNewButton = new FormData();
//        fd_btnNewButton.top = new FormAttachment(Start_Panel, 110);
//        fd_btnNewButton.right = new FormAttachment(100, -10);
//        fd_btnNewButton.left = new FormAttachment(0, 806);
//        btnNewButton.setLayoutData(fd_btnNewButton);
//        btnNewButton.addSelectionListener(new ButtonClickListener());
//        btnNewButton.setText("配置参数写入");
//
//        Button Deceleration = new Button(Start_Panel, SWT.NONE);
//        Deceleration.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
//        Deceleration.setFont(SWTResourceManager.getFont("Microsoft YaHei UI", 12, SWT.NORMAL));
//        Deceleration.addSelectionListener(new ButtonClickListener());
//        Deceleration.setText("减       速");
//
//        Composite composite = new Composite(shell, SWT.NONE);
//        composite.setBackground(SWTResourceManager.getColor(0, 191, 255));
//        FormData fd_composite = new FormData();
//        fd_composite.bottom = new FormAttachment(Index_panel, 0, SWT.BOTTOM);
//        fd_composite.top = new FormAttachment(Index_panel, 0, SWT.TOP);
//        fd_composite.left = new FormAttachment(0, 544);
//        fd_composite.right = new FormAttachment(100, -199);
//        composite.setLayoutData(fd_composite);
//
//        Defect_B_Status = new Text(composite, SWT.BORDER);
//        Defect_B_Status.setBounds(10, 10, 182, 30);
//        Defect_B_Status.setBackground(SWTResourceManager.getColor(SWT.COLOR_WHITE));
//
//        statusLb = new Text(composite, SWT.BORDER);
//        statusLb.setBounds(10, 52, 182, 30);
//    }
//    //打开串行端口
//    public void openSerialPort() {
//        CommPortIdentifier portId;
//        // 获取要打开的端口
//        try {
//            portId = CommPortIdentifier.getPortIdentifier(serialPort);
//        } catch (NoSuchPortException e) {return;}
//        // 初始化
//        try {serialPort = (SerialPort) portId.open("my_project", 100);
//        } catch (PortInUseException e) {return;}
//        // 设置端口参数
//        try {serialPort.setSerialPortParams(9600,8,1,1);
//        }catch (UnsupportedCommOperationException e){}
//        // 初始化的IO流管道
//        try {BufferedoutputStream = serialPort.getOutputStream();
//            BufferedinputStream = serialPort.getInputStream();
//        }catch (IOException e){showErrMesgbox(e.getMessage());}
//        // 给端口添加监听器
//        try {serialPort.addEventListener(this);
//        }catch (TooManyListenersException e){showErrMesgbox(e.getMessage());}
//        serialPort.notifyOnDataAvailable(true);
//    }
//    // 给串行端口发送数据
//    public void sendDataToSeriaPort() {
//        try {  sendCount++;
//            for(int i=0;i<13;i++)
//            { BufferedoutputStream.write(a[i]);
//                BufferedoutputStream.flush();}
//            try
//            {Thread.sleep(10);}//毫秒
//            catch(Exception e){}
//        }catch (IOException e){showErrMesgbox(e.getMessage());}
//        Display display= Display.getDefault();
//        display.syncExec(new Runnable() {
//            @Override
//            public void run() {
//                statusLb.setText("  发送: " + sendCount + "                接收: " + reciveCount);
//            }
//        });
//    }
//    // 关闭串行端口
//    public void closeSerialPort() {
//        try {
//            if (BufferedoutputStream != null)
//                BufferedoutputStream.close();
//            if (serialPort != null)
//                serialPort.close();
//            serialPort = null;
//        }catch(Exception e){showErrMesgbox(e.getMessage());
//            System.out.println("关闭异常");}
//    }
//    // 显示错误或警告信息
//    public void showErrMesgbox(String msg) {
//        //JOptionPane.showMessageDialog(this, msg);
//    }
//    //crc计算函数，供后面调用计算出高低位的crc在赋值给a[6]和a[7]
//    public void crc6()
//    {
//        int xda, xdapoly, j,k, xdabit ;
//        xda = 0xFFFF ;
//        xdapoly = 0xA001 ;
//        for( j=0;j<6;j++)  //利用前面输入的六个字节的数字
//        {xda ^= a[j] ;
//            for(k=0;k<8;k++) {
//                xdabit =xda & 0x01 ;
//                xda >>= 1 ;
//                if( xdabit==1 )
//                    xda ^= xdapoly ;
//            }
//        }
//        a[6] = xda & 0xFF ; //CRC的低位
//        a[7] = xda>>8 ; //CRC的高位
//    }
//    public void crc11()
//    {
//        int xda, xdapoly, j,k, xdabit ;
//        xda = 0xFFFF ;
//        xdapoly = 0xA001 ;
//        for( j=0;j<11;j++)  //利用前面输入的六个字节的数字
//        {xda ^= a[j] ;
//            for(k=0;k<8;k++) {
//                xdabit =xda & 0x01 ;
//                xda >>= 1 ;
//                if( xdabit==1 )
//                    xda ^= xdapoly ;
//            }
//        }
//        a[11] = xda & 0xFF ; //crc6的低位
//        a[12] = xda>>8 ; //crc6的高位
//    }

//    public void write()                                                                              //write函数
//    {
//        a[0]=1;a[1]=6;a[2]=0;
//        if (W_data>=0){
//            a[4]=W_data/256; a[5]=W_data%256;}
//        else if (W_data<0){
//            a[4]=(W_data+65536)/256; a[5]=(W_data+65536)%256;}
//        crc6();
//        sendDataToSeriaPort();}

//    public void Mult_write()
//    {
//        a[0]=1;a[1]=16;a[2]=0;a[4]=0;a[5]=2;a[6]=4;
//        if(W_data1>=0){
//            a[7]=W_data1/256;
//            a[8]=W_data1%256;}
//        else if (W_data1<0){
//            a[7]=(W_data1+65536)/256;
//            a[8]=(W_data1+65536)%256;}
//        if(W_data2>=0){
//            a[9]=W_data2/256;
//            a[10]=W_data2%256;}
//        else if(W_data2<0){
//            a[9]=(W_data2+65536)/256;
//            a[10]=(W_data2+65536)%256;}
//        crc11();
//        sendDataToSeriaPort();}

//    public void pulse_calculation(){
//        if(readBuffer[3]>=0) //从一个寄存器读出来的数据，若高8位为非负数则说明该寄存器中的值为正，若寄存器中高8位为负数则该寄存器中值为负
//        {if(readBuffer[4]<0)
//            x=readBuffer[3]*256+readBuffer[4]+256;
//        else if(readBuffer[4]>=0)
//            x=readBuffer[3]*256+readBuffer[4];}
//        else if(readBuffer[3]<0) //dn11中的值为负的时候
//        {if(readBuffer[4]>=0)
//            x=(readBuffer[3]+1)*256+readBuffer[4]-256;
//        else if(readBuffer[4]<0)
//            x=(readBuffer[3]+1)*256+readBuffer[4];}
//        if(readBuffer[5]>=0) //(dn12中的值)同理由寄存器的高8位判断寄存器 中的值时正还是负，高8位为正则寄存器中的值为正，高8位为负则寄存器中的值为负
//        {if(readBuffer[6]<0)
//            y=readBuffer[5]*256+readBuffer[6]+256;
//        else if(readBuffer[6]>=0)
//            y=readBuffer[5]*256+readBuffer[6];}
//        else if(readBuffer[5]<0)
//        {if(readBuffer[6]>=0)
//            y=(readBuffer[5]+1)*256+readBuffer[6]-256;
//        else if(readBuffer[6]<0)
//            y=(readBuffer[5]+1)*256+readBuffer[6];}
//        z=10000*y+x;//总的脉冲值
//    }

//    public void Read(){a[0]=1;a[1]=3;a[2]=0;a[4]=0;a[5]=2;
//        crc6();
//        sendDataToSeriaPort();}

//    private class ButtonClickListener extends SelectionAdapter {
//        @Override
//        public void widgetSelected(SelectionEvent e)  {
//            Button action=(Button) e.widget;
//            if (action.getText() == "初始化")
//            {openSerialPort();
//                RS=0;
//                a[3]=68;
//                W_data1=32767;
//                W_data2=32767;
//                Mult_write();}
//            if (action.getText() == "开始转动")
//            { RS=7;
//                a[3]=71;//高位7,低位255,先是暂停状态，后面才能触发
//                W_data=2044;
//                write();}
//            if (action.getText() == "反向转动")
//            {
//                RS=6;//写71号寄存器
//                a[3]=71;//高位7,低位255,先是暂停状态，后面才能触发
//                W_data=2044;
//                write();
//            }
//
//            if (action.getText() == "连续转动")
//            { RS=10;
//                a[3]=71;
//                W_data=2044;
//                write();}
//            if (action.getText() == "连续反转")
//            { RS=11;
//                a[3]=71;
//                W_data=2044;
//                write();}
//            if(action.getText() == "停      止")//点击停止按钮的同时写Pn070寄存器不使能电机
//            {
//                RS=16;
//                a[3]=70;
//                W_data1=32703;
//                W_data2=2044;
//                Mult_write();
//                sendCount=0; reciveCount=0;
//                display.syncExec(new Runnable(){
//                    @Override
//                    public void run() {
//                        statusLb.setText("  发送: " + sendCount + "                接收: " + reciveCount);
//                    }
//                });
//            }
//            if(action.getText() == "加       速")//加速，设置的Pn98寄存器的值，
//            { RS=33;
//                a[3]=101;
//                Read();}
//            if(action.getText() == "减       速")//
//            { RS=34;
//                a[3]=101;
//                Read();}
//            if(action.getText() == "反向点动")//正微转，设置的Pn120寄存器的值，将脉冲变为正的，
//            { RS=8;//设计正微转角度为2度
//                a[3]=71;
//                W_data=2044;
//                write();}
//            if(action.getText() == "点       动")//加速，设置的Pn120寄存器的值，将脉冲变为负的，
//            { RS=9;//此处RS的值和开始转动的时候相同，都是利用71号寄存器先发出暂停指令然后触发
//                a[3]=71;
//                W_data=2044;
//                write();}
//            if(action.getText() == "暂       停")//暂停，设置的Pn71寄存器的pstop和ptriger两位
//            {
//                RS=4;//设置为3是为了进入返回函数之后直接终止switch语句
//                a[3]=71;
//                W_data=2044;
//                write();
//            }
//            if(action.getText() == "转子定位")//1号叶片定位定位按钮，也就是读取dn11和dn12中的数据,采用连续读读两个寄存器的方法
//            {RS=19;//1好叶片定位时先暂停
//                a[3]=71;//高位7,低位255,先是暂停状态，后面才能触发
//                W_data=2044;
//                write();}
//            if(action.getText() == "继      续")//点击继续按钮时，发动机开始转动走一圈，此时电机反向点动
//            {  RS=30;
//                a[3]=71;
//                W_data=2044;
//                write();}
//            if (action.getText() == "配置参数写入")
//            {RS=36;//配置参数写入
//                a[3]=81;
//                W_data=0;
//                write();
//                System.out.println("配置参数写入");}
//        }
//    }
//    @Override
//    public void serialEvent(SerialPortEvent event) {
//        switch (event.getEventType()) {
//            case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
//                break;
//            case SerialPortEvent.DATA_AVAILABLE:
//                try {
//                    while (BufferedinputStream.available()>0) {
//                        Thread.sleep(50);//硬件发送是分段的，加一个延时就行了
//                        BufferedinputStream.read(readBuffer);
//                    }
//                    for(int k=0;k<9;k++)
//                    {System.out.print(readBuffer[k]);
//                        if(k==8) {System.out.print('\n');}
//                        else System.out.print(',');}
//                    reciveCount++;
//                }catch (IOException e){showErrMesgbox(e.getMessage());}catch (InterruptedException e){e.printStackTrace();}
//                display.syncExec(new Runnable() {
//                    @Override
//                    public void run() {
//                        statusLb.setText("  发送: " + sendCount + "                接收: " + reciveCount);
//                    }
//                });
//        }
//        RS++;
//        switch(RS)
//        {
//            case 1://打开串口的同时写寄存器Pn002为4（控制模式为位置/转矩模式）
//            { a[3]=2;
//                W_data=4;
//                write();}break;
//            case 2:
//            { a[3]=101;
//                Read();}break;
//            case 3:{
//                a[3]=101;
//                W_data=10*(readBuffer[5]*256+readBuffer[6]);
//                write();}break;
//            case 4: { a[3]=117;
//                W_data=1;
//                write();}break;
//            case 5://确定执行内部位置指令速度值（设置为20转/分钟）
//            { a[3]=128;
//                W_data=20;
//                write();}break;
//            case 6: break;
//            case 7://发动机反向点动一圈电机开始转动。
//            { RS=12;
//                a[3]=120;
//                W_data1=-(int)((700000/I)/10000);
//                W_data2=-(int)((700000/I)%10000);
//                Mult_write();
//            }break;
//
//            case 8://设置发动机顺时针转一圈。
//            { RS=12;
//                a[3]=120;
//                W_data1=(int)(700000/I)/10000;
//                W_data2=(int)((700000/I)%10000);
//                Mult_write();} break;
//            case 9:
//            { RS=12;
//                a[3]=120;
//                W_data1=-(int)(35000/(9*I))/10000;
//                W_data2=-(int)((35000/(9*I))%10000);
//                Mult_write();}break;
//            case 10:
//            { RS=12;
//                a[3]=120;
//                W_data1=(int)(35000/(9*I))/10000;
//                W_data2=(int)((35000/(9*I))%10000);
//                Mult_write();}break;
//            case 11://设置连续正转
//            { RS=12;
//                a[3]=120;
//                W_data1=9999;
//                W_data2=9999;
//                Mult_write();
//            } break;
//            case 12://设置连续反转
//            { a[3]=120;
//                W_data1=-9999;
//                W_data2=-9999;
//                Mult_write();} break;
//            case 13:{
//                a[3]=118;//写完返回所需要写的脉冲指令后还得取消暂停指令触发电机转动
//                W_data=0;
//                write();}break;
//            case 14:{ RS=36;//使能
//                a[3]=70;
//                W_data1=32702;
//                W_data2=3068;//写寄存器选择电子齿轮比为101
//                Mult_write();}break;
//            case 15:break;
//            case 16:break;
//            case 17:closeSerialPort();
//            case 18:break;//点击一次减速按钮也只需要写一个寄存器
//            case 20://1号叶片定位时暂停后读取当前位置脉冲DN11/DN12
//            { a[3]=123;
//                Read();}
//            case 21://1号叶片定位
//            { RS=36;
//                pulse_calculation();
//                No1_B=z;}break;//将1号叶片的位置脉冲值放在  No1_B中
//            case 23:
//            { a[3]=71;
//                W_data=2044;
//                write();}break;
//            case 24://1号叶片定位时暂停后读取当前位置脉冲
//            { a[3]=123;
//                Read();}
//            case 25://标记缺损叶片
//            { pulse_calculation();
//                System.out.println("z="+z);
//                b[DBcount-1]=(z- No1_B)%(int)(700000/I);//以原来方向转动时距1号叶片的位置脉冲数
//                c[DBcount-1]=st;//记录这个标记在哪一级
//                ha[DBcount-1]=(int)(-b[DBcount-1]*I*ye/700000);//当前叶片号
//                display.syncExec(new Runnable() {
//                    @Override
//                    public void run() {
//                        if(key==0)//所选择的为压气机
//                        {Defect_B_Status.setText("高压压气机第"+st+"级"+ha[DBcount-1]+"号叶片");
//                            System.out.println("key1="+key);}
//                        else if(key==1)
//                        {Defect_B_Status.setText("高压涡轮第"+st+"级"+ha[DBcount-1]+"号叶片");
//                            System.out.println(key=1);}
//                    }
//                });} break;
//            case 26:{
//                a[3]=123;//读取当前位置脉冲值,连续读取两个寄存器379和380
//                Read();}break;
//            case 27:
//            { z=(z- No1_B)%(int)(700000/I);//当前位置以原来方向转动时距1号叶片的位置脉冲数(减掉整圈)
//                p=z-b[re];//当前位置脉冲值和所要返回的标       记位置脉冲值的差
//                if(p>0)//发动机始终保持开始转动（即电机反向点动）(因为电机开始转动的时候位置脉冲的值越来越大，反向点动的时候位置脉冲的值越来越小)
//                {m=-p;
//                    j=m/10000+65536;//整万的脉冲
//                    k=m%10000+65536;}//不足整万的脉冲
//                else if(p<0)
//                {m=(int)(-700000/I)-p;
//                    j=m/10000+65536;
//                    k=m%10000+65536;}
//                a[3]=120;
//                W_data1=j;
//                W_data2=k;
//                Mult_write();
//            } break;
//            case 28:{
//                a[3]=118;//写完返回所需要写的脉冲指令后还得取消暂停指令触发电机转动
//                W_data=0;
//                write();}break;
//            case 29:
//            { RS=36;
//                a[3]=71;//写完返回所需要写的脉冲指令后还得取消暂停指令触发电机转动
//                W_data=3068;
//                write();}break;
//            case 31:
//            {	a[3]=118;//写完返回所需要写的脉冲指令后还得取消暂停指令触发电机转动
//                W_data=0;
//                write();}break;
//            case 32:
//            {  a[3]=71;
//                W_data=3068;
//                write();}break;
//            case 33:break;
//
//            case 34:
//            {RS=35;
//                a[3]=101;
//                W_data=readBuffer[3]*256+readBuffer[4]+readBuffer[5]*256+readBuffer[6];
//                write();} break;
//            case 35:
//            {a[3]=101;
//                W_data=readBuffer[3]*256+readBuffer[4]-(readBuffer[5]*256+readBuffer[6]);
//                write();}break;
//            case 36:break;
//            case 37:{
//                a[3]=81;
//                W_data=1;
//                write();}break;
//            case 38:break;
//        }
//    }
//    public static void main(String[] args) {
//        try {
//            My_project11 window = new My_project11();
//            window.open();
//        } catch (Exception e) {e.printStackTrace();}
//    }
//}

