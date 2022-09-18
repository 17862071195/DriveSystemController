package com.example.Drive_system.DataBase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.Drive_system.connect.Constant;

public class Dao {
    private static final String TAG = "Dao";
    private  final DataBaseHelper mHelper;
    private final Context thisContext;

    public Dao(Context context) {
        mHelper = new DataBaseHelper(context);
        thisContext = context;
        }

    public void insert() {

        SQLiteDatabase db = mHelper.getWritableDatabase();
        String sql = "insert into " + Constant.TABLE_NAME + " (DataId,Type,Part,Stage,BladeNumber,Position) values(?,?,?,?,?,?)";

        Constant.DataId++;

        db.execSQL(sql,new Object[]{Constant.DataId,Constant.MAK_ENGINE_TYPE,Constant.MAK_ENGINE_PART,Constant.MAK_ENGINE_STAGE,Constant.MAK_NUMBER_BLADE,Constant.DATA_POSITION});
        db.close();
    }

//    public void delete(int DataID) {
//        String DataIDString = String.valueOf(DataID);
//        SQLiteDatabase db = mHelper.getWritableDatabase();
//        String sql = "delete from " + Constant.TABLE_NAME + " where _id = " + DataIDString;
//        db.execSQL(sql);
//        db.close();
//    }
    public void delete(int DataID) {
        String DataIDString = String.valueOf(DataID);
        SQLiteDatabase db = mHelper.getWritableDatabase();
        db.delete(Constant.TABLE_NAME , "rowId = ?" , new String[]{DataIDString});
        //更新其余的ID以使输入连续
        String sql = "update Label_of_bad_blade set rowId = rowId-1 where rowId > "+DataIDString;
        db.execSQL(sql);
        db.close();
    }

    public void updata() {
//        SQLiteDatabase db = mHelper.getWritableDatabase();
////        String sql = "update " + Constant.TABLE_NAME + " set DataId = 0 where DataId = 0";
////        db.execSQL(sql);
////        db.close();
    }

    public void query() {
        SQLiteDatabase db = mHelper.getWritableDatabase();
        String sql = "select * from " + Constant.TABLE_NAME;
        Cursor cursor = db.rawQuery(sql, null);

        while (cursor.moveToNext()) {
            int index = cursor.getColumnIndex("BladeNumber");
            int myMakeBlade = cursor.getInt(index);
            Log.d(TAG,"BladeNum" + myMakeBlade);
        }
        cursor.close();
        db.close();
    }

    public Cursor readAllData() {
        String query = "select * from " + Constant.TABLE_NAME;
        SQLiteDatabase db = mHelper.getReadableDatabase();

        Cursor cursor = null;
        if (db != null) {
            cursor = db.rawQuery(query, null);
        }
        return cursor;
    }

    public void deleteDatebase() {
        thisContext.deleteDatabase(Constant.DATABASE_NAME);
    }
}
