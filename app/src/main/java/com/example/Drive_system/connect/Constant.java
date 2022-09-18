package com.example.Drive_system.connect;

import java.util.ArrayList;

/* 给定状态参数常量
 */
public class Constant {
    public static final String CONNECTTION_UUID = "00001101-0000-1000-8000-00805F9B34FB";

    /**
     * 开始监听
     */
    public static final int MSG_START_LISTENING = 1;

    /**
     * 结束监听
     */
    public static final int MSG_FINISH_LISTENING = 2;

    /**
     * 有客户端连接
     */
    public static final int MSG_GOT_A_CLINET = 3;

    /**
     * 连接到服务器
     */
    public static final int MSG_CONNECTED_TO_SERVER = 4;

    /**
     * 获取到数据
     */
    public static final int MSG_GOT_DATA = 5;

    /**
     * 获取到速度数据
     */
    public static final int MSG_GOT_SPEED_DATA = 6;

    /**
     * 获取到位置数据
     */
      public static int MARKED_POSITION=0;//标记过的叶片绝对位置（脉冲数）

    /**
     * 读取速度标志位
     */

    public static int MSG_SPEED=0;

    /**
     * 读取位置标志位
     */
    public static int MSG_POSITION=0;
    public static int Read_Positon_success=0;//位置读取成功
    /**
     * 读取速度值
     */
    public static int Data_Speed = 0;

    /**
     * 读取的位置值
     */
    public static int DATA_POSITION = 0;//当前绝对位置（脉冲数）
    public static int DATA_Zero = 0;//1号叶片的绝对位置（脉冲数）

    /**
     * 当前发动机型号、发动机部件、部件级数、叶片号
     */
    public static int DataId = 0;//数据库标记数据数量
    public static String MAK_ENGINE_TYPE = "";
    public static String MAK_ENGINE_PART = "";
    public static String MAK_ENGINE_STAGE = "";
    public static int MAK_NUMBER_BLADE = 0;//标记的叶片编号
    public static double Ratio = 0.0;
    public static ArrayList<Integer> Blade_need_Return=null;//标记过的所有叶片绝对位置（数据库第6列数据构成的数组）

    /**
     * 当前工作状态 一级的叶片数
     */
    public static int Current_BladesOfStage = 0;

    /**
     * 数据库名称
     */
    public static final String DATABASE_NAME = "Mark Results";

    /**
     * 数据库版本号
     */
    public static final int VERSION_CODE = 1;

    /**
     * 数据库表名称
     */
    public static final String TABLE_NAME = "Label_of_bad_blade";

    /**
     * 出错
     */
    public static final int MSG_ERROR = -1;
}
