package com.app.infideap.androiddevicetracking;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.app.infideap.devicetrack.entity.DeviceInfo;
import com.app.infideap.devicetrack.process.DeviceInformation;
import com.app.infideap.devicetrack.util.Utils;

import java.util.Locale;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceInfoFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceInfoFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    public DeviceInfoFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment DeviceInfoFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static DeviceInfoFragment newInstance() {
        DeviceInfoFragment fragment = new DeviceInfoFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_device_info, container, false);

        DeviceInformation information = new DeviceInformation(getContext());
        DeviceInfo info = information.request(null);
        String[] datetime = Utils.millisToString(info.datetime).split(" ");
        setText(rootView, R.id.textView_timezone, info.timezone);
        setText(rootView, R.id.textView_date, datetime[0]);
        setText(rootView, R.id.textView_time, datetime[1]);
        setText(rootView, R.id.textView_latitude, String.valueOf(info.latitude));
        setText(rootView, R.id.textView_longitude, String.valueOf(info.longitude));
        setText(rootView, R.id.textView_altitude, String.valueOf(info.altitude));
        setText(rootView, R.id.textView_gps_speed,
                String.format(Locale.getDefault(), "%.1f m/s", info.gpsspeed));
        setText(rootView, R.id.textView_accuracy, String.valueOf(info.accuracy));
        setText(rootView, R.id.textView_address, info.address);
        setText(rootView, R.id.textView_network, info.networkClass);
        setText(rootView, R.id.textView_signal_strength, String.valueOf(info.signalStrength));
        setText(rootView, R.id.textView_battery, String.valueOf(info.battery));
        setText(rootView, R.id.textView_memory,
                String.format(Locale.getDefault(),
                        "%.1f/%.1f GB", info.availableMemory / 1024f, info.totalMemory / 1024f));
        setText(rootView, R.id.textView_storage,
                String.format(Locale.getDefault(),
                        "%.1f/%.1f GB", info.availableStorage / 1024f, info.totalStorage / 1024f));

        return rootView;
    }

    private void setText(View rootView, int id, String text) {
        TextView textView = (TextView) rootView.findViewById(id);
        textView.setText(text);
    }


}
