package io.github.mthli.Ninja;

import android.app.Application;

import com.mocean.AdServiceManager;
import com.mocean.IAdService;
import com.mocean.IServiceCallback;

/**
 * Created by zl on 2017/6/21.
 */

public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        AdServiceManager.get(this, new IServiceCallback<IAdService>(){
            @Override
            public void call(IAdService service) {
            }
        });
    }
}
