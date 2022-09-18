package com.example.Drive_system;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Drive_system.DataBase.CustomDisplayAdapter;
import com.example.Drive_system.DataBase.Dao;
import com.example.Drive_system.connect.Constant;
import com.example.drivesystemcontroller.R;

import java.util.ArrayList;

public class DataRecordActivity extends AppCompatActivity {
    private Toast mToast;
    Dao dao;
    private RecyclerView recyclerView;
    ArrayList<String> engine_mode, engine_parts, engine_stages;
    ArrayList<Integer> textViewNumber,NumBlade,Blade_P;

    private CustomDisplayAdapter customDisplayAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_record);

        recyclerView = findViewById(R.id.recyclerView);
        dao = new Dao(getApplicationContext());

        textViewNumber = new ArrayList<>();
        engine_mode = new ArrayList<>();
        engine_parts = new ArrayList<>();
        engine_stages = new ArrayList<>();
        NumBlade = new ArrayList<>();
        Blade_P= new ArrayList<>();

        displayData();

        customDisplayAdapter = new CustomDisplayAdapter(DataRecordActivity.this, textViewNumber,engine_mode, engine_parts, engine_stages,NumBlade);

        recyclerView.setAdapter(customDisplayAdapter);
        recyclerView.setLayoutManager(new LinearLayoutManager(DataRecordActivity.this));
        initUI_EngineControl();

        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.START | ItemTouchHelper.END) {
            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
                Dao dao = new Dao(getApplicationContext());
                int a=viewHolder.getAdapterPosition();
                dao.delete(a+1);
            }

            Drawable icon = ContextCompat.getDrawable(getApplicationContext(),R.drawable.ic_delete_forever_black_24dp);
            Drawable background = new ColorDrawable(Color.LTGRAY);
            @Override
            public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
                View itemView = viewHolder.itemView;
                int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;

                int iconLeft,iconRight,iconTop,iconBottom;
                int backTop,backBottom,backLeft,backRight;
                backTop = itemView.getTop();
                backBottom = itemView.getBottom();
                iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) /2;
                iconBottom = iconTop + icon.getIntrinsicHeight();
                if (dX > 0) {
                    backLeft = itemView.getLeft();
                    backRight = itemView.getLeft() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconLeft = itemView.getLeft() + iconMargin ;
                    iconRight = iconLeft + icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else if (dX < 0){
                    backRight = itemView.getRight();
                    backLeft = itemView.getRight() + (int)dX;
                    background.setBounds(backLeft,backTop,backRight,backBottom);
                    iconRight = itemView.getRight()  - iconMargin;
                    iconLeft = iconRight - icon.getIntrinsicWidth();
                    icon.setBounds(iconLeft,iconTop,iconRight,iconBottom);
                } else {
                    background.setBounds(0,0,0,0);
                    icon.setBounds(0,0,0,0);
                }
                background.draw(c);
                icon.draw(c);
            }
        }).attachToRecyclerView(recyclerView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.data_record_menu,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.clearData:
                AlertDialog.Builder builder = new AlertDialog.Builder(DataRecordActivity.this);
                builder.setTitle("清空数据库?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dao dao = new Dao(getApplicationContext());
                        dao.deleteDatebase();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                });
                builder.create();
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void displayData() {
        Cursor cursor =  dao.readAllData();
        if (cursor.getCount() == 0) {
            showToast("没有数据!");
        }else {
            while (cursor.moveToNext()) {
                textViewNumber.add(cursor.getInt(0));
                engine_mode.add(cursor.getString(1));
                engine_parts.add(cursor.getString(2));
                engine_stages.add(cursor.getString(3));
                NumBlade.add(cursor.getInt( 4));
                Blade_P.add(cursor.getInt( 5));
                Constant.Blade_need_Return= Blade_P;
            }
        }
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




    private void initUI_EngineControl() {
        Button myCeshi = (Button) findViewById(R.id.button111);
        myCeshi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Dao dao = new Dao(getApplicationContext());
                dao.insert();
            }
        });

        Button myCeshi2 = (Button) findViewById(R.id.button222);
        myCeshi2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(DataRecordActivity.this);
                builder.setTitle("清空数据库?");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Dao dao = new Dao(getApplicationContext());
                        dao.deleteDatebase();
                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                builder.create();
                builder.show();

            }
        });

        Button myCeshi3 = (Button) findViewById(R.id.button333);
        myCeshi3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Constant.MAK_NUMBER_BLADE++;

            }
        });
    }
}
