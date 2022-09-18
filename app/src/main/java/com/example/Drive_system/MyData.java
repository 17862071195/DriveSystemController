package com.example.Drive_system;

import android.content.Context;
import android.content.SharedPreferences;

public class MyData {

    public int number;
    public String str;
    public Float flo;
    private final Context context;
    public MyData(Context context) {
        this.context = context;
    }

    public void SaveFloat(String DataName) {
        SharedPreferences shp = context.getSharedPreferences("Global_Variable_string",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putFloat(DataName,flo);
        editor.apply();
    }

    public Float LoadFloat(String DataName) {
        SharedPreferences shp = context.getSharedPreferences("Global_Variable_string",Context.MODE_PRIVATE);
        Float f = shp.getFloat(DataName,0);
        flo = f;
        return f;
    }

    public void SaveString(String DataName) {
        SharedPreferences shp = context.getSharedPreferences("Global_Variable_string",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putString(DataName,str);
        editor.apply();
    }

    public String LoadString(String DataName) {
        SharedPreferences shp = context.getSharedPreferences("Global_Variable_string",Context.MODE_PRIVATE);
        String s = shp.getString(DataName,"");
        str = s;
        return s;
    }

    public void SaveInt(String DataName) {
        SharedPreferences shp = context.getSharedPreferences("Global_Variable_int",Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = shp.edit();
        editor.putInt(DataName,number);
        editor.apply();
    }

    public int LoadInt(String DataName) {
        SharedPreferences shp = context.getSharedPreferences("Global_Variable_int",Context.MODE_PRIVATE);
        int a = shp.getInt(DataName,0);
        number = a;
        return a;
    }
}