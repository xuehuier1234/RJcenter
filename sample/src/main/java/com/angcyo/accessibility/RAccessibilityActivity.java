package com.angcyo.accessibility;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import com.angcyo.sample.R;
import com.rsen.base.RBaseActivity;
import com.rsen.util.T;

import java.util.List;

public class RAccessibilityActivity extends RBaseActivity implements AccessibilityManager.AccessibilityStateChangeListener {

    private AccessibilityManager accessibilityManager;

    @Override
    protected int getContentView() {
        return R.layout.activity_raccessibility;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        //监听AccessibilityService 变化
        accessibilityManager = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        accessibilityManager.addAccessibilityStateChangeListener(this);

        mViewHolder.v("enable").setOnClickListener(v -> {
            try {
                Intent accessibleIntent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
                startActivity(accessibleIntent);
            } catch (Exception e) {
                T.show(RAccessibilityActivity.this, "遇到一些问题,请手动打开系统设置>辅助服务>Rsen微信助手");
                e.printStackTrace();
            }
        });

        updateServiceStatus();
    }

    /**
     * 更新当前 HongbaoService 显示状态
     */
    private void updateServiceStatus() {
        if (isServiceEnabled()) {
            mViewHolder.tV("enable").setText("关闭");
        } else {
            mViewHolder.tV("enable").setText("开启");
        }
    }

    /**
     * 获取 Service 是否启用状态
     *
     * @return
     */
    private boolean isServiceEnabled() {
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            if (info.getId().contains("com.angcyo.accessibility.RsenAccessibilityService")) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onAccessibilityStateChanged(boolean enabled) {
        updateServiceStatus();
    }
}
