package io.github.mthli.Ninja.Utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

/**
 * Created by zl on 2017/8/15.
 */

public class MouseModeCtrl {

    Context mContext;
    private static MouseModeCtrl instance = null;
    private boolean mouseMode = true;
    private boolean bgOrExitOff = false;
    private SharedPreferences sp;
    private final static String SP_DB_NAME = "fpmousemode";
    private final static String FP_MOUSEMODE_KEY = "mm_key";
    private final static String FP_MOUSEMODE_FIRST_KEY = "mm_first";
    private final static String FP_BROADCAST_MOUSE_MODE_ON = "com.fp.broadcasttest.mouse.mode.on";
    private final static String FP_BROADCAST_MOUSE_MODE_OFF = "com.fp.broadcasttest.mouse.mode.off";

    public static MouseModeCtrl getInstance(Context context){
        if (instance == null){
            instance = new MouseModeCtrl(context);
        }
        return instance;
    }

    public MouseModeCtrl(Context context){
        mContext = context;
        if (context != null) {
            sp = context.getSharedPreferences(SP_DB_NAME, Context.MODE_PRIVATE);
            if (sp != null)
                mouseMode = sp.getBoolean(FP_MOUSEMODE_KEY, true);
        }
    }

    private void saveMouseModeSP(boolean status){
        if (sp != null)
            sp.edit().putBoolean(FP_MOUSEMODE_KEY, status);
    }

    public void setMouseMode(boolean flag){
        if (mContext == null) return;
        if (flag == true){
            Log.d("[Mouse]", "turn on");
            Intent intent = new Intent(FP_BROADCAST_MOUSE_MODE_ON);
            mContext.sendBroadcast(intent);
            mouseMode = true;
            saveMouseModeSP(mouseMode);
        } else {
            Log.d("[Mouse]", "turn off");
            Intent intent = new Intent(FP_BROADCAST_MOUSE_MODE_OFF);
            mContext.sendBroadcast(intent);
            mouseMode = false;
            saveMouseModeSP(mouseMode);
        }
    }

    public boolean getMouseMode(){
        return mouseMode;
    }

    public void firstStartAction(){
        if (sp != null){
            boolean first = sp.getBoolean(FP_MOUSEMODE_FIRST_KEY, true);
            if (first == true){
                Log.d("[Mouse]", "first start");
                sp.edit().putBoolean(FP_MOUSEMODE_FIRST_KEY, false);
                setMouseMode(false);
            }
        }
    }

    public void closeMouseModeWhenBGOrExit(){
        if (mContext != null && bgOrExitOff == false){
            Log.d("[Mouse]", "bg or exit close");
            Intent intent = new Intent(FP_BROADCAST_MOUSE_MODE_OFF);
            mContext.sendBroadcast(intent);
            bgOrExitOff = true;
        }
    }

    public void resumeMouseMode(){
        if (mContext != null){
            if (mouseMode == true && bgOrExitOff == true){
                Log.d("[Mouse]", "re-open");
                Intent intent = new Intent(FP_BROADCAST_MOUSE_MODE_ON);
                mContext.sendBroadcast(intent);
                bgOrExitOff = false;
            }
        }
    }
}
