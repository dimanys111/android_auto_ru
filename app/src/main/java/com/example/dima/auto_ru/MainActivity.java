package com.example.dima.auto_ru;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    public static MainActivity Ser=null;

    @Override
    protected void onResume() {
        super.onResume();

        if (WalkingIconService.Ser==null)
            startService(new Intent(this, WalkingIconService.class));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Ser=this;
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        Ser=null;
    }

    public void onMyButtonClick(View view)
    {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(WalkingIconService.ClientSocketThread.stringUrl));
        startActivity(browserIntent);
    }

    protected void tex(String s) {
        TextView t=(TextView)findViewById(R.id.textView4);
        t.setText(s);
    }
}
