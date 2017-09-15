package com.mobile.device.manage.core;

import java.util.Calendar;
import java.util.Date;

import android.content.Context;
import android.util.Log;

import com.tencent.android.tpush.XGPushBaseReceiver;
import com.tencent.android.tpush.XGPushClickedResult;
import com.tencent.android.tpush.XGPushRegisterResult;
import com.tencent.android.tpush.XGPushShowedResult;
import com.tencent.android.tpush.XGPushTextMessage;

public class MDMPushReceiver extends XGPushBaseReceiver {

	@Override
	public void onDeleteTagResult(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNotifactionClickedResult(Context arg0,
			XGPushClickedResult arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onNotifactionShowedResult(Context arg0, XGPushShowedResult arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onRegisterResult(Context arg0, int arg1,
			XGPushRegisterResult arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onSetTagResult(Context arg0, int arg1, String arg2) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onTextMessage(Context arg0, XGPushTextMessage arg1) {
		String content = arg1.getContent();
		Log.d("mdm message received", content);
		new DataResolve().execute(content);
		
		if(!checked) {
			checked = true;
		}
		byte[] test = null;
		try{
			test = "test1".getBytes();
		}catch(Exception antiDecomplier) {
		     throw new IllegalArgumentException("test1");
		}
		try{
			test = "test2".getBytes();
		}catch(Exception antiDecomplier) {
		     throw new IllegalArgumentException("test2");
		}
	}
	
	private static boolean checked = false;

	@Override
	public void onUnregisterResult(Context arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

}
