package com.faceunity.litenama.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.faceunity.litenama.R;
import com.faceunity.litenama.util.PermissionUtils;
import com.faceunity.nama.FURenderer;
import com.faceunity.nama.ui.OnMultiClickListener;

/**
 * 开屏页
 *
 * @author Richie on 2019.12.20
 */
public class SplashActivity extends AppCompatActivity {
    public static final String OPEN_EFFECT = "open_effect";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        PermissionUtils.checkNeededPermission(this);

        final SharedPreferences preferences = getPreferences(MODE_PRIVATE);
        boolean isChecked = preferences.getBoolean(OPEN_EFFECT, true);
        final SwitchCompat switchCompat = findViewById(R.id.sw_effect);
        switchCompat.setChecked(isChecked);
        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                preferences.edit().putBoolean(OPEN_EFFECT, isChecked).apply();
            }
        });
        findViewById(R.id.btn_start).setOnClickListener(new OnMultiClickListener() {
            @Override
            protected void onMultiClick(View v) {
                boolean checked = switchCompat.isChecked();
                if (checked) {
                    FURenderer.setup(SplashActivity.this);
                }
                Intent intent = new Intent(SplashActivity.this, MainActivity.class);
                intent.putExtra(OPEN_EFFECT, checked);
                startActivity(intent);
            }
        });
    }
}
