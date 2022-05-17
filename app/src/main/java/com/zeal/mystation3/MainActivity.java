package com.zeal.mystation3;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.zeal.mystation3.view.AboutActivity;
import com.zeal.mystation3.view.MapActivity;
import com.zeal.mystation3.view.SplashActivity;

public class MainActivity extends AppCompatActivity {

    private Button playBtn, remoteAlbumBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        findViewById(R.id.btn_map).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, MapActivity.class);
            startActivity(intent);
        });

        findViewById(R.id.btn_about).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            startActivity(intent);
        });

    }


}