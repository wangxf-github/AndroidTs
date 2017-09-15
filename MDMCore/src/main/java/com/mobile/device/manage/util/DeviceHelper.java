package com.mobile.device.manage.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.provider.Settings;
import android.telephony.TelephonyManager;

import java.util.UUID;

/**
 * Created by Administrator on 2017/9/6.
 */

public class DeviceHelper {
    /**
     * 获取设备唯一id
     *
     * @param context
     * @return
     */
    public static String getDeviceID(Context context) {
        String device_id = null;
        final String androidId = Settings.Secure.getString(
                context.getContentResolver(), Settings.Secure.ANDROID_ID);
        if (!"9774d56d682e549c".equals(androidId)) {
            device_id = androidId;
        } else {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            String imei = tm.getDeviceId();
            device_id = (imei != null && imei != "") ? imei : UUID.randomUUID().toString();
        }
        return device_id;
    }

    public static String getDeviceIDFromSP(Context context) {
        String deviceID = null;
        SharedPreferences prefs = context
                .getSharedPreferences("mdm_common", 0);
        String id = prefs.getString("device_id", null);
        if (id != null && id != "") {
            deviceID = id;
        } else {
            deviceID = DeviceHelper.getDeviceID(context);
        }
        return deviceID;
    }

}
