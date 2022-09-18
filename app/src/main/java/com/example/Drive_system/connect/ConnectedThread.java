package com.example.Drive_system.connect;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ConnectedThread extends Thread {
    private final BluetoothSocket mmSocket;
    private final InputStream mmInStream;
    private final OutputStream mmOutStream;
    private final Handler mHandler;

    public ConnectedThread(BluetoothSocket socket, Handler handler) {
        mmSocket = socket;
        InputStream tmpIn = null;
        OutputStream tmpOut = null;
        mHandler = handler;
        // 使用临时对象获取输入和输出流，因为成员流是最终的
        try {
            tmpIn = socket.getInputStream();
            tmpOut = socket.getOutputStream();
        } catch (IOException e) { }

        mmInStream = tmpIn;
        mmOutStream = tmpOut;
    }

    public void run() {

        int count; // 从read()返回count
        // 持续监听InputStream，直到出现异常
        while (true) {
            try {
                try {
                    sleep(200);//单位：毫秒
                } catch (Exception e1) {
                    mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e1));
                }
                count=mmInStream.available();

                StringBuilder s = new StringBuilder();
                StringBuilder crc= new StringBuilder();
                int[] data = new int[count];
                for (int i = 0; i < count; i++) {
                    if (i < (count-2)) {
                        s.append(String.format("%02X", data[i] = mmInStream.read()));
                    }
                    else if (i >= (count-2)) {
                        crc.append(String.format("%02X", data[i] = mmInStream.read()));
                    }
                }
                int sss;
                String CRC = getCRC(s.toString());
                if (count > 0 & crc.toString().equals(CRC)) {

                        if (Constant.MSG_SPEED == 1) {
                            sss = data[3]*256+data[4];
                            Message message = mHandler.obtainMessage(Constant.MSG_GOT_SPEED_DATA, sss);
                            mHandler.sendMessage(message);
                            Constant.MSG_SPEED = 0;
                        }
                        else if (count==9) {
                            if (Constant.MSG_POSITION == 1 ) {
                                Constant.DATA_POSITION = (data[5] * 256 + data[6]) * 65536 + (data[3] * 256 + data[4]);
                                Constant.MSG_POSITION = 0;
                           }
                        }
               }
            } catch (IOException e) {
                mHandler.sendMessage(mHandler.obtainMessage(Constant.MSG_ERROR, e));
                break;
            }
        }
    }

    /**
     * 在main中调用此函数，将数据发送到远端设备中
     */
    public void write(byte[] bytes) {
        try {
            mmOutStream.write(bytes);
        } catch (IOException e) { }
    }

    /**
     * 在main中调用此函数，断开连接
     */
    public void cancel() {
        try {
            mmSocket.close();
        } catch (IOException e) { }
    }

    private static String demToHex(int i) {
        StringBuilder Hex=new StringBuilder();
        String m="0123456789ABCDEF";
        if(i==0) Hex.append(i);
        while (i!=0) {
            Hex.append(m.charAt(i%16));
            i>>=4;
        }
        return Hex.reverse().toString();
    }

    /**
     * 计算CRC16校验码
     *
     * @param data 需要校验的字符串
     * @return 校验码
     */
    public static String getCRC(String data) {
        data = data.replace(" ", "");
        int len = data.length();
        if (!(len % 2 == 0)) {
            return "0000";
        }
        int num = len / 2;
        byte[] para = new byte[num];
        for (int i = 0; i < num; i++) {
            int value = Integer.valueOf(data.substring(i * 2, 2 * (i + 1)), 16);
            para[i] = (byte) value;
        }
        return getCRC2(para);
    }

    /**
     * 计算CRC16校验码
     * @param bytes 字节数组
     * @return {@link String} 校验码
     * @since 1.0
     */
    public static String getCRC2(byte[] bytes) {
        //CRC寄存器全为1
        int CRC = 0x0000ffff;
        //多项式校验值
        int POLYNOMIAL = 0x0000a001;
        int i, j;
        for (i = 0; i < bytes.length; i++) {
            CRC ^= ((int) bytes[i] & 0x000000ff);
            for (j = 0; j < 8; j++) {
                if ((CRC & 0x00000001) != 0) {
                    CRC >>= 1;
                    CRC ^= POLYNOMIAL;
                } else {
                    CRC >>= 1;
                }
            }
        }
        //结果转换为16进制
        String result = Integer.toHexString(CRC).toUpperCase();
        if (result.length() != 4) {
            StringBuilder sb = new StringBuilder("0000");
            result = sb.replace(4 - result.length(), 4, result).toString();
        }
        //交换高低位
        return result.substring(2, 4) + result.substring(0, 2);
    }
}