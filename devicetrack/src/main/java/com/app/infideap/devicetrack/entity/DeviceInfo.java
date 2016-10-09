package com.app.infideap.devicetrack.entity;

import java.io.Serializable;

/**
 * Created by Zariman on 28/3/2016.
 */
public class DeviceInfo implements Serializable {


    public double latitude;

    public double longitude;

    public float accuracy;

    public int signalStrength;

    public String networkClass;

    public double altitude;

    public String address;

    public long datetime;

    public String timezone;

    public String carrierName;

    public float gpsspeed;

    public String signalStrengthType = "Other";

    public double battery;

    public double availableMemory;

    public double totalMemory;

    public double availableStorage;

    public double totalStorage;
}
