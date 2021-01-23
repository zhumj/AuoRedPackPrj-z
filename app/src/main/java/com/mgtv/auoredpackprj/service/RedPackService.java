package com.mgtv.auoredpackprj.service;

import android.accessibilityservice.AccessibilityService;
import android.content.ComponentName;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

import com.mgtv.auoredpackprj.utils.CommonUtils;

import java.util.List;
import java.util.regex.Pattern;

public class RedPackService extends AccessibilityService implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = "RedPackService";

    private static final String DEFAULT_ACTIVITY_NAME = "LauncherUI";

    private static final String ACTIVITY_NAME_FOR_LUCKY_MONEY = "LuckyMoneyNotHookReceiveUI";

    private static final String ACTIVITY_NAME_FOR_LUCKY_MONEY_Detail = "LuckyMoneyDeta";

    private static String CHAT_PAGE_TITLE_TEMPLATE = "当前所在页面,与.*的聊天";

    private String mCurrentActivityName = "";

    private Pattern mChatPageTitlePattern;

    private SharedPreferences mSharePreference;

    private Handler mHandler = new android.os.Handler();

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        recordCurrentActivityName(event);
        int eventType = event.getEventType();
        switch (eventType) {
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                // 在红包详情页，点击返回
                if (mCurrentActivityName.contains(ACTIVITY_NAME_FOR_LUCKY_MONEY_Detail)) {
                    mHandler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            performGlobalAction(GLOBAL_ACTION_BACK);
                        }
                    }, 500);
                } else if (mCurrentActivityName.contains(ACTIVITY_NAME_FOR_LUCKY_MONEY)) {
                    applyBtnClick();
                }
                break;
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                if (mCurrentActivityName.contains(DEFAULT_ACTIVITY_NAME)) {
                    applyOpenPacket();
                }
                break;
            default:
                break;
        }
    }

    private void applyOpenPacket() {
        AccessibilityNodeInfo rootNode = getRootInActiveWindow();
        if (rootNode == null) {
            return;
        }
        List<AccessibilityNodeInfo> nodeList = rootNode.findAccessibilityNodeInfosByText("微信红包");
        if (nodeList == null) {
            return;
        }
        for (int i = nodeList.size() - 1; i >= 0; i--) {
            AccessibilityNodeInfo node = nodeList.get(i);
            AccessibilityNodeInfo parent = node.getParent();
            if (parent == null) {
                return;
            }
            List<AccessibilityNodeInfo> tmp1 = parent.findAccessibilityNodeInfosByText("已被领完");
            List<AccessibilityNodeInfo> tmp2 = parent.findAccessibilityNodeInfosByText("已过期");
            List<AccessibilityNodeInfo> tmp3 = parent.findAccessibilityNodeInfosByText("已领取");
            if (tmp1.size() == 0 && tmp2.size() == 0 && tmp3.size() == 0) {
                // 过滤自己发的红包
                if (CommonUtils.isSelfSend(node)) {
                    continue;
                }
                while (parent != null) {
                    if (parent.isClickable()) {
                        parent.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                        break;
                    }
                    parent = parent.getParent();
                }
            }
        }
    }

    /**
     * 拆开红包
     */
    private void applyBtnClick() {
        for (int i = 0; i < 20; i++) {
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                List<AccessibilityNodeInfo> nodeList = rootNode.findAccessibilityNodeInfosByText("开");
                if (nodeList != null) {
                    for (AccessibilityNodeInfo node : nodeList) {
                        AccessibilityNodeInfo tmp = node;
                        while (tmp != null) {
                            if (tmp.isClickable()) {
                                tmp.performAction(AccessibilityNodeInfo.ACTION_CLICK);
                                return;
                            }
                            tmp = tmp.getParent();
                        }
                    }
                }
            }
            try {
                synchronized (RedPackService.this) {
                    wait(50);
                }
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }
    }

    /**
     * 记录当前页面所在的activity的类名
     *
     */
    private void recordCurrentActivityName(AccessibilityEvent event) {
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            return;
        }
        mCurrentActivityName = DEFAULT_ACTIVITY_NAME;
        try {
            ComponentName componentName = new ComponentName(event.getPackageName().toString(), event.getClassName().toString());

            getPackageManager().getActivityInfo(componentName, 0);
            String className = componentName.flattenToShortString();
            if (!TextUtils.isEmpty(className)) {
                mCurrentActivityName = className;
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onInterrupt() {

    }

    /**
     * service连接成功后，初始化相关数据
     */
    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        init();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

    }

    private void init() {
        mSharePreference = PreferenceManager.getDefaultSharedPreferences(this);
        mSharePreference.registerOnSharedPreferenceChangeListener(this);
        if (mChatPageTitlePattern == null) {
            mChatPageTitlePattern = Pattern.compile(CHAT_PAGE_TITLE_TEMPLATE);
        }
    }
}