package com.app.infideap.devicetrack.util;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Shiburagi on 09/10/2016.
 */
public class Utils {
    public static double toMB(long b) {
        return b / 1024f / 1024f;
    }

    public static double getTotalRamBelowJellyBean() {
        RandomAccessFile reader = null;
        String load = null;
        DecimalFormat twoDecimalForm = new DecimalFormat("#.##");
        double totRam = 0;
        String lastValue = "";
        try {
            reader = new RandomAccessFile("/proc/meminfo", "r");
            load = reader.readLine();

            // Get the Number value from the string
            Pattern p = Pattern.compile("(\\d+)");
            Matcher m = p.matcher(load);
            String value = "";
            while (m.find()) {
                value = m.group(1);
                // System.out.println("Ram : " + value);
            }
            reader.close();

            totRam = Double.parseDouble(value);

        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            // Streams.close(reader);
        }

        return toMB((long) totRam);

    }

    public static String millisToString(long datetime) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(datetime);
        SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss", Locale.getDefault());

        return format.format(calendar.getTime());
    }
}
