package com.example.lzw.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;


public class WelcomeActivity extends Activity {
    Button start, help;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bj);
        start = (Button) findViewById(R.id.start);
        help = (Button) findViewById(R.id.help);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, MyActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        });

        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent in = new Intent(WelcomeActivity.this, Hlp.class);
                startActivity(in);
            }
        });
    }


}
