package com.app.infideap.devicetrack.entity;


/**
 * Created by Shiburagi on 24/08/2016.
 */
public class AppUsage {
    public int id;
    public String packageName;
    public String datetime;
    public int type;
    public int status;

    public AppUsage(String packageName, String datetime, int type) {
        this.packageName = packageName;
        this.datetime = datetime;
        this.type = type;
    }

    public AppUsage() {

    }
}
