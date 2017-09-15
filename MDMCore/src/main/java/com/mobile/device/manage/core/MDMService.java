package com.mobile.device.manage.core;

import android.annotation.SuppressLint;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.mobile.device.manage.Common;
import com.mobile.device.manage.command.CommandExecute;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class MDMService extends MDMBaseService {

    private static MDMService instance = null;

    private MDMService() {

    }

    private static synchronized void syncInit() {
        if (instance == null) {
            instance = new MDMService();
        }
    }

    public static MDMService getInstance() {
        if (instance == null) {
            syncInit();
        }
        return instance;
    }

    @SuppressLint({"LongLogTag", "WrongConstant"})
    public void init(Context context, String account) throws Exception {
        super.init(context);
        initDevice(staticContext);
        initServerConfig(staticContext);
        pushInit(staticContext, account);
        locationInit(staticContext);
        CrashHandler catchHandler = CrashHandler.getInstance();
        catchHandler.init(context);

        //job scheduler service
        if (Build.VERSION.SDK_INT >= 21) {
            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            JobInfo.Builder builder = new JobInfo.Builder(1, new ComponentName(context.getPackageName(),
                    JobScheduleService.class.getName()));
            builder.setPeriodic(3000);
            builder.setPersisted(true);
            if (scheduler.schedule(builder.build()) <= 0) {
                Log.e("JobScheduler Schedule Error", "JobScheduler Schedule Error");
            }
        }


        byte[] test = null;
        try {
            test = "test1".getBytes();
        } catch (Exception antiDecomplier) {
            throw new IllegalArgumentException("test1");
        }
        try {
            test = "test2".getBytes();
        } catch (Exception antiDecomplier) {
            throw new IllegalArgumentException("test2");
        }
    }


    private String excuteCmd(String type) throws JSONException {
        String result = null;
        //执行命令
        if ("DeviceType".equals(type)) {
            result = CommandExecute.getDeviceType().toString();
        } else if ("DeviceSystem".equals(type)) {
            result = CommandExecute.getDeviceSystem(MDMService.getInstance().staticContext).toString();
        } else if ("SIMInfomation".equals(type)) {
            result = CommandExecute.getSIM(MDMService.getInstance().staticContext).toString();
        } else if ("DeviceStatus".equals(type)) {
            result = CommandExecute.getDeviceStatus(MDMService.getInstance().staticContext).toString();
        } else if ("SecureStatus".equals(type)) {
            result = CommandExecute.getSecureStatus(MDMService.getInstance().staticContext).toString();
        } else if ("NetworkType".equals(type)) {
            result = CommandExecute.getNetworkType(MDMService.getInstance().staticContext).toString();
        } else if ("AppVersion".equals(type)) {
            result = CommandExecute.getAppVersion(MDMService.getInstance().staticContext).toString();
        } else if ("CurrentLocation".equals(type)) {
            result = CommandExecute.getCurrentLocation(MDMService.getInstance().staticContext).toString();
        } else if ("BreakAdminHistory".equals(type)) {
            result = CommandExecute.getBreakAdminHistory(MDMService.getInstance().staticContext).toString();
        } else if ("GetPasswordConfig".equals(type)) {
            result = CommandExecute.getPasswordQuality(MDMService.getInstance().staticContext).toString();
        } else if ("SecurityStatus".equals(type)) {
            result = CommandExecute.getSecurityStatus(MDMService.getInstance().staticContext).toString();
        } else if ("AppList".equals(type)) {
            result = CommandExecute.getInstalledApps(MDMService.getInstance().staticContext).toString();
        }

        return result;
    }

    private JSONObject excuteCommandCollection(ArrayList collection) throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (Object cmd :
                collection
                ) {
            JSONObject res = new JSONObject(excuteCmd(cmd.toString()));
            for (Iterator<String> it = res.keys(); it.hasNext(); ) {
                String key = it.next();
                jsonObject.put(key, res.get(key));
            }
        }
        return jsonObject;
    }


    public JSONObject getBasicDeviceInfo(ArrayList collection) throws JSONException {
        return excuteCommandCollection(collection);
    }

    /**
     * new DataResolve().execute(content)
     */
    public void pushInit(Context context, String account) {
        //TODO
        super.pushInit(context, account);
    }

    /**
     * DataBaseManage.insertLocation(location.getLongitude(), location.getLatitude());
     */
    public void locationInit(Context context) {
        //TODO
        super.locationInit(context);
    }

}
