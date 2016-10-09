package com.app.infideap.devicetrack.process;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;

import com.app.infideap.devicetrack.entity.DeviceInfo;
import com.app.infideap.devicetrack.service.GPSTracker;
import com.app.infideap.devicetrack.util.Utils;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import gapchenko.llttz.Converter;
import gapchenko.llttz.IConverter;
import gapchenko.llttz.stores.TimeZoneListStore;

/**
 * Created by Zariman on 28/3/2016.
 */
public class DeviceInformation {
    public static final String TAG = DeviceInformation.class.getSimpleName();

    private final Context context;
    private DeviceInfo deviceInfo;
    private GPSTracker gps;

    public DeviceInformation(Context context) {
        this.context = context;
    }

    public DeviceInfo request(final OnReadDeviceInformationListener listener) {
        deviceInfo = new DeviceInfo();
        getMemoryInfo();
        getPowerInfo();

        gps = new GPSTracker(context, new GPSTracker.OnChangedListener() {
            @Override
            public void onChanged(Location location) {
                deviceInfo.latitude = location.getLatitude();
                deviceInfo.longitude = location.getLongitude();
                deviceInfo.gpsspeed = location.getSpeed();
                deviceInfo.accuracy = location.getAccuracy();

                if (listener != null)
                    listener.onLocationUpdate(location);

                gps.stopUsingGPS();

            }
        });



        date();
        gpsInfo();
        signalStrength();
        deviceInfo.networkClass = getNetworkClass();


        return deviceInfo;


    }

    private void getPowerInfo() {
        Intent batteryIntent =
                context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        assert batteryIntent != null;
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);

        // Error checking that probably isn't needed but I added just in case.
        if (level == -1 || scale == -1) {
            deviceInfo.battery = 50.0f;
        }

        deviceInfo.battery = ((float) level / (float) scale) * 100.0f;
    }

    private void date() {
        IConverter iconv = Converter.getInstance(TimeZoneListStore.class);
        TimeZone tz2 = iconv.getTimeZone(deviceInfo.latitude, deviceInfo.longitude);

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        Date currentLocalTime = cal.getTime();
        DateFormat date = new SimpleDateFormat("Z");
        deviceInfo.datetime = cal.getTimeInMillis();
        deviceInfo.timezone = date.format(currentLocalTime);
    }

    private void getMemoryInfo() {
        ActivityManager.MemoryInfo mi = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(mi);
        double availableMegs = Utils.toMB(mi.availMem);
        double totalMegs = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN) {
            totalMegs = Utils.toMB(mi.totalMem);
        } else {
            totalMegs = Utils.getTotalRamBelowJellyBean();
        }

        deviceInfo.availableMemory = availableMegs;
        deviceInfo.totalMemory = totalMegs;

        deviceInfo.availableStorage = getAvailableInternalMemorySize();
        deviceInfo.totalStorage = getTotalInternalMemorySize();
    }

    private double getAvailableInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long availableBlocks = stat.getAvailableBlocks();
        return Utils.toMB(availableBlocks * blockSize);
    }

    private double getTotalInternalMemorySize() {
        File path = Environment.getDataDirectory();
        StatFs stat = new StatFs(path.getPath());
        long blockSize = stat.getBlockSize();
        long totalBlocks = stat.getBlockCount();
        return Utils.toMB(totalBlocks * blockSize);
    }

    private void gpsInfo() {
        if (gps.canGetLocation()) {
            deviceInfo.latitude = gps.getLatitude();
            deviceInfo.longitude = gps.getLongitude();
            deviceInfo.accuracy = gps.getAccuracy();
            deviceInfo.altitude = gps.getAltitude();
            deviceInfo.gpsspeed = gps.getSpeed();

        }

        deviceInfo.address = "";
        Geocoder geocoder = new Geocoder(context);
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(
                    deviceInfo.latitude,
                    deviceInfo.longitude,
                    // In this sample, get just a single address.
                    1);
        } catch (IOException | IllegalArgumentException ioException) {
            ioException.printStackTrace();
        }

        // Handle case where no address was found.
        if (addresses == null || addresses.size() == 0) {

        } else {
            Address address = addresses.get(0);
            // Fetch the address lines using getAddressLine,
            // join them, and send them to the thread.
            for (int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                deviceInfo.address += address.getAddressLine(i);
                if (i < address.getMaxAddressLineIndex() - 1) {
                    deviceInfo.address += "\n";
                }
            }

        }
    }

    private void signalStrength() {
        myPhoneStateListener psListener = new myPhoneStateListener();
        TelephonyManager telephonyManager =
                (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        telephonyManager.listen(psListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        deviceInfo.carrierName = telephonyManager.getNetworkOperatorName();
    }


    public String getNetworkClass() {
        TelephonyManager mTelephonyManager = (TelephonyManager)
                context.getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = mTelephonyManager.getNetworkType();

        String networkTypeCode = String.format(
                Locale.getDefault(),
                "|%d",
                networkType
        );

        switch (networkType) {
            case TelephonyManager.NETWORK_TYPE_EDGE:
                return "EDGE".concat(networkTypeCode);
            case TelephonyManager.NETWORK_TYPE_GPRS:
            case TelephonyManager.NETWORK_TYPE_CDMA:
            case TelephonyManager.NETWORK_TYPE_1xRTT:
            case TelephonyManager.NETWORK_TYPE_IDEN:
                return "GPRS".concat(networkTypeCode);
            case TelephonyManager.NETWORK_TYPE_UMTS:
            case TelephonyManager.NETWORK_TYPE_EVDO_0:
            case TelephonyManager.NETWORK_TYPE_EVDO_A:
            case TelephonyManager.NETWORK_TYPE_HSDPA:
            case TelephonyManager.NETWORK_TYPE_HSUPA:
            case TelephonyManager.NETWORK_TYPE_HSPA:
            case TelephonyManager.NETWORK_TYPE_EVDO_B:
            case TelephonyManager.NETWORK_TYPE_EHRPD:
            case TelephonyManager.NETWORK_TYPE_HSPAP:
                return "3G".concat(networkTypeCode);
            case TelephonyManager.NETWORK_TYPE_LTE:
                return "LTE".concat(networkTypeCode);
            default:
                return "Unknown";
        }
    }


    public DeviceInfo getDeviceInfo() {
        return deviceInfo;
    }

    public class myPhoneStateListener extends PhoneStateListener {
        private final String TAG = myPhoneStateListener.class.getSimpleName();
        public int signalStrengthValue;

        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (signalStrength.isGsm()) {
                Log.d(TAG, "GSM");

                if (signalStrength.getGsmSignalStrength() != 99)
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
                else
                    signalStrengthValue = signalStrength.getGsmSignalStrength();
                deviceInfo.signalStrengthType = "GSM";
            } else {
                Log.d(TAG, "DBM");
                signalStrengthValue = signalStrength.getCdmaDbm();
                deviceInfo.signalStrengthType = "Other";
            }

//            if (signalStrengthValue > 30) {
//                signalStrengthValue = 2;
//            } else if (signalStrengthValue > 20) {
//                signalStrengthValue = 1;
//            } else {
//                signalStrengthValue = 0;
//            }
            deviceInfo.signalStrength = signalStrengthValue;
        }
    }

    public interface OnReadDeviceInformationListener {

        void onLocationUpdate(Location location);
    }
}
