package com.example.lzw.wallpaper;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.lzw.wallpaper.util.GradientTextView;


public class WelcomeActivity extends Activity {
    Button horizontal, vertical, start;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bj);
        start = (Button) findViewById(R.id.start);
        horizontal = (Button) findViewById(R.id.horizontal);
        vertical = (Button) findViewById(R.id.vertical);
        final GradientTextView gradientTextView = (GradientTextView) findViewById(R.id.name);
        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WelcomeActivity.this, MyActivity.class);
                startActivity(intent);
                WelcomeActivity.this.finish();
            }
        });

        horizontal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientTextView.setGradientOrientation(GradientTextView.HORIZONTAL);
                gradientTextView.startAnimator();
            }
        });
        vertical.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gradientTextView.setGradientOrientation(GradientTextView.VERTICAL);
                gradientTextView.startAnimator();
            }
        });
    }


}
