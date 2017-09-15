package com.fingersoft.im.mdm;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.fingersoft.im.R;
import com.mobile.device.manage.Common;
import com.mobile.device.manage.core.DataResolve;
import com.mobile.device.manage.core.MDMService;

import java.util.HashMap;
import java.util.Map;
//import com.mobile.device.manage.core.MDMService;

/**
 * Created by Administrator on 2017/9/1.
 */

public class MDMActivity extends Activity implements View.OnClickListener {

    Button btn_getToken;
    Button btn_regist;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mdm);
        initView();
        initListener();
    }

    private void initListener() {
        btn_getToken.setOnClickListener(this);
        btn_regist.setOnClickListener(this);
    }


    private void initView() {
        btn_getToken = findViewById(R.id.btn_gettoken);
        btn_regist = findViewById(R.id.btn_regist);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_gettoken:
                String token = MDMService.getInstance().getDeviceToken(
                        getApplicationContext());
                Log.d("Token", token);
            case R.id.btn_regist:
                request();
                break;
        }
    }

    public void request() {
        SharedPreferences sharedPreferences = MDMService.getInstance().staticContext.getSharedPreferences("mdm_common", Context.MODE_PRIVATE);
        String url = sharedPreferences.getString("RequestUrl", "");
        StringRequest requestCommand = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String s) {
                Log.d("res", s);
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
                map.put("RequestId", "e0c858db2d4c4ce793a2672befca2345");
                return map;
            }
        };
        Common.getRequestQueue().add(requestCommand);
    }
}
