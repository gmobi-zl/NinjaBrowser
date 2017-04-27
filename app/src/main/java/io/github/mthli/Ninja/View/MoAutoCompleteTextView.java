package io.github.mthli.Ninja.View;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AutoCompleteTextView;
//import android.support.v7.widget.AppCompatAutoCompleteTextView;

/**
 * Created by zl on 2017/4/26.
 */

public class MoAutoCompleteTextView extends AutoCompleteTextView{

    public interface IPopListItemClickedListener{
        void onClicked(int pos);
    }

    IPopListItemClickedListener listKeyBoardClickedListener;

    public void setListKeyBoardClickedListener(IPopListItemClickedListener listKeyBoardClickedListener) {
        this.listKeyBoardClickedListener = listKeyBoardClickedListener;
    }

    public MoAutoCompleteTextView(Context context){
        super(context);
    }

    public MoAutoCompleteTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MoAutoCompleteTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    public boolean onKeyPreIme(int keyCode, KeyEvent event) {
        //Log.i("Nanja", "onKeyPreIme  = " + keyCode + "    event = " + event);


        boolean ret = super.onKeyPreIme(keyCode, event);
        //Log.i("Nanja", "super onKeyPreIme return " + ret);
        return ret;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        Log.i("Nanja", "onKeyDown popshow = " + isPopupShowing());
        Log.i("Nanja", "onKeyDown keycode = " + keyCode + "    event = " + event);
        boolean ret = super.onKeyDown(keyCode, event);
        Log.i("Nanja", "super onKeyDown return " + ret);
        return ret;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        Log.i("Nanja", "onKeyUp popshow = " + isPopupShowing());
        Log.i("Nanja", "onKeyUp keycode = " + keyCode + "    event = " + event);
        boolean isShowingPopup = isPopupShowing();
        int selectListIndex = getListSelection();
        if (KeyEvent.KEYCODE_DPAD_CENTER == keyCode && isShowingPopup == true && selectListIndex >= 0){
            if (this.listKeyBoardClickedListener != null)
                this.listKeyBoardClickedListener.onClicked(selectListIndex);
        }
        boolean ret = super.onKeyUp(keyCode, event);

        Log.i("Nanja", "super onKeyUp return " + ret);
        return ret;
    }

    @Override
    public void clearFocus() {
        Log.i("Nanja", "---------------------clearFocus-------------------------");
        super.clearFocus();
    }
}
