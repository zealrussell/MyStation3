package com.zeal.mystation3;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.zeal.mystation3.view.AboutActivity;
import com.zeal.mystation3.view.MapActivity;
import com.zeal.mystation3.view.SplashActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    // 所需权限：写外存、录音、摄像
    private static final String[] REQUIRED_PERMISSION_LIST = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.CAMERA, // only required when targetSdkVersion >= 28
    };
    private static final int REQUEST_CODE = 1;
    private List<String> mMissPermissions = new ArrayList<>();

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

        if (isVersionM()) {
            checkAndRequestPermissions();
        }

    }

    // 版本是否大于 23
    private boolean isVersionM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }

    /**
     * 检查权限
     */
    private void checkAndRequestPermissions() {
        mMissPermissions.clear();
        for (String permission : REQUIRED_PERMISSION_LIST) {
            int result = ContextCompat.checkSelfPermission(this, permission);
            if (result != PackageManager.PERMISSION_GRANTED) {
                mMissPermissions.add(permission);
            }
        }
        // check permissions has granted
        if (!mMissPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    mMissPermissions.toArray(new String[mMissPermissions.size()]),
                    REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            for (int i = grantResults.length - 1; i >= 0; i--) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    mMissPermissions.remove(permissions[i]);
                }
            }
        }
        // 有权限缺失
        if (!mMissPermissions.isEmpty()) {
            // Get permissions success or not
            Toast.makeText(this, "get permissions failed,exiting...", Toast.LENGTH_SHORT).show();
            MainActivity.this.finish();
        }
    }


}