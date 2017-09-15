package com.mobile.device.manage;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
import com.mobile.device.manage.core.MDMService;

import android.app.ActivityManager;
import android.content.Context;
import android.app.ActivityManager.RunningServiceInfo;

public class Common {
	
	public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}
	
	public static RequestQueue getRequestQueue() {
		MDMService service = MDMService.getInstance();
		if (service.mRequestQueue == null && service.staticContext!=null)
			service.mRequestQueue = Volley.newRequestQueue(service.staticContext);
		return service.mRequestQueue;
	}
	
}
