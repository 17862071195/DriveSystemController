package com.example.Drive_system.connect;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Handler;

public class Data_process {

    private ConnectThread mConnectThread;

    public void start(BluetoothDevice device, BluetoothAdapter adapter, Handler handler) {
        mConnectThread = new ConnectThread(device,adapter,handler);
        mConnectThread.start();
    }

    public void stop() {
        if(mConnectThread != null) {
            mConnectThread.cancel();
        }
    }

    /**
     * 单例的写法，优点可以自己想想
     */
    private static class ControlHolder {
        private static final Data_process mInstance = new Data_process();
    }

    public static Data_process getInstance() {
        return ControlHolder.mInstance;
    }

    /** HnaDuMing     2021.9.13
     * 16进制表示的字符串转换为字节数组
     * @param hexString 16进制表示的字符串
     * @return byte[] 字节数组
     */
    public static byte[] hexStringToByteArray(String hexString) {
        hexString = hexString.replaceAll(" ", "");
        int len = hexString.length();
        byte[] bytes = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            // 两位一组，表示一个字节,把这样表示的16进制字符串，还原成一个字节
            bytes[i / 2] = (byte) ((Character.digit(hexString.charAt(i), 16) << 4) + Character
                    .digit(hexString.charAt(i + 1), 16));
        }
        return bytes;
    }

    /**HanDuMing       2021.9.13
     * 十六进制字符串转换成byte[]并发送
     * @param str
     */
    public void sendhex(String str) {
        str = str + getCRC(str);
        byte[] data = hexStringToByteArray(str);
        if(mConnectThread != null) {
            mConnectThread.sendData(data);
        }
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
        return getCRC(para);
    }

    /**
     * 计算CRC16校验码
     * @param bytes 字节数组
     * @return {@link String} 校验码
     * @since 1.0
     */
    public static String getCRC(byte[] bytes) {
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
            StringBuffer sb = new StringBuffer("0000");
            result = sb.replace(4 - result.length(), 4, result).toString();
        }
        //交换高低位
        return result.substring(2, 4) + result.substring(0, 2);
    }
}