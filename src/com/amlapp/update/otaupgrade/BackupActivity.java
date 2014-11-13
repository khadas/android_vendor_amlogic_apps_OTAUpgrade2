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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import java.io.File;
import com.amlogic.update.Backup;

/** 
 * @ClassName BackupActivity 
 * @Description TODO
 * @Date 2013-7-16 
 * @Email 
 * @Author 
 * @Version V1.0
 */
public class BackupActivity extends Activity{
    public static String BACKUP_FILE = Environment
            .getExternalStorage2Directory().getAbsolutePath() + "/" + "BACKUP";
        public static final String SdcardDir = Environment
            .getExternalStorage2Directory().getAbsolutePath();
        public static final int FUNCBACKUP = 1;
        public static final int FUNCRESTORE = 2;
        public static int func = 0;
        private static void getBackUpFileName(){
                File devDir = new File(PrefUtils.DEV_PATH);
                File[] devs = devDir.listFiles();
                for(File dev:devs){
                    if(dev.isDirectory()&&dev.canWrite()){
                            BACKUP_FILE = dev.getAbsolutePath();
                            BACKUP_FILE += "/BACKUP";
                            break;
                    }
                }
        }
        @Override
        protected void onCreate(Bundle icicle) {
            getBackUpFileName();
            super.onCreate(icicle);
            boolean flag = false;
            String act = getIntent().getAction();
            if (act.equals(LoaderReceiver.BACKUPDATA)) {
                func = FUNCBACKUP;
                if (!OnSDcardStatus()) {
                    flag = true;
                    Intent intent0 = new Intent(this, BadMovedSDcard.class);
                    Activity activity = (Activity) this;
                    startActivityForResult(intent0, 1);
                }else {
                    Backup();
                }
            } else if (act.equals(LoaderReceiver.RESTOREDATA)) {
                func = FUNCRESTORE;
                if (!OnSDcardStatus()) {
                    flag = true;
                    Intent intent0 = new Intent(this, BadMovedSDcard.class);
                    Activity activity = (Activity) this;
                    startActivityForResult(intent0, 1);
                }else{
                    Restore();
                }
            }
            if (!flag) {
                Intent intent = new Intent();
                setResult(1, intent);
                finish();
            }
        }

        @Override
        protected void onActivityResult(int requestCode, int resultCode, Intent data) {
            super.onActivityResult(requestCode, resultCode, data);
            if (data != null && requestCode == 1) {
                if (resultCode == BadMovedSDcard.SDCANCEL) {
                    this.finish();
                }else if(resultCode == BadMovedSDcard.SDOK){
                    if (func == FUNCBACKUP){
                        Backup();
                    }else{
                        Restore();
                    }
                    Intent intent = new Intent();
                    setResult(1, intent);
                    finish();
                }
            }
        }
        @Override
        protected void onDestroy() {
            super.onDestroy();
        }

        private boolean OnSDcardStatus() {
            File file = new File(BACKUP_FILE);
            return  file.getParentFile().canWrite();
            //return Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorage2State());
        }

        private void Backup() {
            new Thread() {
            final String[] args = { BACKUP_FILE, "backup", "-apk",
                "-system", "-all" };

            public void run() {
                Backup mBackup = new Backup(BackupActivity.this);
                mBackup.main(args);
            }
            }.start();
        }

        private void Restore() {
            new Thread() {
                final String[] args = { BACKUP_FILE, "restore", "-apk",
                    "-system", "-all" };
                public void run() {
                    Backup mBackup = new Backup(BackupActivity.this);
                    mBackup.main(args);
                }
            }.start();
        }
}
