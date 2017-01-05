package com.example.lzw.wallpaper.Net;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;

import com.example.lzw.wallpaper.R;


public class MyWebView extends Activity {
    WebView web;

    Button bn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_web_view);
        web=(WebView)findViewById(R.id.web);

        bn=(Button)findViewById(R.id.bn);
        web.loadUrl("http://www.baidu.com");

        web.getSettings().getJavaScriptEnabled();
        web.setLayerType(View.LAYER_TYPE_SOFTWARE, null);

        web.setWebViewClient(new WebViewClient()

        {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view,
                                                    String url) {
                // TODO Auto-generated method stub
                view.loadUrl(url);
                return true;
            }
        });


        bn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
        web.goBack();
            }
        });


    }




    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.web_view, menu);
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
