/**
 * @Package com.amlogic.otauicase
 * @Description Copyright (c) Inspur Group Co., Ltd. Unpublished Inspur Group
 *              Co., Ltd. Proprietary & Confidential This source code and the
 *              algorithms implemented therein constitute confidential
 *              information and may comprise trade secrets of Inspur or its
 *              associates, and any use thereof is subject to the terms and
 *              conditions of the Non-Disclosure Agreement pursuant to which
 *              this source code was originally received.
 */
package com.amlapp.update.otaupgrade;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Message;

/**
 * @ClassName FileSelector
 * @Description TODO
 * @Date 2013-7-16
 * @Email
 * @Author
 * @Version V1.0
 */
public class FileSelector extends Activity implements OnItemClickListener {
    private static final String TAG = "FileSelector";
    public static final String  FILE       = "file";
    private File                mCurrentDirectory;
    private LayoutInflater      mInflater;
    private FileAdapter         mAdapter   = new FileAdapter();
    private ListView            mListView;
    private static final int JB2 = 17;
    private static final int JB1 = 16;
    private static final int ICS404 = 15;
    private static final String LOCAL      = "/sdcard";
    private static final String UDISK_ICS  = "/mnt/sd";
    private static final String UDISK_JB1  = "/storage/sd";
    private static final String UDISK_JB2  = "/storage/external_storage/sd";
    private static final String SDCARD_JB  = "/sdcard/external_sdcard";
    private static final String SDCARD_JB1 = "/storage/external_storage/sdcard";
    private ProgressDialog mPdWatingScan = null;
    private static final int MSG_HIDE_SHOW_DIALOG = 1;
    private static final int MSG_SHOW_WAIT_DIALOG = 2;
    private static final int MSG_NOTIFY_DATACHANGE = 3;
    private static final int WAITDIALOG_DISPALY_TIME = 500;

    private Handler mHandler =new Handler(){
        @Override
        public void handleMessage(Message msg){
	    super.handleMessage(msg);
	    switch(msg.what) {
	    case MSG_SHOW_WAIT_DIALOG:
                mPdWatingScan = ProgressDialog.show(FileSelector.this, getResources().getString(R.string.scan_title), getResources().getString(R.string.scan_tip) );
            break;
            case MSG_HIDE_SHOW_DIALOG:
		removeMessages(MSG_SHOW_WAIT_DIALOG);
		if( mPdWatingScan != null ) {
            		mPdWatingScan.dismiss();
			mPdWatingScan = null;
		}
	    break;
	    case MSG_NOTIFY_DATACHANGE:
	    	mAdapter.notifyDataSetChanged();
	    break;
	    default:
		break;
	    }
        }
    };

    private void startScanThread() {
      Message nmsg = mHandler.obtainMessage(MSG_SHOW_WAIT_DIALOG);
      mHandler.sendMessageDelayed(nmsg, WAITDIALOG_DISPALY_TIME);

      new Thread(){
         public void run(){
	    File[] files = new File[2];
        files[0] = new File("/sdcard");
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
            files[1] = new File("/mnt/");
        } else {
            files[1] = new File("/storage");
        }
            mAdapter.getList(files);
            mHandler.sendEmptyMessage(MSG_HIDE_SHOW_DIALOG);
         }
      }.start();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mInflater = LayoutInflater.from(this);
        setContentView(R.layout.file_list);
        mListView = (ListView) findViewById(R.id.file_list);
        mListView.setAdapter(mAdapter);
        startScanThread();
        mListView.setOnItemClickListener(this);
    }
    
    @Override
    public void onItemClick(AdapterView<?> adapterView, View view,
            int position, long id) {
        File selectFile = (File) adapterView.getItemAtPosition(position);
        /*
         * if (selectFile.isDirectory()) { mCurrentDirectory = selectFile;
         * FileAdapter adapter = (FileAdapter) adapterView.getAdapter();
         * adapter.setCurrentList(selectFile); } else
         */
        if (selectFile.isFile()) {
            Intent intent = new Intent();
            intent.putExtra(FILE, selectFile.getPath());
            setResult(0, intent);
            finish();
        }
    }
    
    /*
     * @Override public void onBackPressed() { if (mCurrentDirectory == null ||
     * mCurrentDirectory.getPath().equals("/sdcard")) { super.onBackPressed(); }
     * else { mCurrentDirectory = mCurrentDirectory.getParentFile();
     * mAdapter.setCurrentList(mCurrentDirectory); } }
     */
    // @Override
    // public boolean onKeyDown(int keyCode, KeyEvent event) {
    // if (keyCode == KeyEvent.KEYCODE_BACK) {
    // if (mCurrentDirectory == null ||
    // mCurrentDirectory.getPath().equals("/sdcard")) {
    // return super.onKeyDown(keyCode, event);
    // } else {
    // mAdapter.setCurrentList(mCurrentDirectory.getParentFile());
    // return false;
    // }
    // }
    // return super.onKeyDown(keyCode, event)
    // }
    private class FileAdapter extends BaseAdapter {
        private File            mFiles[];
        private ArrayList<File> files = new ArrayList();
        
        public void setCurrentList(File directory) {
            File[] tempFiles = directory.listFiles(new ZipFileFilter());
            for (int i = 0; tempFiles != null && i < tempFiles.length; i++) {
                if (tempFiles[i].isDirectory()) {
                    setCurrentList(tempFiles[i]);
                } else {
                    files.add(tempFiles[i]);
                }
            }
        }
        
        public void getList(File[] dir) {
            for (int j = 0; j < dir.length; j++) {
                setCurrentList(dir[j]);
            }
            mFiles = new File[files.size()];
            for (int i = 0; i < files.size(); i++) {
                mFiles[i] = (File) files.get(i);
            }
            mHandler.sendEmptyMessage(MSG_NOTIFY_DATACHANGE);
        }
        
        @Override
        public int getCount() {
            return mFiles == null ? 0 : mFiles.length;
        }
        
        @Override
        public File getItem(int position) {
            File file = mFiles == null ? null : mFiles[position];
            return file;
        }
        
        @Override
        public long getItemId(int position) {
            return position;
        }
        
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.large_text, null);
            }
            TextView tv = (TextView) convertView;
            File file = mFiles[position];
            String name = file.getPath();
            tv.setText(name);
            return tv;
        }
    }
    
    class ZipFileFilter implements FilenameFilter {
        public boolean accept(File directory, String file) {
            String dir = directory.getPath();
            if (new File(directory, file).isDirectory()) {
                return true;
            } else if (file.toLowerCase().endsWith(".zip") && isSdcard(dir) && !isUdisk(dir)) {
                return true;
            } else if (file.toLowerCase().endsWith(".zip") && isUdisk(dir) && !isSdcard(dir)) {
                return true;
            } else if (file.toLowerCase().endsWith(".zip") && isMediaPart(dir)) {
                return true;
            } else {
                return false;
            }
        }
    }
    
    public static boolean isUdisk(String dir) {
        if (Build.VERSION.SDK_INT == ICS404
                && dir.startsWith(UDISK_ICS) && !dir.contains("sdcard")) {
            return true;
        } else if (Build.VERSION.SDK_INT == JB1
                && dir.startsWith(UDISK_JB1) && !dir.contains("sdcard")) {
            return true;
        } else if (Build.VERSION.SDK_INT >= JB2
                && dir.startsWith(UDISK_JB2) && !dir.contains("sdcard")) {
            return true;
        }
        return false;
    }
    
    public static boolean isSdcard(String dir) {
        if (Build.VERSION.SDK_INT == ICS404
                && dir.startsWith(SDCARD_JB)) {
            return true;
        } else if (Build.VERSION.SDK_INT == JB1
                && dir.startsWith(SDCARD_JB)) {
            return true;
        } else if (Build.VERSION.SDK_INT >= JB2
                && dir.startsWith(SDCARD_JB1)) {
            return true;
        }
        return false;
    }
    
    public static boolean isMediaPart(String dir) {
        return ((Build.VERSION.SDK_INT < JB2) && dir
                .startsWith(LOCAL) && !dir.startsWith(SDCARD_JB));
    }
    
    public static boolean isICSupdate() {
        return (Build.VERSION.SDK_INT == ICS404);
    }
}
