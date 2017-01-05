package com.example.lzw.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class BjActivity extends Activity {
Button start,help;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bj);
        start =(Button)findViewById(R.id.start);
        help =(Button)findViewById(R.id.help);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BjActivity.this.finish();
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in=new Intent(BjActivity.this,Hlp.class);
                startActivity(in);
                BjActivity.this.finish();
            }
        });
    }


}
