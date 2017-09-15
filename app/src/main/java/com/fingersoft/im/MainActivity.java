package com.fingersoft.im;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fingersoft.im.dialog.DialogActivity;
import com.fingersoft.im.mdm.MDMActivity;
import com.mobile.device.manage.Common;
import com.mobile.device.manage.core.DataResolve;
import com.mobile.device.manage.core.MDMService;
import com.mobile.device.manage.util.DeviceHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    Button btn_mdm;
    Button btn_ts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        int largeHeap = 0;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            largeHeap = activityManager.getLargeMemoryClass();
        }
        Log.d("heap", largeHeap + "");
        initView();
        initListener();
    }

    private void initListener() {
        btn_mdm.setOnClickListener(this);
        btn_ts.setOnClickListener(this);
    }

    private void initView() {
        btn_mdm = (Button) findViewById(R.id.btn_mdm);
        btn_ts = (Button) findViewById(R.id.btn_other);

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_mdm:
                startActivity(new Intent(view.getContext(), MDMActivity.class));
                break;
            case R.id.btn_other:
//                Log.d("DeviceID", DeviceHelper.getDeviceIDFromSP(view.getContext()));
//                requestCommand(view.getContext());
//                try {
//                    String fileName = "crash.log";
//                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
//                        String path = Environment.getExternalStorageDirectory() + "/mdmlogs/";
//                        File logpath = new File(path + fileName);
//                        if (!new File(path).exists()) {
//                            logpath.getParentFile().mkdirs();
//                        }
//                        logpath.createNewFile();
//
//                    }
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//                startActivity(new Intent(view.getContext(), DialogActivity.class));
//                requestCommand(view.getContext());
                ArrayList arrayList = new ArrayList();
                arrayList.add("DeviceType");
                arrayList.add("DeviceSystem");
                arrayList.add("SIMInfomation");
                arrayList.add("DeviceStatus");
                arrayList.add("SecureStatus");
                arrayList.add("NetworkType");
                arrayList.add("AppVersion");
                arrayList.add("GetPasswordConfig");
                arrayList.add("SecurityStatus");
                try {
                    JSONObject jsonObject = MDMService.getInstance().getBasicDeviceInfo(arrayList);
                    Log.d("MDM",jsonObject.toString());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    //
    public void requestCommand(final Context context) {
        String url = "http://113.247.240.28:8082/interface/m/auth";
        StringRequest requestCommand = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.d("res", s);
//                new DataResolve().execute(s);
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
                map.put("j_ecode", "zoomlion");
                map.put("j_username", "00006679");
                map.put("j_password", "zlzk@157");
                map.put("deviceId", DeviceHelper.getDeviceIDFromSP(context));
                return map;
            }
        };
        Common.getRequestQueue().add(requestCommand);
    }
}
