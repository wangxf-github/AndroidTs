package com.mobile.device.manage.core;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

public class ChargeReceiver extends BroadcastReceiver {
	
	@Override
	public void onReceive(Context context, Intent intent) {
		if(intent!=null && Intent.ACTION_BATTERY_CHANGED==intent.getAction()) {
			int level = intent.getIntExtra("level", 0);
			int scale = intent.getIntExtra("scale", 100);
			float charge = (level*100)/scale;
			SharedPreferences sp = context.getSharedPreferences("battery", 
					Context.MODE_PRIVATE);
			Editor editor = sp.edit();
			editor.putString("charge", String.valueOf(charge/100));
			editor.commit();
		}
	}

}
