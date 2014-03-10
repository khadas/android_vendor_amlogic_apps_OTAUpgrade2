/** 
 * Description: 
 * @Copyright: Copyright (c) 2012
 * @Company: Amlogic
 * @version: 1.0
 */

package com.amlapp.update.otaupgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.SystemProperties;
import android.util.Log;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileInputStream;
import com.amlogic.update.Backup;

public class LoaderReceiver extends BroadcastReceiver {

    private static final String TAG = PrefUtils.TAG;
    public static final String UPDATE_GET_NEW_VERSION = "com.android.update.UPDATE_GET_NEW_VERSION";
    public static final String CHECKING_TASK_COMPLETED = "com.android.update.CHECKING_TASK_COMPLETED";
    public static final String RESTOREDATA = "com.android.amlogic.restoredata";
    public static final String BACKUPDATA = "com.android.amlogic.backupdata";
    public static final String BACKUP_FILE = Environment
            .getExternalStorage2Directory().getAbsolutePath() + "/" + "BACKUP";
    private PrefUtils mPref;
    private Context mContext;
    @Override
    public void onReceive(Context context, Intent intent) {
        mContext = context;
        mPref = new PrefUtils(mContext);
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)
            || intent.getAction().equals(RESTOREDATA)) {
            mPref.setBoolean("Boot_Checked",true);
            afterReboot();
        } else if (intent.getAction().equals(BACKUPDATA)) {
            if(PrefUtils.DEBUG)
                Log.d(TAG, "backup");
            backup();
        }
        //Log.d(TAG,"getAction:"+intent.getAction());
        if ((ConnectivityManager.CONNECTIVITY_ACTION).equals(intent.getAction())) {
            NetworkInfo netInfo = (NetworkInfo) intent.getExtra(WifiManager.EXTRA_NETWORK_INFO, null);
            if(PrefUtils.DEBUG)
                Log.d(TAG,"BootCompleteFlag"+(mPref.getBooleanVal("Boot_Checked",false) )+""+(netInfo != null)+ ""+mPref.getBooleanVal(PrefUtils.PREF_AUTO_CHECK,false) + (netInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED));
            if(mPref.getBooleanVal("Boot_Checked",false) && (netInfo != null) && (netInfo.getDetailedState() == NetworkInfo.DetailedState.CONNECTED)) {
                mPref.setBoolean("Boot_Checked",false);
                if(mPref.getBooleanVal(PrefUtils.PREF_AUTO_CHECK,false)) {
                    mContext.startService(new Intent(UpdateService.ACTION_AUTOCHECK));
                    return;
                }else if(("true").equals(SystemProperties.getBoolean("ro.product.update.autocheck",false))) {
                    mPref.setBoolean(PrefUtils.PREF_AUTO_CHECK,true);
                    mContext.startService(new Intent(UpdateService.ACTION_AUTOCHECK));
                }
            }
        }
    }


    private void afterReboot(){
        final String[] args = {
            BACKUP_FILE,
            "restore",
            "-apk",
            "-system",
            "-all"
            };
        new Thread(){
            public void run(){
                try {
                    Thread.sleep(5000);
                }catch (InterruptedException e){
                }finally{
                    boolean ismounted = Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorage2State());
                    File flagFile = new File(Environment.getExternalStorage2Directory(),PrefUtils.FlagFile);
                    if (ismounted) {
                        File bkfile = new File(BACKUP_FILE);
                        if (flagFile.exists()) {
                            try{
                                String files = null;
                                BufferedReader input = new BufferedReader(new FileReader(flagFile));
                                while((files= input.readLine())!=null){
                                    File temp = new File(files);
                                    if (temp.exists()){
                                        temp.delete();
                                    }
                                }
                                flagFile.delete();
                            }catch(IOException ex){}
                        }
                        if (bkfile.exists() && !mPref.getBooleanVal(PrefUtils.PREF_START_RESTORE, false)){
                            mPref.setBoolean(PrefUtils.PREF_START_RESTORE, true);
                                try{
                                FileInputStream fis = new FileInputStream(bkfile) ;
                                if (fis.available()<=0){
                                    bkfile.delete();
                                }else{
                                    Backup mBackup = new Backup(mContext);
                                    mBackup.main(args);
                                }
                            }catch(Exception ex){}
                        }else if(bkfile.exists()) {
                            mPref.setBoolean(PrefUtils.PREF_START_RESTORE, false);
                            bkfile.delete();
                        }
                    }
                }
            }
        }.start();
    }

    private void backup(){
        final String[] args = {
            BACKUP_FILE,
            "backup", "-apk","-system","-all" };
        Backup mBackup = new Backup(mContext);
        mBackup.main(args);
    }
}


