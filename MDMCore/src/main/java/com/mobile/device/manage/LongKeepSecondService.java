package com.mobile.device.manage;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;

public class LongKeepSecondService extends Service {

	Context context;
	private boolean isrunning = false;
	private int u;
	private static Handler longTermHandler;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = this;
		isrunning = true;
		
		u = 0;
		longTermHandler = new Handler() {
			
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				
				if(isrunning) {
					if(!Common.isServiceRunning(LongKeepService.class, context)) {
						Intent intent = new Intent(context, LongKeepService.class);
						startService(intent);
					};
					if(u==10)
						u = 0;
					u++;
				}
				longTermHandler.sendEmptyMessageDelayed(1, 1000);
			}
			
		};
		longTermHandler.sendEmptyMessageDelayed(1, 1000);
		
		return START_STICKY;
    }
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public void onDestroy() {
		isrunning = false;
		if(!Common.isServiceRunning(LongKeepService.class, context)) {
			Intent intent = new Intent(context, LongKeepService.class);
			startService(intent);
		};
		super.onDestroy();
    }
	
}
