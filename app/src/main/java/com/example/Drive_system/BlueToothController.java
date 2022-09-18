package com.example.Drive_system;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;

import java.util.ArrayList;
import java.util.List;

public class BlueToothController {
    private final BluetoothAdapter mAdapter;
    public BlueToothController () {
        mAdapter = BluetoothAdapter.getDefaultAdapter();//获取本地蓝牙接口
    }

    public BluetoothAdapter getAdapter() {
        return mAdapter;
    }

    /* 打开蓝牙
    */
    public void turnOnBlueTooth(Activity activity, int requestCode) {
        Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        activity.startActivityForResult(intent, requestCode);
    }

    /* 查找设备
    */
    public void findDevice() {
        if (mAdapter.isDiscovering()) {                                                             //新加代码：判断蓝牙设备是否正在搜索，如果正在搜索则取消搜索后再搜索
            mAdapter.cancelDiscovery();                                                             //新加代码：判断蓝牙设备是否正在搜索，如果正在搜索则取消搜索后再搜索
        }
        assert (mAdapter !=null);
        mAdapter.startDiscovery();
    }

    /**
     * 获取绑定设备
     * */
    public List <BluetoothDevice> getBondedDeviceList() {
        return new ArrayList<>(mAdapter.getBondedDevices());
    }
}