package io.github.mthli.Ninja.Activity;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

import java.util.List;

import io.github.mthli.Ninja.Utils.MouseModeCtrl;

/**
 * Created by gmobi-zl on 2017/8/15.
 */

public class BaseActivity extends Activity{

    boolean isActive = true;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isActive = true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (!isAppOnForeground()) {
            MouseModeCtrl.getInstance(this).closeMouseModeWhenBGOrExit();
            isActive = false;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (!isActive) {
            MouseModeCtrl.getInstance(this).resumeMouseMode();
            isActive = true;
        }
    }

    public boolean isAppOnForeground() {
        ActivityManager activityManager = (ActivityManager) getApplicationContext().getSystemService(Context.ACTIVITY_SERVICE);
        String packageName = getApplicationContext().getPackageName();

        List<ActivityManager.RunningAppProcessInfo> appProcesses = activityManager
                .getRunningAppProcesses();
        if (appProcesses == null)
            return false;

        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(packageName)
                    && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }

        return false;
    }
}
