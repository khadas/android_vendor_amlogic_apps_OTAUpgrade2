/**
* Description:
* @Copyright: Copyright (c) 2012
* @Company: Amlogic
* @version: 1.0
*/
package com.droidlogic.otaupgrade;

import android.app.Activity;
import android.app.Dialog;
import android.app.Service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.os.Bundle;
import android.os.Environment;
import android.os.Process;

import android.util.Log;

import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;

import android.widget.Button;


public class BadMovedSDcard extends Activity {
    public static final String TAG = "SDCARDACTION";
    public static final int SDCANCEL = 1;
    public static final int SDOK = 2;
    private Button mCancleBtn;
    private Button mSureBtn;
    private boolean mReg = false;
    private View.OnClickListener l = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.sdcard_cancel) {
                    Intent in = new Intent();
                    setResult(SDCANCEL, in);
                    finish();
                } else if (v.getId() == R.id.sdcard_ok) {
                    Intent in = new Intent();
                    setResult(SDOK, in);
                    finish();
                }
            }
        };

    private BroadcastReceiver sdcardListener = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (Intent.ACTION_MEDIA_MOUNTED.equals(action)) {
                    mSureBtn.setEnabled(true);
                }
            }
        };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK) &&
                (event.getRepeatCount() == 0)) {
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mid);
        mCancleBtn = (Button) findViewById(R.id.sdcard_cancel);
        mSureBtn = (Button) findViewById(R.id.sdcard_ok);
        mCancleBtn.setOnClickListener(l);
        mSureBtn.setOnClickListener(l);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (Environment.MEDIA_MOUNTED.equals(
                    Environment.getExternalStorageState()) &&
                (mSureBtn != null)) {
            mSureBtn.setEnabled(true);
        } else {
            IntentFilter intentFilter = new IntentFilter(Intent.ACTION_MEDIA_MOUNTED);
            intentFilter.addDataScheme("file");

            if (!mReg) {
                mReg = true;
                mSureBtn.setEnabled(false);
                registerReceiver(sdcardListener, intentFilter);
            }
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return true;
    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mReg) {
            mReg = false;
            unregisterReceiver(sdcardListener);
        }
    }
}
