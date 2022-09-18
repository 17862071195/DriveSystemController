package com.example.Drive_system.DataBase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

import com.example.Drive_system.connect.Constant;

public class DataBaseHelper extends SQLiteOpenHelper {

    //private Context context;

    /**
     *
     * @ context   上下文
     * @ name      数据库名称
     * @ factory   游标指针
     * @ version   版本号
     */
    public DataBaseHelper(@Nullable Context context) {
        super(context, Constant.DATABASE_NAME, null, Constant.VERSION_CODE);
        //this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        //创建时的回调
//        String sql = "create table " + Constant.TABLE_NAME + "(型号 String, 部件 String, 级数 String, 叶片号 String)";
        String sql = "create table "+Constant.TABLE_NAME+"(DataId integer,Type varchar,Part varchar,Stage varchar,BladeNumber integer,Position integer)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //升级数据库时的回调

    }
}
