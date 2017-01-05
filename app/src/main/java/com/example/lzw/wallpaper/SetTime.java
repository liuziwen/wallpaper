package com.example.lzw.wallpaper;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


public class SetTime extends Activity {
SharedPreferences preferences;
SharedPreferences.Editor editor;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_set_time);
        preferences=getSharedPreferences("mytime", Context.MODE_PRIVATE);
        editor=preferences.edit();
        final TextView time=(TextView)findViewById(R.id.time1);
        Button up=(Button)findViewById(R.id.up);
        Button down=(Button)findViewById(R.id.down);
        Button set=(Button)findViewById(R.id.set);
        final EditText newtime=(EditText)findViewById(R.id.newtime);
        final int t=preferences.getInt("time",5);
//        editor.putInt("time", 5);
//        editor.commit();
        time.setText("当前设置每"+t+"秒更换一次壁纸");
        newtime.setText(""+preferences.getInt("time",5));
        set.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editor.putInt("time", Integer.parseInt(newtime.getText().toString()));
                editor.commit();
                time.setText("当前设置每"+preferences.getInt("time",5)+"秒更换一次壁纸");
            }
        });


        up.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a=Integer.parseInt(newtime.getText().toString());
                newtime.setText(""+(a+1));
            }
        });


        down.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int a = Integer.parseInt(newtime.getText().toString());
                if(a>1) newtime.setText(""+(a-1));
                else newtime.setText("1");

            }
        });


    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.set_time, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
