package com.mobile.device.manage.core;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.admin.DeviceAdminReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Log;

public class DeviceAdmin extends DeviceAdminReceiver {

    @Override
    public void onEnabled(Context context, Intent intent) {
        super.onEnabled(context, intent);
    }

    @Override
    public CharSequence onDisableRequested(Context context, Intent intent) {
        return super.onDisableRequested(context, intent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
    }

    @Override
    public void onDisabled(Context context, Intent intent) {
        super.onDisabled(context, intent);
        try{
            //获取待上报内容
            SharedPreferences sp = context.getSharedPreferences("mdm_common",
                    Context.MODE_PRIVATE);
            JSONArray reportArray = null;
            String stored = sp.getString("report", null);
            if(stored==null || "".equals(stored))
                reportArray = new JSONArray();
            else
                reportArray = new JSONArray(stored);

            //添加上报——取消设备管理器权限
            JSONObject disableReport = new JSONObject();
            disableReport.put("type", "AdminDisabled");
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            disableReport.put("date", dateFormat.format(new Date()));
            reportArray.put(disableReport);

            //存储
            Editor editor = sp.edit();
            editor.putString("report", reportArray.toString());
            editor.commit();
        }catch(Exception e) {
            Log.e("onDisabled error", e.getMessage(), e);
        }
    }

    @Override
    public void onPasswordChanged(Context context, Intent intent) {
        super.onPasswordChanged(context, intent);
    }

    @Override
    public void onPasswordFailed(Context context, Intent intent) {
        super.onPasswordFailed(context, intent);
    }

    @Override
    public void onPasswordSucceeded(Context context, Intent intent) {
        super.onPasswordSucceeded(context, intent);
    }

    @Override
    public void onPasswordExpiring(Context context, Intent intent) {
        super.onPasswordExpiring(context, intent);
    }

    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context.getApplicationContext(), DeviceAdmin.class);
    }

}
