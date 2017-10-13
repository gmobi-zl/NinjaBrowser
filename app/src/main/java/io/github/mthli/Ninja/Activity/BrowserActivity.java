package io.github.mthli.Ninja.Activity;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.*;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.*;

import com.mocean.AdServiceManager;
import com.mocean.IAd;
import com.mocean.IAdService;
import com.mocean.ICallback;
import com.mocean.IServiceCallback;

import io.github.mthli.Ninja.Browser.AdBlock;
import io.github.mthli.Ninja.Browser.AlbumController;
import io.github.mthli.Ninja.Browser.BrowserContainer;
import io.github.mthli.Ninja.Browser.BrowserController;
import io.github.mthli.Ninja.Service.ClearService;
import io.github.mthli.Ninja.Database.Record;
import io.github.mthli.Ninja.Database.RecordAction;
import io.github.mthli.Ninja.R;
import io.github.mthli.Ninja.Service.HolderService;
import io.github.mthli.Ninja.Utils.AdvertisingIdClient;
import io.github.mthli.Ninja.Unit.BrowserUnit;
import io.github.mthli.Ninja.Unit.IntentUnit;
import io.github.mthli.Ninja.Utils.BookmarksUtil;
import io.github.mthli.Ninja.Utils.FileHelper;
import io.github.mthli.Ninja.Utils.HttpHelper;
import io.github.mthli.Ninja.Utils.JsonHelper;
import io.github.mthli.Ninja.Utils.MouseModeCtrl;
import io.github.mthli.Ninja.Utils.SystemHelper;
import io.github.mthli.Ninja.Unit.ViewUnit;
import io.github.mthli.Ninja.View.*;
import org.askerov.dynamicgrid.DynamicGridView;
import org.json.JSONObject;

import java.io.File;
import java.util.*;

public class BrowserActivity extends BaseActivity implements BrowserController {
    // Sync with NinjaToast.show() 2000ms delay
    private static final int DOUBLE_TAPS_QUIT_DEFAULT = 2000;

    private static Activity holdActivity;
//    private SwitcherPanel switcherPanel;
    private int anchor;
    private float dimen156dp;
    private float dimen144dp;
    private float dimen117dp;
    private float dimen108dp;
    private float dimen48dp;

    private static final String DEFAULT_HOME_PAGE = "http://search.viebrowser.com";
    private static final String CUSTOME_HOME_PAGE = "http://www.google.hr";//"http://www.google.co.th";
    private boolean showStartPage = true;
    Map<String, String> extraHeaders = null;

//    private HorizontalScrollView switcherScroller;
//    private LinearLayout switcherContainer;
    //private LinearLayout switcherRootView;
//    private ImageButton switcherSetting;
//    private ImageButton switcherBookmarks;
//    private ImageButton switcherHistory;
//    private ImageButton switcherAdd;

    private RelativeLayout omnibox;
    private MoAutoCompleteTextView inputBox;
//    private ImageButton omniboxBookmark;
//    private ImageButton omniboxRefresh;
//    private ImageButton omniboxOverflow;
    private ProgressBar progressBar;
    private RelativeLayout bottomMenu;

//    private RelativeLayout searchPanel;
//    private EditText searchBox;
//    private ImageButton searchUp;
//    private ImageButton searchDown;
//    private ImageButton searchCancel;

//    private Button relayoutOK;
    private FrameLayout contentFrame;

    private class VideoCompletionListener implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            return false;
        }

        @Override
        public void onCompletion(MediaPlayer mp) {
            onHideCustomView();
        }
    }
    private FullscreenHolder fullscreenHolder;
    private View customView;
    private VideoView videoView;
    private int originalOrientation;
    private WebChromeClient.CustomViewCallback customViewCallback;
    private ValueCallback<Uri> uploadMsg = null;
    private ValueCallback<Uri[]> filePathCallback = null;

    private static boolean quit = false;
    private boolean create = true;
    private int shortAnimTime = 0;
    private int mediumAnimTime = 0;
    private int longAnimTime = 0;
    private AlbumController currentAlbumController = null;


    private String adid;
    private String ipcountry;
    private String ipcity;
    private Handler extraHandler = new Handler();
    private Context mContext;
    final static String URL_IP_LOCATION = "http://ip.mocean.cc/s";



    /**
     * Add for low memory mode
     *@{
     */
    @Override
    public void onTrimMemory(int level) {
        Log.d(BaseActivity.LOG_TAG, "BrowserActivity.onTrimMemory() level = " + level);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d(BaseActivity.LOG_TAG, "BrowserActivity onLowMemory");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i(BaseActivity.LOG_TAG, "========== onActivityResult ================ ");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            filePathCallback.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
        } else {
            Log.i(BaseActivity.LOG_TAG, "========== requestCode = " + requestCode);
            Log.i(BaseActivity.LOG_TAG, "========== resultCode = " + resultCode);
            if (requestCode == IntentUnit.REQUEST_FILE_16){
                if (null == this.uploadMsg) return;
                Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                try {
                    //Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), intent.getData());
                    //Uri result = intent == null ? null : FileHelper.savePicture(this, bitmap, 1400);
                    Log.d(BaseActivity.LOG_TAG, "========== result = " + result);
                    this.uploadMsg.onReceiveValue(result);
                    this.uploadMsg = null;
                } catch (Exception e){
                    Log.d(BaseActivity.LOG_TAG, "========== e = " + e.toString());
                }
            }
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(BaseActivity.LOG_TAG,"Browser onCreate");
        mContext = this;
        holdActivity = this;

        if (showStartPage == true){
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        adid = AdvertisingIdClient.getAdvertisingIdInfo(mContext).getId();
//                    } catch (Exception e) {
//                        Log.e("error", e.toString());
//                    }
//
//                    try{
//                        HttpHelper.Response resp = HttpHelper.doGet(URL_IP_LOCATION, null);
//                        if (resp != null && resp.getBody() != null){
//                            JSONObject jo = JsonHelper.parse(resp.getBody());
//                            ipcountry = jo.optString("country", null);
//                            ipcity = jo.optString("city", null);
//                        }
//                    }catch(Exception e){
//                        Log.e("error", e.toString());
//                    }
//                }
//            }).start();
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityManager.TaskDescription description = new ActivityManager.TaskDescription(
                    getString(R.string.app_name),
                    BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher),
                    getResources().getColor(R.color.background_dark)
            );
            setTaskDescription(description);
        }

//        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//        anchor = Integer.valueOf(sp.getString(getString(R.string.sp_anchor), "0"));
//        if (anchor == 0) {
//            setContentView(R.layout.main_top);
//        } else {
//            setContentView(R.layout.main_bottom);
//        }
        setContentView(R.layout.main_lite);

        create = true;
        shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
        mediumAnimTime = getResources().getInteger(android.R.integer.config_mediumAnimTime);
        longAnimTime = getResources().getInteger(android.R.integer.config_longAnimTime);
//        switcherPanel = (SwitcherPanel) findViewById(R.id.switcher_panel);
//        switcherPanel.setStatusListener(new SwitcherPanel.StatusListener() {
//            @Override
//            public void onFling() {}
//
//            @Override
//            public void onExpanded() {}
//
//            @Override
//            public void onCollapsed() {
//                inputBox.clearFocus();
//            }
//        });

        dimen156dp = getResources().getDimensionPixelSize(R.dimen.layout_width_156dp);
        dimen144dp = getResources().getDimensionPixelSize(R.dimen.layout_width_144dp);
        dimen117dp = getResources().getDimensionPixelSize(R.dimen.layout_height_117dp);
        dimen108dp = getResources().getDimensionPixelSize(R.dimen.layout_height_108dp);
        dimen48dp = getResources().getDimensionPixelOffset(R.dimen.layout_height_48dp);

        initSwitcherView();
        initOmnibox();
        initSearchPanel();
        //relayoutOK = (Button) findViewById(R.id.main_relayout_ok);
        contentFrame = (FrameLayout) findViewById(R.id.main_content);

        new AdBlock(this); // For AdBlock cold boot
        dispatchIntent(getIntent());

        bottomMenu = (RelativeLayout)findViewById(R.id.rlBottomMenu);
        //bottomMenu.bringToFront();

        TextView tvBottomLeft = (TextView)findViewById(R.id.tvBottomLeft);
        tvBottomLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showOverflow();
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

        MouseModeCtrl mmCtrl = MouseModeCtrl.getInstance(this);
        mmCtrl.appOpenMouseModeAction();
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
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.i(BaseActivity.LOG_TAG,"Browser onResume");
        IntentUnit.setContext(this);
        if (create) {
            return;
        }

        dispatchIntent(getIntent());

        if (IntentUnit.isDBChange()) {
            updateBookmarks();
            updateAutoComplete();
            IntentUnit.setDBChange(false);
        }

        if (IntentUnit.isSPChange()) {
            for (AlbumController controller : BrowserContainer.list()) {
                if (controller instanceof NinjaWebView) {
                    ((NinjaWebView) controller).initPreferences();
                }
            }

            IntentUnit.setSPChange(false);
        }
    }

    private void dispatchIntent(Intent intent) {
        Intent toHolderService = new Intent(this, HolderService.class);
        IntentUnit.setClear(false);
        stopService(toHolderService);

        if (intent != null && intent.hasExtra(IntentUnit.OPEN)) { // From HolderActivity's menu
            pinAlbums(intent.getStringExtra(IntentUnit.OPEN));
        } else if (intent != null && intent.getAction() != null && intent.getAction().equals(Intent.ACTION_WEB_SEARCH)) { // From ActionMode and some others
            pinAlbums(intent.getStringExtra(SearchManager.QUERY));
        } else if (intent != null && filePathCallback != null) {
            filePathCallback = null;
        } else {
            if (showStartPage == true){
                pinAlbums(CUSTOME_HOME_PAGE);
                showStartPage = false;
            } else {
                pinAlbums(null);
            }

//            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//            if (sp.getBoolean(getString(R.string.sp_first), true)) {
//                String lang;
//                if (getResources().getConfiguration().locale.getLanguage().equals("zh")) {
//                    lang = BrowserUnit.INTRODUCTION_ZH;
//                } else {
//                    lang = BrowserUnit.INTRODUCTION_EN;
//                }
//                pinAlbums(BrowserUnit.BASE_URL + lang);
//                sp.edit().putBoolean(getString(R.string.sp_first), false).commit();
//            } else {
//                // default web url  zhangliang
//                pinAlbums("http://www.baidu.com");
//            }
        }
    }

    @Override
    public void onPause() {
        Log.i(BaseActivity.LOG_TAG,"Browser onPause");

        Intent toHolderService = new Intent(this, HolderService.class);
        IntentUnit.setClear(false);
        stopService(toHolderService);

        create = false;
        inputBox.clearFocus();
        if (currentAlbumController != null && currentAlbumController instanceof NinjaRelativeLayout) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
            if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
                DynamicGridView gridView = (DynamicGridView) layout.findViewById(R.id.home_grid);
                if (gridView.isEditMode()) {
                    gridView.stopEditMode();
                    //relayoutOK.setVisibility(View.GONE);
                    omnibox.setVisibility(View.VISIBLE);
                    initHomeGrid(layout, true);
                }
            }
        }

        IntentUnit.setContext(this);
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(BaseActivity.LOG_TAG,"Browser onDestroy");
        holdActivity = null;
        Intent toHolderService = new Intent(this, HolderService.class);
        IntentUnit.setClear(true);
        stopService(toHolderService);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        boolean exit = true;
        if (sp.getBoolean(getString(R.string.sp_clear_quit), false)) {
            Intent toClearService = new Intent(this, ClearService.class);
            startService(toClearService);
            exit = false;
        }

        BrowserContainer.clear();
        IntentUnit.setContext(null);
        super.onDestroy();
        if (exit) {
            System.exit(0); // For remove all WebView thread
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        if (currentAlbumController != null && currentAlbumController instanceof NinjaRelativeLayout) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
            if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
                DynamicGridView gridView = (DynamicGridView) layout.findViewById(R.id.home_grid);
                if (gridView.isEditMode()) {
                    gridView.stopEditMode();
                    //relayoutOK.setVisibility(View.GONE);
                    omnibox.setVisibility(View.VISIBLE);
                }
            }
        }

        hideSoftInput(inputBox);
        hideSearchPanel();
//        if (switcherPanel.getStatus() != SwitcherPanel.Status.EXPANDED) {
//            switcherPanel.expanded();
//        }
        super.onConfigurationChanged(newConfig);

//        float coverHeight = ViewUnit.getWindowHeight(this) - ViewUnit.getStatusBarHeight(this) - dimen108dp - dimen48dp;
//        switcherPanel.setCoverHeight(coverHeight);
//        switcherPanel.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//            @Override
//            public void onGlobalLayout() {
//                switcherPanel.fixKeyBoardShowing(switcherPanel.getHeight());
//                switcherPanel.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//            }
//        });

        if (currentAlbumController != null && currentAlbumController instanceof NinjaRelativeLayout) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
            if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
                initHomeGrid(layout, true);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
//        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
//            // When video fullscreen, just control the sound
//            return !(fullscreenHolder != null || customView != null || videoView != null) && onKeyCodeVolumeUp();
//        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//            // When video fullscreen, just control the sound
//            return !(fullscreenHolder != null || customView != null || videoView != null) && onKeyCodeVolumeDown();
//        } else
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            //return showOverflow();
        } else if (keyCode == KeyEvent.KEYCODE_BACK) {
            // When video fullscreen, first close it
//            if (switcherRootView.getVisibility() == View.VISIBLE){
//                switcherRootView.setVisibility(View.GONE);
//            }

            if (fullscreenHolder != null || customView != null || videoView != null) {
                return onHideCustomView();
            }
            return onKeyCodeBack(true);
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_MENU){
            showOverflow();
            return true;
        }

        // When video fullscreen, just control the sound
//        if (fullscreenHolder != null || customView != null || videoView != null) {
//            if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
//                int vc = Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"));
//                if (vc != 2) {
//                    return true;
//                }
//            }
//        }

//        View rootview = this.getWindow().getDecorView();
//        View focusView = rootview.findFocus();
//        ViewParent parentView = focusView.getParent();
//        int focusViewId = rootview.findFocus().getId();
          Log.i(BaseActivity.LOG_TAG,"keyup keyCode = " + keyCode);
//        Log.i("Ninja","focusViewId = " + focusViewId);
//        Log.i("Ninja","focusView = " + focusView);
//        Log.i("Ninja","parentView = " + parentView);

        return super.onKeyUp(keyCode, event);
    }

    private void initSwitcherView() {
        //switcherRootView = (LinearLayout) findViewById(R.id.switcher_view);
        //switcherRootView.setVisibility(View.GONE);

//        switcherScroller = (HorizontalScrollView) findViewById(R.id.switcher_scroller);
//        switcherContainer = (LinearLayout) findViewById(R.id.switcher_container);
//        switcherSetting = (ImageButton) findViewById(R.id.switcher_setting);
//        switcherBookmarks = (ImageButton) findViewById(R.id.switcher_bookmarks);
//        switcherHistory = (ImageButton) findViewById(R.id.switcher_history);
//        switcherAdd = (ImageButton) findViewById(R.id.switcher_add);
//
//        switcherSetting.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(BrowserActivity.this, SettingActivity.class);
//                startActivity(intent);
//            }
//        });
//
//        switcherBookmarks.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addAlbum(BrowserUnit.FLAG_BOOKMARKS);
//            }
//        });
//
//        switcherHistory.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addAlbum(BrowserUnit.FLAG_HISTORY);
//            }
//        });
//
//        switcherAdd.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                addAlbum(BrowserUnit.FLAG_HOME);
//            }
//        });
    }

    private void initOmnibox() {
        omnibox = (RelativeLayout) findViewById(R.id.main_omnibox);
        inputBox = (MoAutoCompleteTextView) findViewById(R.id.main_omnibox_input);
//        omniboxBookmark = (ImageButton) findViewById(R.id.main_omnibox_bookmark);
//        omniboxRefresh = (ImageButton) findViewById(R.id.main_omnibox_refresh);
//        omniboxOverflow = (ImageButton) findViewById(R.id.main_omnibox_overflow);
        progressBar = (ProgressBar) findViewById(R.id.main_progress_bar);
        ImageButton btnGoto = (ImageButton)findViewById(R.id.btn_goto);

        omnibox.setEnabled(true);
//        inputBox.setOnTouchListener(new SwipeToBoundListener(omnibox, new SwipeToBoundListener.BoundCallback() {
//            private KeyListener keyListener = inputBox.getKeyListener();
//
//            @Override
//            public boolean canSwipe() {
//                Log.i("Ninja", "inputBox setOnTouchListener canSwipe");
//                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(BrowserActivity.this);
//                boolean ob = sp.getBoolean(getString(R.string.sp_omnibox_control), true);
//                return !switcherPanel.isKeyBoardShowing() && ob;
//            }
//
//            @Override
//            public void onSwipe() {
//                Log.i("Ninja", "inputBox setOnTouchListener onSwipe");
//                inputBox.setKeyListener(null);
//                inputBox.setFocusable(false);
//                inputBox.setFocusableInTouchMode(false);
//                inputBox.clearFocus();
//            }
//
//            @Override
//            public void onBound(boolean canSwitch, boolean left) {
//                Log.i("Ninja", "inputBox setOnTouchListener onBound");
//                inputBox.setKeyListener(keyListener);
//                inputBox.setFocusable(true);
//                inputBox.setFocusableInTouchMode(true);
//                inputBox.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
//                inputBox.clearFocus();
//
//                if (canSwitch) {
//                    AlbumController controller = nextAlbumController(left);
//                    showAlbum(controller, false, false, true);
//                    NinjaToast.show(BrowserActivity.this, controller.getAlbumTitle());
//                }
//            }
//        }));

        inputBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                Log.i("Ninja","onEditorAction actionId = " + actionId);
                Log.i("Ninja","onEditorAction KeyEvent = " + event);

                if (currentAlbumController == null) { // || !(actionId == EditorInfo.IME_ACTION_DONE)
                    return false;
                }

                String query = inputBox.getText().toString().trim();
                if (query.isEmpty()) {
                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
                    return true;
                }

                updateAlbum(query);
                hideSoftInput(inputBox);
                return false;
            }
        });

        inputBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = inputBox.getText().toString().trim();
                if (query.isEmpty()) {
                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
                } else {
                    updateAlbum(query);
                    hideSoftInput(inputBox);
                }
            }
        });

        btnGoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String query = inputBox.getText().toString().trim();
                if (query.isEmpty()) {
                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
                } else {
                    updateAlbum(query);
                    hideSoftInput(inputBox);
                }
            }
        });

        updateBookmarks();
        updateAutoComplete();

//        omniboxBookmark.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (!prepareRecord()) {
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_add_bookmark_failed);
//                    return;
//                }
//
//                NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
//                String title = ninjaWebView.getTitle();
//                String url = ninjaWebView.getUrl();
//
//                RecordAction action = new RecordAction(BrowserActivity.this);
//                action.open(true);
//                if (action.checkBookmark(url)) {
//                    action.deleteBookmark(url);
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_delete_bookmark_successful);
//                } else {
//                    action.addBookmark(new Record(title, url, System.currentTimeMillis()));
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_add_bookmark_successful);
//                }
//                action.close();
//
//                updateBookmarks();
//                updateAutoComplete();
//            }
//        });

//        omniboxRefresh.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (currentAlbumController == null) {
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_refresh_failed);
//                    return;
//                }
//
//                if (currentAlbumController instanceof NinjaWebView) {
//                    NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
//                    if (ninjaWebView.isLoadFinish()) {
//                        ninjaWebView.reload();
//                    } else {
//                        ninjaWebView.stopLoading();
//                    }
//                } else if (currentAlbumController instanceof NinjaRelativeLayout) {
//                    final NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
//                    if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
//                        initHomeGrid(layout, true);
//                        return;
//                    }
//                    initBHList(layout, true);
//                } else {
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_refresh_failed);
//                }
//            }
//        });
//
//        omniboxOverflow.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                showOverflow();
//            }
//        });
    }

    private void addToBookMark(){
        if (!prepareRecord()) {
            NinjaToast.show(BrowserActivity.this, R.string.toast_add_bookmark_failed);
            return;
        }

        NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
        String title = ninjaWebView.getTitle();
        String url = ninjaWebView.getUrl();
        BookmarksUtil bkUtil = BookmarksUtil.getInstance(this);

        RecordAction action = new RecordAction(BrowserActivity.this);
        action.open(true);
        if (action.checkBookmark(url)) {
           if (!bkUtil.isDefBookmark(url)){
               action.deleteBookmark(url);
               NinjaToast.show(BrowserActivity.this, R.string.toast_delete_bookmark_successful);
            }
        } else {
            String favFile = bkUtil.getFaviconsIconFile(url);
            action.addBookmark(new Record(title, url, System.currentTimeMillis(), favFile, 0));
            NinjaToast.show(BrowserActivity.this, R.string.toast_add_bookmark_successful);
        }
        action.close();

        updateBookmarks();
        updateAutoComplete();
    }

    private void refreshPage(){
        if (currentAlbumController == null) {
            NinjaToast.show(BrowserActivity.this, R.string.toast_refresh_failed);
            return;
        }

        if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            if (ninjaWebView.isLoadFinish()) {
                ninjaWebView.reload();
            } else {
                ninjaWebView.stopLoading();
            }
        } else if (currentAlbumController instanceof NinjaRelativeLayout) {
            final NinjaRelativeLayout layout = (NinjaRelativeLayout) currentAlbumController;
            if (layout.getFlag() == BrowserUnit.FLAG_HOME) {
                initHomeGrid(layout, true);
                return;
            }
            initBHList(layout, true);
        } else {
            NinjaToast.show(BrowserActivity.this, R.string.toast_refresh_failed);
        }
    }

    private void initHomeGrid(final NinjaRelativeLayout layout, boolean update) {
        if (update) {
            updateProgress(BrowserUnit.PROGRESS_MIN);
        }

        RecordAction action = new RecordAction(this);
        action.open(false);
        final List<GridItem> gridList = action.listGrid();
        action.close();

        DynamicGridView gridView = (DynamicGridView) layout.findViewById(R.id.home_grid);
        TextView aboutBlank = (TextView) layout.findViewById(R.id.home_about_blank);
        gridView.setEmptyView(aboutBlank);

        final GridAdapter gridAdapter;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            gridAdapter = new GridAdapter(this, gridList, 3);
        } else {
            gridAdapter = new GridAdapter(this, gridList, 2);
        }
        gridView.setAdapter(gridAdapter);
        try {
            gridAdapter.notifyDataSetChanged();
        } catch (Exception e){

        }

        /* Wait for gridAdapter.notifyDataSetChanged() */
        if (update) {
            gridView.postDelayed(new Runnable() {
                @Override
                public void run() {
                    layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                    updateProgress(BrowserUnit.PROGRESS_MAX);
                }
            }, shortAnimTime);
        }

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                updateAlbum(gridList.get(position).getURL());
            }
        });

        gridView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                showGridMenu(gridList.get(position));
                return true;
            }
        });
    }

    private void initBHList(final NinjaRelativeLayout layout, boolean update) {
        if (update) {
            updateProgress(BrowserUnit.PROGRESS_MIN);
        }

        RecordAction action = new RecordAction(BrowserActivity.this);
        action.open(false);
        final List<Record> list;
        if (layout.getFlag() == BrowserUnit.FLAG_BOOKMARKS) {
            list = action.listBookmarks();
            Collections.sort(list, new Comparator<Record>() {
                @Override
                public int compare(Record first, Record second) {
                    return first.getTitle().compareTo(second.getTitle());
                }
            });
        } else if (layout.getFlag() == BrowserUnit.FLAG_HISTORY) {
            list = action.listHistory();
        } else {
            list = new ArrayList<>();
        }
        action.close();

        ListView listView = (ListView) layout.findViewById(R.id.record_list);
        TextView textView = (TextView) layout.findViewById(R.id.record_list_empty);
        listView.setEmptyView(textView);

        final RecordAdapter adapter = new RecordAdapter(BrowserActivity.this, R.layout.record_item, list);
        listView.setAdapter(adapter);

        try {
            adapter.notifyDataSetChanged();
        } catch (Exception e){

        }


        /* Wait for adapter.notifyDataSetChanged() */
//        if (update) {
//            listView.postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
//                    updateProgress(BrowserUnit.PROGRESS_MAX);
//                }
//            }, shortAnimTime);
//        }

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("popobrowser", "bookmarks or history item clicked...");
                updateAlbum(list.get(position).getURL());
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                //showListMenu(adapter, list, position);
                return true;
            }
        });
    }

    private void initSearchPanel() {
//        searchPanel = (RelativeLayout) findViewById(R.id.main_search_panel);
//        searchBox = (EditText) findViewById(R.id.main_search_box);
//        searchUp = (ImageButton) findViewById(R.id.main_search_up);
//        searchDown = (ImageButton) findViewById(R.id.main_search_down);
//        searchCancel = (ImageButton) findViewById(R.id.main_search_cancel);
//
//        searchBox.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence s, int start, int before, int count) {
//            }
//
//            @Override
//            public void afterTextChanged(Editable s) {
//                if (currentAlbumController != null && currentAlbumController instanceof NinjaWebView) {
//                    ((NinjaWebView) currentAlbumController).findAllAsync(s.toString());
//                }
//            }
//        });
//
//        searchBox.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            @Override
//            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
//                if (actionId != EditorInfo.IME_ACTION_DONE) {
//                    return false;
//                }
//
//                if (searchBox.getText().toString().isEmpty()) {
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
//                    return true;
//                }
//                return false;
//            }
//        });
//
//        searchUp.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String query = searchBox.getText().toString();
//                if (query.isEmpty()) {
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
//                    return;
//                }
//
//                hideSoftInput(searchBox);
//                if (currentAlbumController instanceof NinjaWebView) {
//                    ((NinjaWebView) currentAlbumController).findNext(false);
//                }
//            }
//        });
//
//        searchDown.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String query = searchBox.getText().toString();
//                if (query.isEmpty()) {
//                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
//                    return;
//                }
//
//                hideSoftInput(searchBox);
//                if (currentAlbumController instanceof NinjaWebView) {
//                    ((NinjaWebView) currentAlbumController).findNext(true);
//                }
//            }
//        });
//
//        searchCancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                hideSearchPanel();
//            }
//        });
    }

    private synchronized void addAlbum(int flag) {
        final AlbumController holder;
        if (flag == BrowserUnit.FLAG_BOOKMARKS) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.record_list, null, false);
            layout.setBrowserController(this);
            layout.setFlag(BrowserUnit.FLAG_BOOKMARKS);
            layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            layout.setAlbumTitle(getString(R.string.album_title_bookmarks));
            layout.setFocusable(true);
            holder = layout;
            initBHList(layout, false);
        } else if (flag == BrowserUnit.FLAG_HISTORY) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.record_list, null, false);
            layout.setBrowserController(this);
            layout.setFlag(BrowserUnit.FLAG_HISTORY);
            layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            layout.setAlbumTitle(getString(R.string.album_title_history));
            layout.setFocusable(true);
            holder = layout;
            initBHList(layout, false);
        } else if (flag == BrowserUnit.FLAG_HOME) {
            NinjaRelativeLayout layout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.home, null, false);
            layout.setBrowserController(this);
            layout.setFlag(BrowserUnit.FLAG_HOME);
            layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            layout.setAlbumTitle(getString(R.string.album_title_home));
            holder = layout;
            initHomeGrid(layout, true);
            return;
        } else {
            return;
        }

        final View albumView = holder.getAlbumView();
        albumView.setVisibility(View.INVISIBLE);

        BrowserContainer.add(holder);
        showAlbum(holder, false, true, true);
//        switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//
//        Animation animation = AnimationUtils.loadAnimation(this, R.anim.album_slide_in_up);
//        animation.setAnimationListener(new Animation.AnimationListener() {
//            @Override
//            public void onAnimationRepeat(Animation animation) {
//            }
//
//            @Override
//            public void onAnimationStart(Animation animation) {
//                albumView.setVisibility(View.VISIBLE);
//            }
//
//            @Override
//            public void onAnimationEnd(Animation animation) {
//                showAlbum(holder, false, true, true);
//            }
//        });
//        albumView.startAnimation(animation);
    }

    private synchronized void addAlbum(String title, final String url, final boolean foreground, final Message resultMsg) {
        final NinjaWebView webView = new NinjaWebView(this);
        webView.setBrowserController(this);
        webView.setFlag(BrowserUnit.FLAG_NINJA);
        webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
        webView.setAlbumTitle(title);
        ViewUnit.bound(this, webView);

        final View albumView = webView.getAlbumView();
        if (currentAlbumController != null && (currentAlbumController instanceof NinjaWebView) && resultMsg != null) {
            int index = BrowserContainer.indexOf(currentAlbumController) + 1;
            BrowserContainer.add(webView, index);
//            switcherContainer.addView(albumView, index, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT));
        } else {
            BrowserContainer.add(webView);
//            switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        }

        if (!foreground) {
            ViewUnit.bound(this, webView);
            webView.loadUrl(url);
            webView.deactivate();

            albumView.setVisibility(View.VISIBLE);
//            if (currentAlbumController != null) {
//                switcherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
//            }
            return;
        }

        albumView.setVisibility(View.INVISIBLE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.album_slide_in_up);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationRepeat(Animation animation) {
            }

            @Override
            public void onAnimationStart(Animation animation) {
                albumView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {
                showAlbum(webView, false, true, false);

                if (url != null && !url.isEmpty()) {
                    webView.loadUrl(url);
                } else if (resultMsg != null) {
                    WebView.WebViewTransport transport = (WebView.WebViewTransport) resultMsg.obj;
                    transport.setWebView(webView);
                    resultMsg.sendToTarget();
                }
            }
        });
        albumView.startAnimation(animation);
    }

//    public String getImei() {
//        return SystemHelper.getPhoneInfo(this).getImei();
//    }
//
//    public String getImsi() {
//        return SystemHelper.getPhoneInfo(this).getImsi();
//    }
//
//    public String getWifiMac() {
//        String wmac = SystemHelper.getWifiMac(this);
//        return "02:00:00:00:00:00".equals(wmac) ? null : wmac;
//    }

//    public JSONObject getDeviceInfoAsJson() {
//        JSONObject json = new JSONObject();
//        try {
//            int sw = SystemHelper.getScreenWidth(this);
//            int sh = SystemHelper.getScreenHeight(this);
//
//            json.put("app", SystemHelper.getAppId(this));
//            json.put("ch", "");
//            json.put("app_v", SystemHelper.getAppVersion(this));
//            if (getImsi() != null) json.put("imsi", getImsi());
//            if (getImei() != null) json.put("imei", getImei());
//            json.put("adid", adid == null ? "" : adid);
//            json.put("ua", SystemHelper.getUA(false));
//            json.put("os", "android");
//            json.put("os_v", SystemHelper.getOsVersion());
//            if (getWifiMac() != null) json.put("wmac", getWifiMac());
//            json.put("sn", SystemHelper.getAndroidId(this));
//            json.put("sa", SystemHelper.isSystemApp(this));
//            json.put("sw", sw);
//            json.put("sh", sh);
//            json.put("sd", SystemHelper.getScreenDensity(this));
//            json.put("lang", Locale.getDefault().toString());
//            json.put("country", SystemHelper.getCountry(this));
//            json.put("net", SystemHelper.getNetworkType(this));
//            json.put("roaming", SystemHelper.isRoaming(this));
//
//            if (ipcountry != null){
//                json.put("ipcountry", ipcountry);
//            }
//        } catch(Exception e){
//            Log.e("error", e.toString());
//        }
//        return json;
//    }

//    private Map<String, String> getHomePageExtraHeaders(){
//        if (extraHeaders == null){
//            extraHeaders = new HashMap<String, String>();
//
//            try {
//                JSONObject devInfo = getDeviceInfoAsJson();
//                if (devInfo != null){
//                    String devStr = devInfo.toString();
//                    byte[] bs  = Base64.encode(devStr.getBytes(), Base64.DEFAULT);
//                    String data = new String(bs);
//                    extraHeaders.put("X-Requested-With", data);
//                }
//            } catch (Exception e){
//
//            }
//        }
//
//        return extraHeaders;
//    }

    private synchronized void pinAlbums(String url) {
        hideSoftInput(inputBox);
        hideSearchPanel();
//        switcherContainer.removeAllViews();

        for (AlbumController controller : BrowserContainer.list()) {
            if (controller instanceof NinjaWebView) {
                ((NinjaWebView) controller).setBrowserController(this);
            } else if (controller instanceof NinjaRelativeLayout) {
                ((NinjaRelativeLayout) controller).setBrowserController(this);
            }
//            switcherContainer.addView(controller.getAlbumView(), LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.MATCH_PARENT);
            controller.getAlbumView().setVisibility(View.VISIBLE);
            controller.deactivate();
        }

//        if (BrowserContainer.size() < 1 && url == null) {
//            addAlbum(BrowserUnit.FLAG_HOME);
//        } else
        if (BrowserContainer.size() >= 1 && url == null) {
            if (currentAlbumController != null) {
                currentAlbumController.activate();
                return;
            }

            int index = BrowserContainer.size() - 1;
            currentAlbumController = BrowserContainer.get(index);
            contentFrame.removeAllViews();
            contentFrame.addView((View) currentAlbumController);
            currentAlbumController.activate();

            updateOmnibox();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    switcherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
                    currentAlbumController.setAlbumCover(ViewUnit.capture(((View) currentAlbumController), dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                }
            }, shortAnimTime);
        } else { // When url != null
            NinjaWebView webView = new NinjaWebView(this);
            webView.setBrowserController(this);
            webView.setFlag(BrowserUnit.FLAG_NINJA);
            webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            webView.setAlbumTitle(getString(R.string.album_untitled));
            ViewUnit.bound(this, webView);

            if (url != null){
                if (url.equals(DEFAULT_HOME_PAGE)){
                    //Map<String, String> expHeader = getHomePageExtraHeaders();
                    //webView.setExtraHeader(expHeader);
                    //webView.setExtraHeaderUrl(url);

                    String params = "";
                    String appid = SystemHelper.getAppId(mContext);
                    String key = SystemHelper.getAppMeta(mContext, "mocean.key", "");
                    String ch = SystemHelper.getAppMeta(mContext, "mocean.channel", "");
                    params = "/?token=" + key + "&channel=" + ch;
                    String paramUrl = url + params;

                    webView.setExtraHeaderUrl(paramUrl);

                    webView.loadUrl(paramUrl);
                } else {
                    webView.loadUrl(url);
                }
            }

            BrowserContainer.add(webView);
            final View albumView = webView.getAlbumView();
            albumView.setVisibility(View.VISIBLE);
//            switcherContainer.addView(albumView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            contentFrame.removeAllViews();
            contentFrame.addView(webView);

            if (currentAlbumController != null) {
                currentAlbumController.deactivate();
            }
            currentAlbumController = webView;
            currentAlbumController.activate();

            updateOmnibox();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
//                    switcherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
                    currentAlbumController.setAlbumCover(ViewUnit.capture(((View) currentAlbumController), dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                }
            }, shortAnimTime);

            if (url == null) {
                inputBox.setFocusable(true);
                inputBox.setFocusableInTouchMode(true);
                inputBox.requestFocus();
                inputBox.requestFocusFromTouch();
            }
        }
    }

    @Override
    public synchronized void showAlbum(AlbumController controller, boolean anim, final boolean expand, final boolean capture) {
        if (controller == null || controller == currentAlbumController) {
//            switcherPanel.expanded();
            return;
        }

        if (currentAlbumController != null && anim) {
            currentAlbumController.deactivate();
            final View rv = (View) currentAlbumController;
            final View av = (View) controller;

            Animation fadeOut = AnimationUtils.loadAnimation(this, R.anim.album_fade_out);
            fadeOut.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationRepeat(Animation animation) {}

                @Override
                public void onAnimationEnd(Animation animation) {}

                @Override
                public void onAnimationStart(Animation animation) {
                    contentFrame.removeAllViews();
                    contentFrame.addView(av);
                }
            });
            rv.startAnimation(fadeOut);
        } else {
            if (currentAlbumController != null) {
                currentAlbumController.deactivate();
            }
            contentFrame.removeAllViews();
            contentFrame.addView((View) controller);
        }

        currentAlbumController = controller;
        currentAlbumController.activate();
//        switcherScroller.smoothScrollTo(currentAlbumController.getAlbumView().getLeft(), 0);
        updateOmnibox();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (expand) {
//                    switcherPanel.expanded();
                }

                if (capture) {
                    currentAlbumController.setAlbumCover(ViewUnit.capture(((View) currentAlbumController), dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
                }
            }
        }, shortAnimTime);
    }

    private synchronized void updateAlbum() {
        if (currentAlbumController == null) {
            return;
        }

        NinjaRelativeLayout layout = (NinjaRelativeLayout) getLayoutInflater().inflate(R.layout.home, null, false);
        layout.setBrowserController(this);
        layout.setFlag(BrowserUnit.FLAG_HOME);
        layout.setAlbumCover(ViewUnit.capture(layout, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
        layout.setAlbumTitle(getString(R.string.album_title_home));
        initHomeGrid(layout, true);

//        int index = switcherContainer.indexOfChild(currentAlbumController.getAlbumView());
        currentAlbumController.deactivate();
//        switcherContainer.removeView(currentAlbumController.getAlbumView());
        contentFrame.removeAllViews(); ///

//        switcherContainer.addView(layout.getAlbumView(), index, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        contentFrame.addView(layout);
//        BrowserContainer.set(layout, index);
        currentAlbumController = layout;
        updateOmnibox();
    }

    private synchronized void updateAlbum(String url) {
        if (currentAlbumController == null) {
            return;
        }

        if (currentAlbumController instanceof NinjaWebView) {
            ((NinjaWebView) currentAlbumController).loadUrl(url);
            updateOmnibox();
        } else if (currentAlbumController instanceof NinjaRelativeLayout) {
            hideSoftInput(inputBox);
            NinjaWebView webView = new NinjaWebView(this);
            webView.setBrowserController(this);
            webView.setFlag(BrowserUnit.FLAG_NINJA);
            webView.setAlbumCover(ViewUnit.capture(webView, dimen144dp, dimen108dp, false, Bitmap.Config.RGB_565));
            webView.setAlbumTitle(getString(R.string.album_untitled));
            ViewUnit.bound(this, webView);

//            int index = switcherContainer.indexOfChild(currentAlbumController.getAlbumView());
            currentAlbumController.deactivate();
//            switcherContainer.removeView(currentAlbumController.getAlbumView());
            contentFrame.removeAllViews(); ///

//            switcherContainer.addView(webView.getAlbumView(), index, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            contentFrame.addView(webView);
//            BrowserContainer.set(webView, index);
            currentAlbumController = webView;
            webView.activate();

            webView.loadUrl(url);
            updateOmnibox();
        } else {
            NinjaToast.show(this, R.string.toast_load_error);
        }
    }

    @Override
    public synchronized void removeAlbum(AlbumController controller) {
        if (currentAlbumController == null || BrowserContainer.size() <= 1) {
//            switcherContainer.removeView(controller.getAlbumView());
            BrowserContainer.remove(controller);
            addAlbum(BrowserUnit.FLAG_HOME);
            return;
        }

        if (controller != currentAlbumController) {
//            switcherContainer.removeView(controller.getAlbumView());
            BrowserContainer.remove(controller);
        } else {
//            switcherContainer.removeView(controller.getAlbumView());
            int index = BrowserContainer.indexOf(controller);
            BrowserContainer.remove(controller);
            if (index >= BrowserContainer.size()) {
                index = BrowserContainer.size() - 1;
            }
            showAlbum(BrowserContainer.get(index), false, false, false);
        }
    }

    @Override
    public void updateAutoComplete() {
        RecordAction action = new RecordAction(this);
        action.open(false);
        List<Record> list = action.listBookmarks();
        list.addAll(action.listHistory());
        action.close();

        final CompleteAdapter adapter = new CompleteAdapter(this, R.layout.complete_item, list);
        inputBox.setAdapter(adapter);
        try {
            adapter.notifyDataSetChanged();
        } catch (Exception e){

        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            inputBox.setDropDownVerticalOffset(getResources().getDimensionPixelOffset(R.dimen.layout_height_6dp));
        }
        inputBox.setDropDownWidth(ViewUnit.getWindowWidth(this));
        inputBox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Ninja", "inputBox onItemClick");

                String url = ((TextView) view.findViewById(R.id.complete_item_url)).getText().toString();
                inputBox.setText(Html.fromHtml(BrowserUnit.urlWrapper(url)), EditText.BufferType.SPANNABLE);
                inputBox.setSelection(url.length());
                updateAlbum(url);
                hideSoftInput(inputBox);
            }
        });

        inputBox.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.i("Ninja", "inputBox item change : " + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.i("Ninja", "inputBox item onNothingSelected ");
            }
        });

        inputBox.setListKeyBoardClickedListener(new MoAutoCompleteTextView.IPopListItemClickedListener() {
            @Override
            public void onClicked(int pos) {
                String url = adapter.getSelectItemUrl(pos);
                if (url != null){
                    inputBox.setText(Html.fromHtml(BrowserUnit.urlWrapper(url)), EditText.BufferType.SPANNABLE);
                    inputBox.setSelection(url.length());
                    updateAlbum(url);
                    hideSoftInput(inputBox);
                }
            }
        });
    }

    @Override
    public void updateBookmarks() {
//        if (currentAlbumController == null || !(currentAlbumController instanceof NinjaWebView)) {
////            omniboxBookmark.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.bookmark_selector_dark));
//            return;
//        }

//        RecordAction action = new RecordAction(this);
//        action.open(false);
//        String url = ((NinjaWebView) currentAlbumController).getUrl();

//        if (action.checkBookmark(url)) {
//            omniboxBookmark.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.bookmark_selector_blue));
//        } else {
//            omniboxBookmark.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.bookmark_selector_dark));
//        }

//        action.close();
    }

    @Override
    public void updateInputBox(String query) {
        if (query != null) {
            inputBox.setText(Html.fromHtml(BrowserUnit.urlWrapper(query)), EditText.BufferType.SPANNABLE);
        } else {
            inputBox.setText(null);
        }
        inputBox.clearFocus();
    }

    private void updateOmnibox() {
        if (currentAlbumController == null) {
            return;
        }

        if (currentAlbumController instanceof NinjaRelativeLayout) {
            updateProgress(BrowserUnit.PROGRESS_MAX);
            updateBookmarks();
            updateInputBox(null);
        } else if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            updateProgress(ninjaWebView.getProgress());
            updateBookmarks();
            if (ninjaWebView.getUrl() == null && ninjaWebView.getOriginalUrl() == null) {
                updateInputBox(null);
            } else if (ninjaWebView.getUrl() != null) {
                updateInputBox(ninjaWebView.getUrl());
            } else {
                updateInputBox(ninjaWebView.getOriginalUrl());
            }
            if (bottomMenu != null) {
                bottomMenu.setVisibility(View.VISIBLE);
                //bottomMenu.bringToFront();
            }
        }
    }

    @Override
    public synchronized void updateProgress(int progress) {
        if (progress > progressBar.getProgress()) {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", progress);
            animator.setDuration(shortAnimTime);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        } else if (progress < progressBar.getProgress()) {
            ObjectAnimator animator = ObjectAnimator.ofInt(progressBar, "progress", 0, progress);
            animator.setDuration(shortAnimTime);
            animator.setInterpolator(new DecelerateInterpolator());
            animator.start();
        }

        updateBookmarks();
        if (progress < BrowserUnit.PROGRESS_MAX) {
            updateRefresh(true);
            progressBar.setVisibility(View.VISIBLE);
        } else {
            updateRefresh(false);
            progressBar.setVisibility(View.GONE);
        }
    }

    private void updateRefresh(boolean running) {
//        if (running) {
//            omniboxRefresh.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.cl_selector_dark));
//        } else {
//            omniboxRefresh.setImageDrawable(ViewUnit.getDrawable(this, R.drawable.refresh_selector));
//        }
    }

    /***
     *  file chooer
     */
    private Intent createChooserIntent(Intent... intents) {
        Intent chooser = new Intent(Intent.ACTION_CHOOSER);
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents);
        chooser.putExtra(Intent.EXTRA_TITLE,
                getResources().getString(R.string.dialog_content_upload));
        return chooser;
    }

    private Intent createOpenableIntent(String type) {
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType(type);
        return i;
    }

    private Intent createCameraIntent() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File externalDataDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM);
        File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
                File.separator + "browser-photos");
        cameraDataDir.mkdirs();
        String mCameraFilePath = cameraDataDir.getAbsolutePath() + File.separator +
                System.currentTimeMillis() + ".jpg";
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(new File(mCameraFilePath)));
        return cameraIntent;
    }

    private Intent createCamcorderIntent() {
        return new Intent(MediaStore.ACTION_VIDEO_CAPTURE);
    }

    private Intent createSoundRecorderIntent() {
        return new Intent(MediaStore.Audio.Media.RECORD_SOUND_ACTION);
    }

    private Intent createDefaultOpenableIntent() {
        // Create and return a chooser with the default OPENABLE
        // actions including the camera, camcorder and sound
        // recorder where available.
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");

        Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
                createSoundRecorderIntent());
        chooser.putExtra(Intent.EXTRA_INTENT, i);
        return chooser;
    }

    private void openImageChooserActivity() {
        final String imageMimeType = "image/*";
        final String videoMimeType = "video/*";
        final String audioMimeType = "audio/*";
        final String mediaSourceKey = "capture";
        final String mediaSourceValueCamera = "camera";
        final String mediaSourceValueFileSystem = "filesystem";
        final String mediaSourceValueCamcorder = "camcorder";
        final String mediaSourceValueMicrophone = "microphone";

        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("*/*");

//        Intent chooser = createChooserIntent(createCameraIntent(), createCamcorderIntent(),
//                createSoundRecorderIntent());

        Intent chooser = createChooserIntent(createCameraIntent());
        chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));

        //chooser.putExtra(Intent.EXTRA_INTENT, i);

        startActivityForResult(chooser, IntentUnit.REQUEST_FILE_16);
        Log.i(BaseActivity.LOG_TAG, "========== open file chooser ");
    }


    private String mCameraFilePath;
    @Override
    public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
        // Because Activity launchMode is singleInstance,
        // so we can not get result from onActivityResult when Android 4.X,
        // what a pity

        final String imageMimeType = "image/*";
        final String videoMimeType = "video/*";
        final String audioMimeType = "audio/*";
        final String mediaSourceKey = "capture";
        final String mediaSourceValueCamera = "camera";
        final String mediaSourceValueFileSystem = "filesystem";
        final String mediaSourceValueCamcorder = "camcorder";
        final String mediaSourceValueMicrophone = "microphone";

        // According to the spec, media source can be 'filesystem' or 'camera' or 'camcorder'
        // or 'microphone' and the default value should be 'filesystem'.
        String mediaSource = mediaSourceValueFileSystem;

//        if (this.uploadMsg != null) {
//            // Already a file picker operation in progress.
//            return;
//        }
        this.uploadMsg = uploadMsg;

        // Parse the accept type.
        String params[] = acceptType.split(";");
        String mimeType = params[0];

        if (capture.length() > 0) {
            mediaSource = capture;
        }

        if (capture.equals(mediaSourceValueFileSystem)) {
            // To maintain backwards compatibility with the previous implementation
            // of the media capture API, if the value of the 'capture' attribute is
            // "filesystem", we should examine the accept-type for a MIME type that
            // may specify a different capture value.
            for (String p : params) {
                String[] keyValue = p.split("=");
                if (keyValue.length == 2) {
                    // Process key=value parameters.
                    if (mediaSourceKey.equals(keyValue[0])) {
                        mediaSource = keyValue[1];
                    }
                }
            }
        }

        //Ensure it is not still set from a previous upload.
        mCameraFilePath = null;

        if (mimeType.equals(imageMimeType)) {
            if (mediaSource.equals(mediaSourceValueCamera)) {
                // Specified 'image/*' and requested the camera, so go ahead and launch the
                // camera directly.
                startActivity(createCameraIntent());
                return;
            } else {
                // Specified just 'image/*', capture=filesystem, or an invalid capture parameter.
                // In all these cases we show a traditional picker filetered on accept type
                // so launch an intent for both the Camera and image/* OPENABLE.
                Intent chooser = createChooserIntent(createCameraIntent());
                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(imageMimeType));
                startFileActivity(chooser);
                return;
            }
        } else if (mimeType.equals(videoMimeType)) {
            if (mediaSource.equals(mediaSourceValueCamcorder)) {
                // Specified 'video/*' and requested the camcorder, so go ahead and launch the
                // camcorder directly.
                startFileActivity(createCamcorderIntent());
                return;
            } else {
                // Specified just 'video/*', capture=filesystem or an invalid capture parameter.
                // In all these cases we show an intent for the traditional file picker, filtered
                // on accept type so launch an intent for both camcorder and video/* OPENABLE.
                Intent chooser = createChooserIntent(createCamcorderIntent());
                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(videoMimeType));
                startFileActivity(chooser);
                return;
            }
        } else if (mimeType.equals(audioMimeType)) {
            if (mediaSource.equals(mediaSourceValueMicrophone)) {
                // Specified 'audio/*' and requested microphone, so go ahead and launch the sound
                // recorder.
                startFileActivity(createSoundRecorderIntent());
                return;
            } else {
                // Specified just 'audio/*',  capture=filesystem of an invalid capture parameter.
                // In all these cases so go ahead and launch an intent for both the sound
                // recorder and audio/* OPENABLE.
                Intent chooser = createChooserIntent(createSoundRecorderIntent());
                chooser.putExtra(Intent.EXTRA_INTENT, createOpenableIntent(audioMimeType));
                startFileActivity(chooser);
                return;
            }
        }

        // No special handling based on the accept type was necessary, so trigger the default
        // file upload chooser.
        startFileActivity(createDefaultOpenableIntent());
    }

    private void startFileActivity(Intent intent) {
        Log.i(BaseActivity.LOG_TAG, "========== open file chooser ");
        try {
            startActivityForResult(intent, IntentUnit.REQUEST_FILE_16);
        } catch (ActivityNotFoundException e) {
            // No installed app was able to handle the intent that
            // we sent, so fallback to the default file upload control.
            try {
                //mCaughtActivityNotFoundException = true;
                startActivityForResult(createDefaultOpenableIntent(), IntentUnit.REQUEST_FILE_16);
            } catch (ActivityNotFoundException e2) {
                // Nothing can return us a file, so file upload is effectively disabled.
//                Toast.makeText(mContext, R.string.uploads_disabled,
//                        Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    public void showFileChooser(ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            this.filePathCallback = filePathCallback;
            try {
                Intent intent = fileChooserParams.createIntent();
                startActivityForResult(intent, IntentUnit.REQUEST_FILE_21);
            } catch (Exception e) {
                NinjaToast.show(this, R.string.toast_open_file_manager_failed);
            }
        }
    }

    @Override
    public void onCreateView(WebView view, final Message resultMsg) {
        if (resultMsg == null) {
            return;
        }
//        switcherPanel.collapsed();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                addAlbum(getString(R.string.album_untitled), null, true, resultMsg);
            }
        }, shortAnimTime);
    }

    @Override
    public boolean onShowCustomView(View view, int requestedOrientation, WebChromeClient.CustomViewCallback callback) {
        return onShowCustomView(view, callback);
    }

    @Override
    public boolean onShowCustomView(View view, WebChromeClient.CustomViewCallback callback) {
        if (view == null) {
            return false;
        }
        if (customView != null && callback != null) {
            callback.onCustomViewHidden();
            return false;
        }

        customView = view;
        originalOrientation = getRequestedOrientation();

        fullscreenHolder = new FullscreenHolder(this);
        fullscreenHolder.addView(
                customView,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        decorView.addView(
                fullscreenHolder,
                new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT,
                        FrameLayout.LayoutParams.MATCH_PARENT
                ));

        customView.setKeepScreenOn(true);
        ((View) currentAlbumController).setVisibility(View.GONE);
        setCustomFullscreen(true);

        if (view instanceof FrameLayout) {
            if (((FrameLayout) view).getFocusedChild() instanceof VideoView) {
                videoView = (VideoView) ((FrameLayout) view).getFocusedChild();
                videoView.setOnErrorListener(new VideoCompletionListener());
                videoView.setOnCompletionListener(new VideoCompletionListener());
            }
        }
        customViewCallback = callback;
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE); // Auto landscape when video shows

        return true;
    }

    @Override
    public boolean onHideCustomView() {
        if (customView == null || customViewCallback == null || currentAlbumController == null) {
            return false;
        }

        FrameLayout decorView = (FrameLayout) getWindow().getDecorView();
        if (decorView != null) {
            decorView.removeView(fullscreenHolder);
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            try {
                customViewCallback.onCustomViewHidden();
            } catch (Throwable t) {}
        }

        customView.setKeepScreenOn(false);
        ((View) currentAlbumController).setVisibility(View.VISIBLE);
        setCustomFullscreen(false);

        fullscreenHolder = null;
        customView = null;
        if (videoView != null) {
            videoView.setOnErrorListener(null);
            videoView.setOnCompletionListener(null);
            videoView = null;
        }
        setRequestedOrientation(originalOrientation);

        return true;
    }

    @Override
    public void onLongPress(String url) {
        WebView.HitTestResult result;
        if (!(currentAlbumController instanceof NinjaWebView)) {
            return;
        }
        result = ((NinjaWebView) currentAlbumController).getHitTestResult();

        final List<String> list = new ArrayList<>();
        list.add(getString(R.string.main_menu_new_tab));
        list.add(getString(R.string.main_menu_copy_link));
        if (result != null && (result.getType() == WebView.HitTestResult.IMAGE_TYPE || result.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE)) {
            list.add(getString(R.string.main_menu_save));
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_list, null, false);
        builder.setView(layout);

        ListView listView = (ListView) layout.findViewById(R.id.dialog_list);
        DialogAdapter adapter = new DialogAdapter(this, R.layout.dialog_text_item, list);
        listView.setAdapter(adapter);
        try {
            adapter.notifyDataSetChanged();
        } catch (Exception e){

        }

        final AlertDialog dialog = builder.create();
        if (url != null || (result != null && result.getExtra() != null)) {
            if (url == null) {
                url = result.getExtra();
            }
            dialog.show();
        }

        final String target = url;
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = list.get(position);
                if (s.equals(getString(R.string.main_menu_new_tab))) { // New tab
                    addAlbum(getString(R.string.album_untitled), target, false, null);
                    NinjaToast.show(BrowserActivity.this, R.string.toast_new_tab_successful);
                } else if (s.equals(getString(R.string.main_menu_copy_link))) { // Copy link
                    BrowserUnit.copyURL(BrowserActivity.this, target);
                } else if (s.equals(getString(R.string.main_menu_save))) { // Save
                    BrowserUnit.download(BrowserActivity.this, target, target, BrowserUnit.MIME_TYPE_IMAGE);
                }

                dialog.hide();
                dialog.dismiss();
            }
        });
    }

    private boolean onKeyCodeVolumeUp() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int vc = Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"));

        if (vc == 0) { // Switch tabs
//            if (switcherPanel.isKeyBoardShowing()) {
//                return true;
//            }

            AlbumController controller = nextAlbumController(false);
            showAlbum(controller, false, false, true);
            NinjaToast.show(this, controller.getAlbumTitle());

            return true;
        } else if (vc == 1 && currentAlbumController instanceof NinjaWebView) { // Scroll webpage
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            int height = ninjaWebView.getMeasuredHeight();
            int scrollY = ninjaWebView.getScrollY();
            int distance = Math.min(height, scrollY);

            ObjectAnimator anim = ObjectAnimator.ofInt(ninjaWebView, "scrollY", scrollY, scrollY - distance);
            anim.setDuration(mediumAnimTime);
            anim.start();

            return true;
        }

        return false;
    }

    private boolean onKeyCodeVolumeDown() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        int vc = Integer.valueOf(sp.getString(getString(R.string.sp_volume), "1"));

        if (vc == 0) { // Switch tabs
//            if (switcherPanel.isKeyBoardShowing()) {
//                return true;
//            }

            AlbumController controller = nextAlbumController(true);
            showAlbum(controller, false, false, true);
            NinjaToast.show(this, controller.getAlbumTitle());

            return true;
        } else if (vc == 1 && currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            int height = ninjaWebView.getMeasuredHeight();
            int scrollY = ninjaWebView.getScrollY();
            int surplus = (int) (ninjaWebView.getContentHeight() * ViewUnit.getDensity(this) - height - scrollY);
            int distance = Math.min(height, surplus);

            ObjectAnimator anim = ObjectAnimator.ofInt(ninjaWebView, "scrollY", scrollY, scrollY + distance);
            anim.setDuration(mediumAnimTime);
            anim.start();

            return true;
        }

        return false;
    }

    private boolean onKeyCodeBack(boolean douQ) {
        hideSoftInput(inputBox);
//        if (switcherPanel.getStatus() != SwitcherPanel.Status.EXPANDED) {
//            switcherPanel.expanded();
//        } else
        if (currentAlbumController == null) {
            safelyFinish();
        } else if (currentAlbumController instanceof NinjaWebView) {
            NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
            if (ninjaWebView.canGoBack()) {
                ninjaWebView.goBack();
            } else {
                updateAlbum();
            }
        } else if (currentAlbumController instanceof NinjaRelativeLayout) {
            switch (currentAlbumController.getFlag()) {
                case BrowserUnit.FLAG_BOOKMARKS:
                    updateAlbum();
                    break;
                case BrowserUnit.FLAG_HISTORY:
                    updateAlbum();
                    break;
                case BrowserUnit.FLAG_HOME:
                    if (douQ) {
                        doubleTapsQuit();
                    }
                    break;
                default:
                    safelyFinish();
                    break;
            }
        } else {
            safelyFinish();
        }

        return true;
    }

    private void doubleTapsQuit() {
        final Timer timer = new Timer();
        if (!quit) {
            quit = true;
            NinjaToast.show(this, R.string.toast_double_taps_quit);
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    quit = false;
                    timer.cancel();
                }
            }, DOUBLE_TAPS_QUIT_DEFAULT);
        } else {
            exitTurnOffMouseMode();
            timer.cancel();
            safelyFinish();
        }
    }

    private void safelyFinish(){
        exitTurnOffMouseMode();
        finish();
    }

    private void exitTurnOffMouseMode(){
        MouseModeCtrl mmCtrl = MouseModeCtrl.getInstance(this);
        mmCtrl.closeMouseModeWhenBGOrExit();
    }

    private void hideSoftInput(View view) {
        view.clearFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    private void showSoftInput(View view) {
        view.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void hideSearchPanel() {
//        hideSoftInput(searchBox);
//        searchBox.setText("");
//        searchPanel.setVisibility(View.GONE);
        omnibox.setVisibility(View.VISIBLE);
    }

    private void showSearchPanel() {
        omnibox.setVisibility(View.GONE);
//        searchPanel.setVisibility(View.VISIBLE);
//        showSoftInput(searchBox);
    }
    private boolean demoMouseMode = false;
    private boolean getDeviceMouseMode(){
        MouseModeCtrl mmCtrl = MouseModeCtrl.getInstance(this);
        demoMouseMode = mmCtrl.getMouseMode();
        return demoMouseMode;
    }

    private void switchMouseMode(){
        boolean mode = getDeviceMouseMode();
        MouseModeCtrl mmCtrl = MouseModeCtrl.getInstance(this);
        if (mode == true){
            demoMouseMode = false;
            mmCtrl.setMouseMode(demoMouseMode);
            String toastStr = getResources().getString(R.string.mouse_mode_off) + " : " + getResources().getString(R.string.action_successful);
            NinjaToast.show(BrowserActivity.this, toastStr);
        } else {
            demoMouseMode = true;
            mmCtrl.setMouseMode(demoMouseMode);
            String toastStr = getResources().getString(R.string.mouse_mode_on) + " : " + getResources().getString(R.string.action_successful);
            NinjaToast.show(BrowserActivity.this, toastStr);
        }
    }

    private void goToHomePage(){
        updateAlbum(CUSTOME_HOME_PAGE);
    }

    private boolean showOverflow() {
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_list, null, false);
        builder.setView(layout);

        final String[] array = getResources().getStringArray(R.array.main_overflow);
        if (currentAlbumController != null && currentAlbumController instanceof NinjaWebView) {
            RecordAction action = new RecordAction(this);
            action.open(false);
            String url = ((NinjaWebView) currentAlbumController).getUrl();
            BookmarksUtil bkUtil = BookmarksUtil.getInstance(this);
            if (action.checkBookmark(url) && !bkUtil.isDefBookmark(url)) {
                if (array != null && array.length > 4){
                    String removeBookmark = getResources().getString(R.string.remove_bookmark);
                    array[2] = removeBookmark;
                }
            }
            action.close();
        }


        // For Mouse Mode
//        if (array != null && array.length > 5){
//            boolean mouseMode = getDeviceMouseMode();
//            if (mouseMode == true){
//                String offMode = getResources().getString(R.string.mouse_mode_off);
//                array[3] = offMode;
//            } else {
//                String onMode = getResources().getString(R.string.mouse_mode_on);
//                array[3] = onMode;
//            }
//        }


        final List<String> stringList = new ArrayList<>();
        stringList.addAll(Arrays.asList(array));
//        if (currentAlbumController != null && currentAlbumController instanceof NinjaRelativeLayout) {
//            //stringList.remove(array[0]); // Go to top
//            stringList.remove(array[1]); // Add to home
//            stringList.remove(array[2]); // Find in page
//            //stringList.remove(array[3]); // Screenshot
//            //stringList.remove(array[4]); // Readability
//            //stringList.remove(array[5]); // Share
//
//            NinjaRelativeLayout ninjaRelativeLayout = (NinjaRelativeLayout) currentAlbumController;
//            if (ninjaRelativeLayout.getFlag() != BrowserUnit.FLAG_HOME) {
//                stringList.remove(array[6]); // Relayout
//            }
//        } else if (currentAlbumController != null && currentAlbumController instanceof NinjaWebView) {
//            if (!sp.getBoolean(getString(R.string.sp_readability), false)) {
//                stringList.remove(array[4]); // Readability
//            }
//            stringList.remove(array[6]); // Relayout
//        }

        ListView listView = (ListView) layout.findViewById(R.id.dialog_list);
        DialogAdapter dialogAdapter = new DialogAdapter(this, R.layout.dialog_text_item, stringList);
        listView.setAdapter(dialogAdapter);
        try {
            dialogAdapter.notifyDataSetChanged();
        } catch (Exception e){

        }

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String s = stringList.get(position);
//                if (s.equals(array[0])){
//                    hideSoftInput(inputBox);
//                    showSearchPanel();
//                }else
                if (s.equals(array[0])) { // refresh
                    refreshPage();
                } else if (s.equals(array[1])) { // homepage
                    goToHomePage();
                } else if (s.equals(array[2])) { // add/remove bookmarks
                    addToBookMark();
                }
//                else if (s.equals(array[3])) { // mouse mode
//                    switchMouseMode();
//                }
                else if (s.equals(array[3])) { // bookmarks
                    //switcherRootView.setVisibility(View.VISIBLE);
                    addAlbum(BrowserUnit.FLAG_BOOKMARKS);
                } else if (s.equals(array[4])) { // history
                    //switcherRootView.setVisibility(View.VISIBLE);
                    addAlbum(BrowserUnit.FLAG_HISTORY);
                } else if (s.equals(array[5])) { // Settings
                    Intent intent = new Intent(BrowserActivity.this, SettingActivity.class);
                    startActivity(intent);
                } else if (s.equals(array[6])) { // Quit
                    safelyFinish();
                }
//                if (s.equals(array[0])) { // Go to top
//                    NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
//                    ObjectAnimator anim = ObjectAnimator.ofInt(ninjaWebView, "scrollY", ninjaWebView.getScrollY(), 0);
//                    anim.setDuration(mediumAnimTime);
//                    anim.start();
//                } else if (s.equals(array[1])) { // Add to home
//                    NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
//                    RecordAction action = new RecordAction(BrowserActivity.this);
//                    action.open(true);
//                    if (action.checkGridItem(ninjaWebView.getUrl())) {
//                        NinjaToast.show(BrowserActivity.this, R.string.toast_already_exist_in_home);
//                    } else {
//                        String title = ninjaWebView.getTitle().trim();
//                        String url = ninjaWebView.getUrl().trim();
//                        Bitmap bitmap = ViewUnit.capture(ninjaWebView, dimen156dp, dimen117dp, false, Bitmap.Config.ARGB_8888);
//                        String filename = System.currentTimeMillis() + BrowserUnit.SUFFIX_PNG;
//                        int ordinal = action.listGrid().size();
//                        GridItem item = new GridItem(title, url, filename, ordinal);
//
//                        if (BrowserUnit.bitmap2File(BrowserActivity.this, bitmap, filename) && action.addGridItem(item)) {
//                            NinjaToast.show(BrowserActivity.this, R.string.toast_add_to_home_successful);
//                        } else {
//                            NinjaToast.show(BrowserActivity.this, R.string.toast_add_to_home_failed);
//                        }
//                    }
//                    action.close();
//                } else if (s.equals(array[2])) { // Find in page
//                    hideSoftInput(inputBox);
//                    showSearchPanel();
//                } else if (s.equals(array[3])) { // Screenshot
//                    NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
//                    new ScreenshotTask(BrowserActivity.this, ninjaWebView).execute();
//                } else if (s.equals(array[4])) { // Readability
//                    String token = sp.getString(getString(R.string.sp_readability_token), null);
//                    if (token == null || token.trim().isEmpty()) {
//                        NinjaToast.show(BrowserActivity.this, R.string.toast_token_empty);
//                    } else {
//                        NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
//                        Intent intent = new Intent(BrowserActivity.this, ReadabilityActivity.class);
//                        intent.putExtra(IntentUnit.URL, ninjaWebView.getUrl());
//                        startActivity(intent);
//                    }
//                } else if (s.equals(array[5])) { // Share
//                    if (!prepareRecord()) {
//                        NinjaToast.show(BrowserActivity.this, R.string.toast_share_failed);
//                    } else {
//                        NinjaWebView ninjaWebView = (NinjaWebView) currentAlbumController;
//                        IntentUnit.share(BrowserActivity.this, ninjaWebView.getTitle(), ninjaWebView.getUrl());
//                    }
//                } else if (s.equals(array[6])) { // Relayout
//                    NinjaRelativeLayout ninjaRelativeLayout = (NinjaRelativeLayout) currentAlbumController;
//                    final DynamicGridView gridView = (DynamicGridView) ninjaRelativeLayout.findViewById(R.id.home_grid);
//                    final List<GridItem> gridList = ((GridAdapter) gridView.getAdapter()).getList();
//
//                    omnibox.setVisibility(View.GONE);
//                    relayoutOK.setVisibility(View.VISIBLE);
//
//                    relayoutOK.setOnTouchListener(new View.OnTouchListener() {
//                        @Override
//                        public boolean onTouch(View v, MotionEvent event) {
//                            if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                                relayoutOK.setTextColor(getResources().getColor(R.color.blue_500));
//                            } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
//                                relayoutOK.setTextColor(getResources().getColor(R.color.white));
//                            }
//
//                            return false;
//                        }
//                    });
//
//                    relayoutOK.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            gridView.stopEditMode();
//                            relayoutOK.setVisibility(View.GONE);
//                            omnibox.setVisibility(View.VISIBLE);
//
//                            RecordAction action = new RecordAction(BrowserActivity.this);
//                            action.open(true);
//                            action.clearGrid();
//                            for (GridItem item : gridList) {
//                                action.addGridItem(item);
//                            }
//                            action.close();
//                            NinjaToast.show(BrowserActivity.this, R.string.toast_relayout_successful);
//                        }
//                    });
//
//                    gridView.setOnDragListener(new DynamicGridView.OnDragListener() {
//                        private GridItem dragItem;
//
//                        @Override
//                        public void onDragStarted(int position) {
//                            dragItem = gridList.get(position);
//                        }
//
//                        @Override
//                        public void onDragPositionsChanged(int oldPosition, int newPosition) {
//                            if (oldPosition < newPosition) {
//                                for (int i = newPosition; i > oldPosition; i--) {
//                                    GridItem item = gridList.get(i);
//                                    item.setOrdinal(i - 1);
//                                }
//                            } else if (oldPosition > newPosition) {
//                                for (int i = newPosition; i < oldPosition; i++) {
//                                    GridItem item = gridList.get(i);
//                                    item.setOrdinal(i + 1);
//                                }
//                            }
//                            dragItem.setOrdinal(newPosition);
//
//                            Collections.sort(gridList, new Comparator<GridItem>() {
//                                @Override
//                                public int compare(GridItem first, GridItem second) {
//                                    if (first.getOrdinal() < second.getOrdinal()) {
//                                        return -1;
//                                    } else if (first.getOrdinal() > second.getOrdinal()) {
//                                        return 1;
//                                    } else {
//                                        return 0;
//                                    }
//                                }
//                            });
//                        }
//                    });
//                    gridView.startEditMode();
//                } else if (s.equals(array[7])) { // Quit
//                    finish();
//                }

                dialog.hide();
                dialog.dismiss();
            }
        });

        return true;
    }

    private void showGridMenu(final GridItem gridItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_list, null, false);
        builder.setView(layout);

        final String[] array = getResources().getStringArray(R.array.list_menu);
        final List<String> stringList = new ArrayList<>();
        stringList.addAll(Arrays.asList(array));
        stringList.remove(array[1]); // Copy link
        stringList.remove(array[2]); // Share

        ListView listView = (ListView) layout.findViewById(R.id.dialog_list);
        DialogAdapter dialogAdapter = new DialogAdapter(this, R.layout.dialog_text_item, stringList);
        listView.setAdapter(dialogAdapter);
        try {
            dialogAdapter.notifyDataSetChanged();
        } catch (Exception e){

        }

        final AlertDialog dialog = builder.create();
        dialog.show();

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = stringList.get(position);
                if (s.equals(array[0])) { // New tab
                    addAlbum(getString(R.string.album_untitled), gridItem.getURL(), false, null);
                    NinjaToast.show(BrowserActivity.this, R.string.toast_new_tab_successful);
                } else if (s.equals(array[3])) { // Edit
                    showEditDialog(gridItem);
                } else if (s.equals(array[4])) { // Delete
                    RecordAction action = new RecordAction(BrowserActivity.this);
                    action.open(true);
                    action.deleteGridItem(gridItem);
                    action.close();
                    BrowserActivity.this.deleteFile(gridItem.getFilename());

                    initHomeGrid((NinjaRelativeLayout) currentAlbumController, true);
                    NinjaToast.show(BrowserActivity.this, R.string.toast_delete_successful);
                }

                dialog.hide();
                dialog.dismiss();
            }
        });
    }

    private void showListMenu(final RecordAdapter recordAdapter, final List<Record> recordList, final int location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_list, null, false);
        builder.setView(layout);

        final String[] array = getResources().getStringArray(R.array.list_menu);
        final List<String> stringList = new ArrayList<>();

        stringList.addAll(Arrays.asList(array));
        if (currentAlbumController.getFlag() != BrowserUnit.FLAG_BOOKMARKS) {
            stringList.remove(array[3]);
        }

        ListView listView = (ListView) layout.findViewById(R.id.dialog_list);
        DialogAdapter dialogAdapter = new DialogAdapter(this, R.layout.dialog_text_item, stringList);
        listView.setAdapter(dialogAdapter);
        try {
            dialogAdapter.notifyDataSetChanged();
        } catch (Exception e){

        }

        final AlertDialog dialog = builder.create();
        dialog.show();

        final Record record = recordList.get(location);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String s = stringList.get(position);
                if (s.equals(array[0])) { // New tab
                    addAlbum(getString(R.string.album_untitled), record.getURL(), false, null);
                    NinjaToast.show(BrowserActivity.this, R.string.toast_new_tab_successful);
                } else if (s.equals(array[1])) { // Copy link
                    BrowserUnit.copyURL(BrowserActivity.this, record.getURL());
                } else if (s.equals(array[2])) { // Share
                    IntentUnit.share(BrowserActivity.this, record.getTitle(), record.getURL());
                } else if (s.equals(array[3])) { // Edit
                    showEditDialog(recordAdapter, recordList, location);
                } else if (s.equals(array[4])) { // Delete
                    RecordAction action = new RecordAction(BrowserActivity.this);
                    action.open(true);
                    if (currentAlbumController.getFlag() == BrowserUnit.FLAG_BOOKMARKS) {
                        action.deleteBookmark(record);
                    } else if (currentAlbumController.getFlag() == BrowserUnit.FLAG_HISTORY) {
                        action.deleteHistory(record);
                    }
                    action.close();

                    recordList.remove(location);
                    try {
                        recordAdapter.notifyDataSetChanged();
                    } catch (Exception e){

                    }


                    updateBookmarks();
                    updateAutoComplete();

                    NinjaToast.show(BrowserActivity.this, R.string.toast_delete_successful);
                }

                dialog.hide();
                dialog.dismiss();
            }
        });
    }

    private void showEditDialog(final GridItem gridItem) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_edit, null, false);
        builder.setView(layout);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final EditText editText = (EditText) layout.findViewById(R.id.dialog_edit);
        editText.setHint(R.string.dialog_title_hint);
        editText.setText(gridItem.getTitle());
        editText.setSelection(gridItem.getTitle().length());
        hideSoftInput(inputBox);
        showSoftInput(editText);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_DONE) {
                    return false;
                }

                String text = editText.getText().toString().trim();
                if (text.isEmpty()) {
                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
                    return true;
                }

                RecordAction action = new RecordAction(BrowserActivity.this);
                action.open(true);
                gridItem.setTitle(text);
                action.updateGridItem(gridItem);
                action.close();

                hideSoftInput(editText);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                        dialog.dismiss();
                    }
                }, longAnimTime);
                return false;
            }
        });
    }

    private void showEditDialog(final RecordAdapter recordAdapter, List<Record> recordList, int location) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(true);

        FrameLayout layout = (FrameLayout) getLayoutInflater().inflate(R.layout.dialog_edit, null, false);
        builder.setView(layout);

        final AlertDialog dialog = builder.create();
        dialog.show();

        final Record record = recordList.get(location);
        final EditText editText = (EditText) layout.findViewById(R.id.dialog_edit);
        editText.setHint(R.string.dialog_title_hint);
        editText.setText(record.getTitle());
        editText.setSelection(record.getTitle().length());
        hideSoftInput(inputBox);
        showSoftInput(editText);

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId != EditorInfo.IME_ACTION_DONE) {
                    return false;
                }

                String text = editText.getText().toString().trim();
                if (text.isEmpty()) {
                    NinjaToast.show(BrowserActivity.this, R.string.toast_input_empty);
                    return true;
                }

                RecordAction action = new RecordAction(BrowserActivity.this);
                action.open(true);
                record.setTitle(text);
                action.updateBookmark(record);
                action.close();

                try {
                    recordAdapter.notifyDataSetChanged();
                } catch (Exception e){

                }
                hideSoftInput(editText);
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        dialog.hide();
                        dialog.dismiss();
                    }
                }, longAnimTime);
                return false;
            }
        });
    }

    private boolean prepareRecord() {
        if (currentAlbumController == null || !(currentAlbumController instanceof NinjaWebView)) {
            return false;
        }

        NinjaWebView webView = (NinjaWebView) currentAlbumController;
        String title = webView.getTitle();
        String url = webView.getUrl();
        if (title == null
                || title.isEmpty()
                || url == null
                || url.isEmpty()
                || url.startsWith(BrowserUnit.URL_SCHEME_ABOUT)
                || url.startsWith(BrowserUnit.URL_SCHEME_MAIL_TO)
                || url.startsWith(BrowserUnit.URL_SCHEME_INTENT)) {
            return false;
        }
        return true;
    }

    private void setCustomFullscreen(boolean fullscreen) {
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        /*
         * Can not use View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION,
         * so we can not hide NavigationBar :(
         */
        int bits = WindowManager.LayoutParams.FLAG_FULLSCREEN;

        if (fullscreen) {
            layoutParams.flags |= bits;
        } else {
            layoutParams.flags &= ~bits;
            if (customView != null) {
                customView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            } else {
                contentFrame.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
            }
        }
        getWindow().setAttributes(layoutParams);
    }

    private AlbumController nextAlbumController(boolean next) {
        if (BrowserContainer.size() <= 1) {
            return currentAlbumController;
        }

        List<AlbumController> list = BrowserContainer.list();
        int index = list.indexOf(currentAlbumController);
        if (next) {
            index++;
            if (index >= list.size()) {
                index = 0;
            }
        } else {
            index--;
            if (index < 0) {
                index = list.size() - 1;
            }
        }

        return list.get(index);
    }



}
