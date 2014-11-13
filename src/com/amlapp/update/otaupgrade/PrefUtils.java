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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.SystemProperties;
/**
 * @ClassName PrefUtils
 * @Description TODO
 * @Date 2013-7-16
 * @Email
 * @Author
 * @Version V1.0
 */
public class PrefUtils {
    public static Boolean DEBUG                   = false;
    public static final String  TAG                     = "OTA";
    private Context             mContext;
    private static final String PREFS_DOWNLOAD_FILELIST = "download_filelist";
    private static final String PREFS_UPDATE_FILEPATH   = "update_file_path";
    private static final String PREFS_UPDATE_SCRIPT     = "update_with_script";
    private static final String PREFS_UPDATE_FILESIZE = "update_file_size";
    private static final String PREFS_UPDATE_DESC ="update_desc";
    public static final String DEV_PATH ="/storage/external_storage";
    public static final String PREF_START_RESTORE      = "retore_start";
    public static final String PREF_AUTO_CHECK = "auto_check";
    static final String  FlagFile                = ".wipe_record";
    private SharedPreferences   mPrefs;
    
    PrefUtils(Context context) {
        mPrefs = context.getSharedPreferences("update", Context.MODE_PRIVATE);
        mContext = context;
    }
    
    private void setString(String key, String Str) {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putString(key, Str);
        mEditor.commit();
    }
    
    private void setStringSet(String key, Set<String> downSet) {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putStringSet(key, downSet);
        mEditor.commit();
    }
    
    private void setInt(String key, int Int) {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putInt(key, Int);
        mEditor.commit();
    }
    public void setDescrib(String desc){
        setString(PREFS_UPDATE_DESC,desc);
    }
    public String getDescri(){
        return mPrefs.getString(PREFS_UPDATE_DESC,"");
    }
    private void setLong(String key, long Long) {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putLong(key, Long);
        mEditor.commit();
    }
    
    void setBoolean(String key, Boolean bool) {
        SharedPreferences.Editor mEditor = mPrefs.edit();
        mEditor.putBoolean(key, bool);
        mEditor.commit();
    }
    
    public void setScriptAsk(boolean bool) {
        setBoolean(PREFS_UPDATE_SCRIPT, bool);
    }
    
    public boolean getScriptAsk() {
        return mPrefs.getBoolean(PREFS_UPDATE_SCRIPT, false);
    }
    
    void setDownFileList(Set<String> downlist) {
        if (downlist.size() > 0) {
            setStringSet(PREFS_DOWNLOAD_FILELIST, downlist);
        }
    }
    
    Set<String> getDownFileSet() {
        return mPrefs.getStringSet(PREFS_DOWNLOAD_FILELIST, null);
    }
    
    boolean getBooleanVal(String key, boolean def) {
        return mPrefs.getBoolean(key, def);
    }
    
    public void setUpdatePath(String path) {
        setString(PREFS_UPDATE_FILEPATH, path);
    }
    
    public String getUpdatePath() {
        return mPrefs.getString(PREFS_UPDATE_FILEPATH, null);
    }
    public long getFileSize(){
        return mPrefs.getLong(PREFS_UPDATE_FILESIZE, 0);
    }
    public void saveFileSize(long fileSize){
        setLong(PREFS_UPDATE_FILESIZE,fileSize);
    }
    
    static boolean isUserVer() {
        String userVer = SystemProperties.get("ro.secure","");
        String userDebug = SystemProperties.get("ro.debuggable","");
        String hideLocalUp = SystemProperties.get("ro.otaupdate.local","");
        if (hideLocalUp != null && hideLocalUp.equals("1")) {
            if (userVer != null && userVer.length() > 0) {
                return (userVer.trim().equals("1")) && (userDebug.equals("0"));
            }
        }
        return false;
    }

    public static boolean getAutoCheck() {
        return ("true").equals(SystemProperties.get("ro.product.update.autocheck"));
    }
    void write2File() {
        String flagParentPath=Environment
             .getExternalStorage2Directory().getAbsolutePath()+"/";
        File devDir = new File(DEV_PATH);
            File[] devs = devDir.listFiles();
            for(File dev:devs){
                if(dev.isDirectory()&&dev.canWrite()){
                flagParentPath = dev.getAbsolutePath();
                flagParentPath += "/";
                break;
            }
        }
        File flagFile = new File(flagParentPath,FlagFile);
        if (!flagFile.exists()) {
            try {
                flagFile.createNewFile();
            } catch (IOException excep) {
            }
        }
        if (!flagFile.canWrite())
        {
            return;
        }
        FileWriter fw = null;
        try {
            fw = new FileWriter(flagFile);
        } catch (IOException excep) {
        }
        BufferedWriter output = new BufferedWriter(fw);
        Set<String> downfiles = mPrefs.getStringSet(PREFS_DOWNLOAD_FILELIST,
                null);
        if (downfiles != null && downfiles.size() > 0) {
            String[] downlist = downfiles.toArray(new String[0]);
            for (int i = 0; i < downlist.length; i++) {
                try {
                    output.write(downlist[i]);
                    output.newLine();
                } catch (IOException ex) {
                }
            }
        }
        try {
            output.close();
        } catch (IOException e) {
        }
    }
}
