package com.example.Drive_system.DataBase;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.Drive_system.Engine_Drive_Activity;
import com.example.Drive_system.connect.Constant;
import com.example.drivesystemcontroller.R;

import java.util.ArrayList;

public class CustomDisplayAdapter extends RecyclerView.Adapter <CustomDisplayAdapter.MyViewHolder> {
    private final Context context;
    private final ArrayList textViewNumber;
    private final ArrayList engine_mode;
    private final ArrayList engine_parts;
    private final ArrayList engine_stages;
    private final ArrayList numBlade;
    private AlertDialog mRowAlertDialog = null;
    private AlertDialog.Builder builder = null;

    public CustomDisplayAdapter(Context context, ArrayList textViewNumber, ArrayList engine_mode, ArrayList engine_parts, ArrayList engine_stages, ArrayList numBlade) {
        this.context = context;
        this.textViewNumber = textViewNumber;
        this.engine_mode = engine_mode;
        this.engine_parts = engine_parts;
        this.engine_stages = engine_stages;
        this.numBlade = numBlade;
       }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.my_recyclerview_row, parent, false);
        final MyViewHolder holder = new MyViewHolder(view);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {       }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        holder.textViewNumber.setText(String.valueOf(textViewNumber.get(position)));
        holder.engine_mode_txt.setText(String.valueOf(engine_mode.get(position)));
        holder.engine_parts_txt.setText(String.valueOf(engine_parts.get(position)));
        holder.engine_stages_txt.setText(String.valueOf(engine_stages.get(position)));
        holder.NumBlade_txt.setText((numBlade.get(position) + "号"));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mRowAlertDialog = null;
                builder = new AlertDialog.Builder(context);
                builder.setTitle("确定转到标记位吗？");
                builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        Constant.MARKED_POSITION= Constant.Blade_need_Return.get(position);//Integer.parseInt(S);
                        Engine_Drive_Activity.BackMarkPosition();

                    }
                });
                builder.setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });
                mRowAlertDialog=builder.create();
                mRowAlertDialog.show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return engine_mode.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {

           TextView textViewNumber, engine_mode_txt, engine_parts_txt, engine_stages_txt, NumBlade_txt;

         MyViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewNumber = itemView.findViewById(R.id.textViewNumber);
            engine_mode_txt = itemView.findViewById(R.id.engine_mode_txt);
            engine_parts_txt = itemView.findViewById(R.id.engine_parts_txt);
            engine_stages_txt = itemView.findViewById(R.id.engine_stages_txt);
            NumBlade_txt = itemView.findViewById(R.id.NumBlade_txt);
        }
    }
}