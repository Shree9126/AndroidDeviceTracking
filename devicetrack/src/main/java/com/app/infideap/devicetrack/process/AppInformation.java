package com.app.infideap.devicetrack.process;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageStatsObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageStats;
import android.content.pm.PermissionInfo;
import android.net.TrafficStats;
import android.os.RemoteException;
import android.util.Log;

import com.app.infideap.devicetrack.entity.AppInfo;
import com.app.infideap.devicetrack.util.Utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shiburagi on 11/07/2016.
 */
public class AppInformation {


    private static final String TAG = AppInformation.class.getSimpleName();
    private final Context context;
    private int INSTALL=1;

    public AppInformation(Context context) {
        this.context = context;
    }

    public List<AppInfo> request(OnRequestListener listener) {
        PackageManager pm = context.getPackageManager();
        List<PackageInfo> list = pm.getInstalledPackages(0);

        List<AppInfo> appInfos = new ArrayList<>();
        for (PackageInfo pi : list) {
            ApplicationInfo ai = null;
            try {
                ai = pm.getApplicationInfo(pi.packageName, 0);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }
            if (ai == null)
                continue;
            if ((ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0)
                appInfos.add(extractInfo(pm, pi, ai, listener));
        }

        return appInfos;
    }

    private AppInfo extractInfo(PackageManager pm, PackageInfo pi, ApplicationInfo ai,
                                final OnRequestListener listener) {
        long delta_rx = TrafficStats.getUidRxBytes(ai.uid);
        long delta_tx = TrafficStats.getUidTxBytes(ai.uid);

        final AppInfo appInfo = new AppInfo();
        appInfo.packageName = ai.packageName;
        appInfo.appInstall = pi.firstInstallTime;
        appInfo.appUpdate = pi.lastUpdateTime;
        appInfo.updatedDate = System.currentTimeMillis();
        appInfo.install = INSTALL;
        appInfo.version = pi.versionName;
        appInfo.uploadSize = Utils.toMB(delta_tx);
        appInfo.downloadSize = Utils.toMB(delta_rx);
        appInfo.appName = getAppLable(context, ai);
        StringBuilder builder = new StringBuilder();
        if (pi.permissions != null)
            for (PermissionInfo info : pi.permissions) {
                builder.append("\t-> ").append(info.group).append(",\t")
                        .append(info.flags).append(",\t")
                        .append(info.descriptionRes).append(",\t")
                        .append(info.nonLocalizedDescription).append("\n");
            }

        Method getPackageSizeInfo = null;
        try {
            getPackageSizeInfo = pm.getClass().getMethod(
                    "getPackageSizeInfo", String.class, IPackageStatsObserver.class);


            getPackageSizeInfo.invoke(pm, pi.packageName,
                    new IPackageStatsObserver.Stub() {

                        public void onGetStatsCompleted(PackageStats pStats, boolean succeeded)
                                throws RemoteException {

                            Log.i(TAG, "dataSize: " + pStats.dataSize);
                            Log.i(TAG, "cacheSize: " + pStats.cacheSize);

                            appInfo.dataSize = Utils.toMB(pStats.dataSize);
                            appInfo.cacheSize = Utils.toMB(pStats.cacheSize);
                            if (listener != null)
                                listener.onAppInfoChanged(appInfo);
                        }
                    });
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return appInfo;
    }

    public String getAppLable(Context context, ApplicationInfo applicationInfo) {
        PackageManager packageManager = context.getPackageManager();
        return (String) (applicationInfo != null ? packageManager.getApplicationLabel(applicationInfo) : "Unknown");
    }

    public AppInfo request(String packageName, OnRequestListener listener)
            throws PackageManager.NameNotFoundException {
        PackageManager manager = context.getPackageManager();
        PackageInfo packageInfo = manager.getPackageInfo(packageName, 0);
        ApplicationInfo applicationInfo = packageInfo.applicationInfo;

        return extractInfo(manager, packageInfo, applicationInfo, listener);
    }

    public interface OnRequestListener {
        void onAppInfoChanged(AppInfo appInfo);
    }


}
