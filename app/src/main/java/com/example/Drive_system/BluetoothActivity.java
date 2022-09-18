package com.example.Drive_system;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.Drive_system.connect.Constant;
import com.example.Drive_system.connect.Data_process;
import com.example.drivesystemcontroller.R;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 0;
    private final BlueToothController mController = new BlueToothController();
    private final Handler mUIHandler = new MyHandler();

    private ListView mListView;
    private DeviceAdapter mAdapter;
    private Toast mToast;

    private final List<BluetoothDevice> mDeviceList = new ArrayList<>();
    private List<BluetoothDevice> mBondedDeviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        initUI();
        registerBluetoothReceiver();
        mController.turnOnBlueTooth(this,REQUEST_CODE);//软件运行时直接申请打开蓝牙
    }

    private void registerBluetoothReceiver(){
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);//开始查找
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED); //结束查找
        filter.addAction(BluetoothDevice.ACTION_FOUND); //查找设备
        filter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);     //绑定状态
        registerReceiver(receiver, filter);//注册这个广播
    }

    /**蓝牙状态改变提示*/
    private final BroadcastReceiver state_receiver = new BroadcastReceiver() {     /**收听一个蓝牙状态的广播**/
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,-1);
            switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    showToast("STATE_OFF");
                    break;
                case BluetoothAdapter.STATE_ON:
                    showToast("STATE_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
                    showToast("STATE_TURNING_ON");
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    showToast("STATE_TURNING_OFF");
                    break;
            }
        }
    };

    /**注册广播监听搜索结果*/
    private final BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)){                           /*开始查找设备*/
                //初始化数据列表
                mDeviceList.clear();
                mAdapter.notifyDataSetChanged();
            } else if(BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);   //取出查找到的设备信息，找到一个添加一个
                mDeviceList.add(device);                                                            //将设备信息添加到list里面
                mAdapter.notifyDataSetChanged();

            } else if(BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {                        //当绑定设备改变
                BluetoothDevice remoteDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);  //获取远程设备
                if(remoteDevice == null) {
                    showToast("无设备");
                    return;
                }
                int status = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, 0);       //获取绑定状态
                if(status == BluetoothDevice.BOND_BONDED) {
                    showToast("已绑定" + remoteDevice.getName());
                } else if(status == BluetoothDevice.BOND_BONDING) {
                    showToast("正在绑定" + remoteDevice.getName());
                } else if(status == BluetoothDevice.BOND_NONE) {
                    showToast("未绑定" + remoteDevice.getName());
                }
            }
        }
    };

    //初始化用户界面
    private void initUI() {
        mListView = findViewById(R.id.device_list);
        mAdapter = new DeviceAdapter(mDeviceList, this);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(bondDeviceClick);
     }

    /**一键连接**/
    public void turn_on_Bluetooth(View view) {
        /**一键连接程序 开始：**/
        mAdapter.refresh(mDeviceList);
        mController.findDevice();
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                int aaa=0;
                 //遍历list列表，查找是否有目标设备，有则直接连接，没有则提示 未连接到指定设备
                for(int i = 0;i < mDeviceList.size(); i ++){
                    if (mDeviceList.get(i).getAddress().equals("98:D3:32:31:36:D1")) { //HC-05地址:98:D3:32:31:36:D1
                        BluetoothDevice device = mDeviceList.get(i);
                        device.createBond();
                        Data_process.getInstance().start(device, mController.getAdapter(),mUIHandler);    //  2021.3.27修改：添加此行
                        break;
                    }
                    aaa++;
                    if (aaa == mDeviceList.size()){
                        showToast("未连接到指定设备，请重试！");
                    }
                }
            }
        }, 1000);//1秒后执行Runnable中的run方法
    }

    /**查找设备*/
    public void find_device(View view) {

        //mDeviceList =mController.getBondedDeviceList();               /////////////////////////////////////////////////////////////////////////////
        mAdapter.refresh(mDeviceList);
        mController.findDevice();
        mListView.setOnItemClickListener(bondDeviceClick);
        showToast("正在查找设备!");
    }

    /**查看已绑定设备*/
    public void checkBoundDevices(View view) {
        mBondedDeviceList = mController.getBondedDeviceList();
        mAdapter.refresh(mBondedDeviceList);
        mListView.setOnItemClickListener(bondedDeviceClick);    //
    }

    public void turn_off_Bluetooth (View view){                 //停止监听测试
        Data_process.getInstance().stop();
        //mController.turnoffBlueTooth();
    }

    /**绑定设备 监听**/
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
    private final AdapterView.OnItemClickListener bondedDeviceClick = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
            BluetoothDevice device = mBondedDeviceList.get(i);
/*            if (mConnectThread != null) {                                                             2021.3.27修改：注释掉
                mConnectThread.cancel();
            }
            mConnectThread = new ConnectThread(device, mController.getAdapter(), mUIHandler);
            mConnectThread.start();*/
            Data_process.getInstance().start(device, mController.getAdapter(),mUIHandler);    //  2021.3.27修改：添加此行
        }
    };

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

    // 转化十六进制编码为字符串
    public static String strTo16(String s) {
        String str = "";
        for (int i = 0; i < s.length(); i++) {
            int ch = (int) s.charAt(i);
            String s4 = Integer.toHexString(ch);
            str = str + s4;
        }
        return str;
    }

    /**设置一个提示框打印工具**/
    private void showToast (String text) {
        if (mToast == null){
            mToast = Toast.makeText(this, text, Toast.LENGTH_SHORT);
        }
        else {
            mToast.setText(text);
        }
        mToast.show();
    }

    /**检查蓝牙是否开启成功**/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            showToast("蓝牙开启成功!");
        }
        else {
            showToast("蓝牙打开失败!");
        }
    }
}