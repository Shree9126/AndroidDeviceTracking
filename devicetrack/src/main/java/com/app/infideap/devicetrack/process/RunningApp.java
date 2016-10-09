package com.app.infideap.devicetrack.process;

import android.content.Context;
import android.util.Log;

import com.app.infideap.devicetrack.entity.AppInfo;
import com.jaredrummler.android.processes.AndroidProcesses;
import com.jaredrummler.android.processes.models.AndroidAppProcess;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Shiburagi on 24/08/2016.
 */
public class RunningApp{
    private static final String TAG = RunningApp.class.getSimpleName();
    private HashMap<String, AppInfo> map = new HashMap<>();


    public void request(final Context context, final OnAppStateChangeListener listener) {

        Log.d(TAG, "onStartCommand()");
        Timer timer = new Timer();
        timer.scheduleAtFixedRate(new TimerTask() {
            public void run() {
                AppInformation appInformation = new AppInformation(context);
                List<AppInfo> list = appInformation.request(null);
                List<AppInfo> open = new ArrayList<AppInfo>(list.size());
                List<AppInfo> close = new ArrayList<AppInfo>(list.size());
                for (AppInfo appInfo : list) {
                    String packageName = String.valueOf(appInfo.packageName);
                    Log.d(TAG, packageName);
                    if (isForeground(context, appInfo.packageName)) {
                        if (!map.containsKey(packageName)) {
                            map.put(packageName, appInfo);
                            open.add(appInfo);
                        }
                    } else if (map.containsKey(packageName)) {
                        map.remove(packageName);
                        close.add(appInfo);
                    }

                }
                listener.onChanged(open, close);

            }
        }, 0, 6 * 1000);  // every 6 seconds
    }

    public static boolean isForeground(Context context, String processName) {

        List<AndroidAppProcess> processes = AndroidProcesses.getRunningForegroundApps(context);
        for (AndroidAppProcess process : processes) {
            if (process.getPackageName().equalsIgnoreCase(processName))
                return true;
        }


        return false;
    }


    private interface OnAppStateChangeListener {
        void onChanged(List<AppInfo> openList,List<AppInfo> closeList);
    }
}
