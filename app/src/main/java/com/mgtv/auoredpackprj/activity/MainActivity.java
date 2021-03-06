package com.mgtv.auoredpackprj.activity;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.mgtv.auoredpackprj.R;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity implements AccessibilityManager.AccessibilityStateChangeListener {

    private TextView pluginStatusText;
    private ImageView pluginStatusIcon;

    private AccessibilityManager accessibilityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pluginStatusText = findViewById(R.id.layout_control_accessibility_text);
        pluginStatusIcon = findViewById(R.id.layout_control_accessibility_icon);

        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateServiceStatus();
    }

    @Override
    protected void onDestroy() {
        //移除监听服务
        accessibilityManager.removeAccessibilityStateChangeListener(this);
        super.onDestroy();
    }


    /**
     * 更新当前 插件 显示状态
     */
    private void updateServiceStatus() {
        boolean isServiceEnabled = false;
        List<AccessibilityServiceInfo> accessibilityServices = accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().equals(getPackageName() + "/.service.RedPackService")) {
                isServiceEnabled = true;
                break;
            }
        }
        if (isServiceEnabled) {
            pluginStatusText.setText(R.string.service_off);
            pluginStatusIcon.setBackgroundResource(R.drawable.ic_stop);
        } else {
            pluginStatusText.setText(R.string.service_on);
            pluginStatusIcon.setBackgroundResource(R.drawable.ic_start);
        }
    }

    public void openAccessibility(View view) {
        try {
            Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(accessibleIntent);
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.turn_on_error_toast), Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        updateServiceStatus();
    }
}
