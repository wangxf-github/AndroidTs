package com.mobile.device.manage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LongKeepReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		boolean running = Common.isServiceRunning(LongKeepService.class, context);
		if(!running) {
			Intent serviceIntent = new Intent(context, LongKeepService.class);
			context.startService(serviceIntent);
		}
	}

}
