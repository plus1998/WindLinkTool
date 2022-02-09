package com.happyplus.yixuan;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

public class Startup extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e("HAPPYPLUS", "收到广播正在准备开机自启");
            Toast.makeText(context, "正在准备打开嘟嘟车机", Toast.LENGTH_LONG);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    Log.e("HAPPYPLUS", "启动嘟嘟车机");
                    PackageManager packageManager = context.getPackageManager();
                    Intent carIntent = packageManager.getLaunchIntentForPackage("com.wow.carlauncher");
                    carIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    context.startActivity(carIntent);
                }
            }).start();
        }
    }
}