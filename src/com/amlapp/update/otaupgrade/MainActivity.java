/**
 * @Package com.amlogic.otauicase 
 * @Description
 *
 * Copyright (c) Inspur Group Co., Ltd. Unpublished
 *
 * Inspur Group Co., Ltd.
 * Proprietary & Confidential
 *
 * This source code and the algorithms implemented therein constitute
 * confidential information and may comprise trade secrets of Inspur
 * or its associates, and any use thereof is subject to the terms and
 * conditions of the Non-Disclosure Agreement pursuant to which this
 * source code was originally received.
 */
package com.amlapp.update.otaupgrade;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

import org.apache.http.util.EncodingUtils;

import com.amlogic.update.OtaUpgradeUtils;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

/** 
 * @ClassName UpdateActivity 
 * @Description TODO
 * @Date 2013-7-16 
 * @Email 
 * @Author 
 * @Version V1.0
 */
public class MainActivity extends Activity  implements OnClickListener {
    private static final String TAG = UpdateService.TAG;
    private static final String ENCODING = "UTF-8";
    private static final String VERSION_NAME = "version";
    private Button mOnlineUpdateBtn;
    private Button mLocalUpdateBtn;
    private Button mBackupBtn;
    private Button mRestoreBtn;
    private Button mUpdateCertern;
    private CheckBox mWipeDate;
    private CheckBox mWipeMedia;
    //private CheckBox mWipeCache;
    private TextView filepath;
    private TextView filename;
    private static final int queryReturnOk = 0;
    private static final int queryUpdateFile = 1;
    private static int UpdateMode = OtaUpgradeUtils.UPDATE_UPDATE;
    private PrefUtils mPreference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_view);
        hideBackUp();
        mOnlineUpdateBtn = (Button) findViewById(R.id.updatebtn);
        mLocalUpdateBtn = (Button) findViewById(R.id.btn_update_locale);
        mUpdateCertern = (Button) findViewById(R.id.btn_locale_certern);
        mBackupBtn = (Button) findViewById(R.id.backup);
        mRestoreBtn = (Button) findViewById(R.id.restore);
        mWipeDate = (CheckBox) findViewById(R.id.wipedata);
        mWipeMedia = (CheckBox) findViewById(R.id.wipemedia);
        //mWipeCache = (CheckBox) findViewById(R.id.wipecache);
        filename = (TextView) findViewById(R.id.update_file_name);
        filepath = (TextView) findViewById(R.id.update_full_name);
        mPreference = new PrefUtils(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem item = menu.findItem(R.id.settings);
        if (PrefUtils.getAutoCheck()){
            item.setVisible(false);
            mPreference.setBoolean(PrefUtils.PREF_AUTO_CHECK,true);
        }else{
            if(mPreference.getBooleanVal(PrefUtils.PREF_AUTO_CHECK,false)){
            item.setTitle(R.string.auto_check_close);
        }else{
            item.setTitle(R.string.auto_check);
        }
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (data != null) {
            if (requestCode == queryUpdateFile && resultCode == queryReturnOk) {
            Bundle bundle = data.getExtras();
            String file = bundle.getString(FileSelector.FILE);
                if (file != null) {
                    filepath.setText(file);
                    filename.setText(file.substring(file.lastIndexOf("/")+1, file.length()));
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.about) {
            AlertDialog.Builder builder = new Builder(MainActivity.this);
            if (getResources().getConfiguration().locale.getCountry().equals("CN")) {
                builder.setMessage(getFromAssets(VERSION_NAME+"_cn")); 
            } else {
                builder.setMessage(getFromAssets(VERSION_NAME));
            }
            builder.setTitle(R.string.version_info); 
            builder.setPositiveButton(R.string.sure, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
        }else if(item.getItemId() == R.id.settings) {
            if(MainActivity.this.getResources().getString(R.string.auto_check).equals(item.getTitle().toString())){
                mPreference.setBoolean(PrefUtils.PREF_AUTO_CHECK,true);
                item.setTitle(R.string.auto_check_close);
            }else{
                mPreference.setBoolean(PrefUtils.PREF_AUTO_CHECK,false);
                item.setTitle(R.string.auto_check);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void UpdateDialog(String filename) {
        final Dialog dlg = new Dialog(this, R.style.Theme_dialog);
        LayoutInflater inflater = LayoutInflater.from(this);
        InstallPackage dlgView = (InstallPackage) inflater.inflate(
            R.layout.install_ota, null, false);
        dlgView.setPackagePath(filename);
        dlgView.setParamter(UpdateMode);
        dlg.setContentView(dlgView);
        dlg.setCancelable(false);
        dlg.findViewById(R.id.confirm_cancel).setOnClickListener(
            new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    dlg.dismiss();
                }
            });
        dlg.show();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        if(filepath != null) {
            savedInstanceState.putString("filepath", filepath.getText().toString());
        }
        if(filename != null) {
            savedInstanceState.putString("filename", filename.getText().toString());
        }
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        String strPath = savedInstanceState.getString("filepath");
        String strName = savedInstanceState.getString("filename");
        if(filepath!= null && strPath != null) {
            filepath.setText(strPath);
        }
        if(filename!= null && strName != null) {
            filename.setText(strName);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mOnlineUpdateBtn.setOnClickListener(this);
        mLocalUpdateBtn.setOnClickListener(this);
        mBackupBtn.setOnClickListener(this);
        mRestoreBtn.setOnClickListener(this);
        mUpdateCertern.setOnClickListener(this);
        if (PrefUtils.isUserVer()) {
            ViewGroup vg= (ViewGroup)findViewById(R.id.update_locale_layer);
            if (vg != null) {
                vg.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
        case R.id.backup:
            Intent intent = new Intent(LoaderReceiver.BACKUPDATA);
            intent.setClass(this, BackupActivity.class);
            startActivity(intent);
            this.finish();
            break;
        case R.id.restore:
            Intent intent2 = new Intent(LoaderReceiver.RESTOREDATA);
            intent2.setClass(this, BackupActivity.class);
            startActivity(intent2);
            this.finish();
            break;
        case R.id.btn_update_locale:
            Intent intent0 = new Intent(this, FileSelector.class);
            Activity activity = (Activity) this;
            startActivityForResult(intent0, queryUpdateFile);
            break;
        case R.id.btn_locale_certern:
            String fullname = filepath.getText().toString();
            if (filename != null && filename.length() > 0) {
                createAmlScript();
                UpdateDialog(fullname);
            } else {
                Toast.makeText(this, getString(R.string.file_not_exist), 2000)
                    .show();
            }
            break;
        case R.id.updatebtn:
            if (!checkInternet()) {
                Toast.makeText(this, getString(R.string.net_error), 2000)
                    .show();
                return;
            }
            Intent intent1 = new Intent();
            intent1.setAction(UpdateService.ACTION_CHECK);
            intent1.setClass(this, UpdateActivity.class);
            startActivity(intent1);
            this.finish();
            break;

        }
    }

    private boolean checkInternet() {
        ConnectivityManager cm = (ConnectivityManager) this
            .getSystemService(this.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            return true;
        } else {
            Log.v(TAG, "It's can't connect the Internet!");
            return false;
        }
    }

    private boolean createAmlScript() {
        File file;
        String res = "";
        String fullpath = filepath.getText().toString();
        //String filedir = fullpath.substring(0,fullpath.lastIndexOf("/"));
        if (fullpath.lastIndexOf("/") < 0) {
            Toast.makeText(this, getString(R.string.file_not_exist), 2000)
                .show();
            return false;
        }
        file = new File("/cache/recovery/command");
        try{
            File parent = file.getParentFile();
            if(!parent.exists()){
                parent.mkdirs();
            }
            if(!file.exists()){
                file.createNewFile();
            }
        }catch(IOException e){
            e.printStackTrace();
        }
        res += "--update_package=";
        if (Environment.MEDIA_MOUNTED.equals(Environment
            .getExternalStorage2State()) && FileSelector.isSdcard(fullpath)) {
            res += "/sdcard/";
        }else if(FileSelector.isUdisk(fullpath)) {
            res += "/udisk/";
        }else if(FileSelector.isMediaPart(fullpath)) {
            res += "/media/";
            UpdateMode = OtaUpgradeUtils.UPDATE_RECOVERY;
        }else {
            UpdateMode = OtaUpgradeUtils.UPDATE_UPDATE;
        }
        if (FileSelector.isICSupdate() && !FileSelector.isMediaPart(fullpath)){
            UpdateMode = OtaUpgradeUtils.UPDATE_RECOVERY;
        }
        res += filename.getText().toString();
        res += "\n--locale=" + Locale.getDefault().toString();
        res += (mWipeDate.isChecked() ? "\n--wipe_data" : "");
        res += (mWipeMedia.isChecked() ? "\n--wipe_media" : "");
        //res += (mWipeCache.isChecked() ? "\n--wipe_cache" : "");
        try {
            FileOutputStream fout = new FileOutputStream(file);
            byte[] buffer = res.getBytes();
            fout.write(buffer);
            fout.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.d(TAG,"IOException:"+this.getClass());
        }
        return true;
    }

    public String getFromAssets(String fileName){
        String result = "";
        try {
            InputStream in = getResources().getAssets().open(fileName);
            int lenght = in.available();
            byte[]  buffer = new byte[lenght];
            in.read(buffer);
            result = EncodingUtils.getString(buffer, ENCODING);
        } catch (Exception e) {
          e.printStackTrace();
        }
          return result;
    }
    public void hideBackUp(){
        if(FileSelector.isICSupdate()){
            TextView tv = (TextView)findViewById(R.id.backuptitle);
            ViewGroup backupViews = (ViewGroup)findViewById(R.id.backup_layout);
            View div = (View)findViewById(R.id.divider1);
            div.setVisibility(View.GONE);
            tv.setVisibility(View.GONE);
            backupViews.setVisibility(View.GONE);
        }
    }
}
