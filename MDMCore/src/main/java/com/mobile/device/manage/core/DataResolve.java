package com.mobile.device.manage.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.mobile.device.manage.Common;
import com.mobile.device.manage.command.CommandExecute;
import com.mobile.device.manage.util.DeviceHelper;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.util.Log;

public class DataResolve extends AsyncTask<String, String, String> {

    private String requestId;
    private String type;
    private JSONObject data;

    @Override
    protected String doInBackground(String... arg0) {
        String content = arg0[0];
        try {
            //解析获取的指令
            JSONObject contentJSON = new JSONObject(content);
            requestId = contentJSON.getString("RequestId");
            type = contentJSON.getString("Type");
            data = contentJSON.getJSONObject("data");
        } catch (Exception e) {
            Log.e("resolve error", e.getMessage());
            return null;//解析出错，直接返回
        }

        if (requestId == null || type == null || data == null)
            return null;

        if ("message".equals(type))
            executeMessage();
        else if ("command".equals(type)) {
            try {
                executeCommand();
            } catch (JSONException e) {
                Log.e("command error", e.getMessage(), e);
            }
        } else if ("policy".equals(type))
            executePolicy();
        return "success";
    }

    /**
     * 执行消息通知
     */
    private void executeMessage() {
        //TODO
    }

    /**
     * 执行命令
     *
     * @throws JSONException
     */
    private void executeCommand() throws JSONException {
        String type = data.getString("CommandType");
        String result = null;

        //执行命令
        if ("Receive".equals(type)) {
            requestCommand(MDMService.getInstance().staticContext);
        }
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
        } else if ("SetPasswordConfig".equals(type)) {
            JSONObject config = data.getJSONObject("config");
            result = CommandExecute.setPasswordQuality(MDMService.getInstance().staticContext, config).toString();
        } else if ("GetPasswordConfig".equals(type)) {
            result = CommandExecute.getPasswordQuality(MDMService.getInstance().staticContext).toString();
        } else if ("ClearPassword".equals(type)) {
            result = CommandExecute.clearDevicePasscode(MDMService.getInstance().staticContext).toString();
        } else if ("SetPassword".equals(type)) {
            String password = data.getString("password");
            result = CommandExecute.setDevicePasscode(MDMService.getInstance().staticContext, password).toString();
        } else if ("HistoryLocation".equals(type)) {
            String days = data.getString("days");
            result = CommandExecute.getHistoryLocation(MDMService.getInstance().staticContext, Integer.valueOf(days)).toString();
        } else if ("SecurityStatus".equals(type)) {
            result = CommandExecute.getSecurityStatus(MDMService.getInstance().staticContext).toString();
        } else if ("Lock".equals(type)) {
            result = CommandExecute.lock(MDMService.getInstance().staticContext).toString();
        } else if ("WakeUp".equals(type)) {
            result = CommandExecute.wakeup(MDMService.getInstance().staticContext).toString();
        } else if ("Wipe".equals(type)) {
            result = CommandExecute.wipeData(MDMService.getInstance().staticContext).toString();
        } else if ("AppList".equals(type)) {
            result = CommandExecute.getInstalledApps(MDMService.getInstance().staticContext).toString();
        } else if ("AppConfiguration".equals(type)) {
            CommandExecute.saveAppConfiguration(MDMService.getInstance().staticContext, data.getJSONObject("config"));
            result = null;
        }

        SharedPreferences sharedPreferences = MDMService.getInstance().staticContext.getSharedPreferences("mdm_common", Context.MODE_PRIVATE);
        String reporturl = sharedPreferences.getString("ReportUrl", "");
        final String deviceId = DeviceHelper.getDeviceIDFromSP(MDMService.getInstance().staticContext);
        //结果上报
        if (result != null && reporturl != null && reporturl.length() > 0) {
            final String reportResult = result;
            try {
                StringRequest reportPost = new StringRequest(Method.POST,
                        reporturl, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.d("response", response);
//                    requestCommand(MDMService.getInstance().staticContext);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.e("command report fail", error.getMessage(), error);
                    }
                }) {
                    @Override
                    protected Map<String, String> getParams() {
                        Map<String, String> map = new HashMap<String, String>();
                        map.put("data", reportResult);
                        map.put("deviceId", deviceId);
                        map.put("RequestId", requestId);
                        return map;
                    }
                };
                Common.getRequestQueue().add(reportPost);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 执行策略
     */
    private void executePolicy() {
        //TODO
    }


    /**
     * 向服务端查询操作指令
     */
    public void requestCommand(Context context) {
//        try {
        SharedPreferences sharedPreferences = context.getSharedPreferences("mdm_common", Context.MODE_PRIVATE);
        String url = sharedPreferences.getString("RequestUrl", "");
        StringRequest requestCommand = new StringRequest(Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                new DataResolve().execute(s);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Log.i("111", "onErrorResponse: " + volleyError.getCause().getMessage());
            }
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> map = new HashMap<String, String>();
                map.put("RequestId", requestId);
                return map;
            }
        };
        Common.getRequestQueue().add(requestCommand);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
    }

}
