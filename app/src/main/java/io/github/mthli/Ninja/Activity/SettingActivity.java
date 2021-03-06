package io.github.mthli.Ninja.Activity;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import io.github.mthli.Ninja.Task.ImportBookmarksTask;
import io.github.mthli.Ninja.Task.ImportWhitelistTask;
import io.github.mthli.Ninja.View.NinjaToast;
import io.github.mthli.Ninja.Fragment.SettingFragment;
import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.Unit.IntentUnit;

import java.io.File;

public class SettingActivity extends BaseActivity {
    private SettingFragment fragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        fragment = new SettingFragment();
        getFragmentManager().beginTransaction().replace(R.id.settingFrame, fragment).commit();

        TextView tvBottomLeft = (TextView)findViewById(R.id.tvBottomLeft);
        tvBottomLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        TextView tvBottomCenter = (TextView)findViewById(R.id.tvBottomCenter);
        tvBottomCenter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        TextView tvBottomRight = (TextView)findViewById(R.id.tvBottomRight);
        tvBottomRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTouchBack();
            }
        });
    }

    public void onTouchBack(){
        new Thread(){
            public void run() {
                try{
                    Instrumentation inst = new Instrumentation();
                    inst.sendKeyDownUpSync(KeyEvent.KEYCODE_BACK);
                }
                catch (Exception e) {
                    Log.e("Exception when onBack", e.toString());
                }
            }
        }.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                IntentUnit.setDBChange(fragment.isDBChange());
                IntentUnit.setSPChange(fragment.isSPChange());
                finish();
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent keyEvent) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            IntentUnit.setDBChange(fragment.isDBChange());
            IntentUnit.setSPChange(fragment.isSPChange());
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == IntentUnit.REQUEST_BOOKMARKS) {
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                NinjaToast.show(this, R.string.toast_import_bookmarks_failed);
            } else {
                File file = new File(data.getData().getPath());
                new ImportBookmarksTask(fragment, file).execute();
            }
        } else if (requestCode == IntentUnit.REQUEST_WHITELIST) {
            if (resultCode != Activity.RESULT_OK || data == null || data.getData() == null) {
                NinjaToast.show(this, R.string.toast_import_whitelist_failed);
            } else {
                File file = new File(data.getData().getPath());
                new ImportWhitelistTask(fragment, file).execute();
            }
        } else if (requestCode == IntentUnit.REQUEST_CLEAR) {
            if (resultCode == Activity.RESULT_OK && data != null && data.hasExtra(ClearActivity.DB_CHANGE)) {
                fragment.setDBChange(data.getBooleanExtra(ClearActivity.DB_CHANGE, false));
            }
        }
    }
}
