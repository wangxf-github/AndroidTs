package com.mobile.device.manage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.json.JSONObject;

import android.app.Service;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.content.Intent;
import android.content.SharedPreferences.Editor;
import android.os.PowerManager.WakeLock;
import android.util.Log;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.Request.Method;
import com.android.volley.toolbox.StringRequest;
import com.mobile.device.manage.core.MDMService;

public class LongKeepService extends Service {

	private Context context;
	private static WakeLock wl;
	private static Handler longTermHandler;
	private boolean isrunning = false;
	private int i;
	private static String reportUrl;
	private static int reportInterval = -1;
	private static int reportTick = 0;

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		context = this;
		if(wl==null) {
			PowerManager pm = (PowerManager)getSystemService(POWER_SERVICE);
			wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "RunningAllTheTime");
			wl.acquire();
		}

//		Notification.Builder builder = new Notification.Builder(this);
//		builder.setOngoing(true)
//				.setSmallIcon(R.drawable.ic_launcher)
//				.setContentTitle("MDM Service")
//				.setContentText("MDM Service is running!");
//		Notification notify = builder.build();
//		notify.flags = Notification.FLAG_NO_CLEAR | Notification.FLAG_ONGOING_EVENT;
//		startForeground(100, notify);
		Log.d("MDM","mdm service running....");
		isrunning = true;

		Properties p = null;
		if(reportUrl==null) {
			try {
				InputStream is = context.getAssets().open("config.properties");
				p = new Properties();
				p.load(is);
				String rIp = p.getProperty("Server");
				String rUrl = p.getProperty("ReportUrl");
				reportUrl = rIp + rUrl;
				is.close();
			} catch (Exception e) {
				Log.e("resolve config error", e.getMessage(), e);
			}
		}
		if(reportInterval==-1) {
			try {
				int rInter = Integer.valueOf(p.getProperty("reportInterval"));
				if(rInter>=60) {
					reportInterval = rInter;
				}
			} catch (Exception e) {
				Log.e("resolve config error", e.getMessage(), e);
			}
		}

		i = 0;
		reportTick = 0;
		longTermHandler = new Handler() {

			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);

				if(isrunning) {
					if(!Common.isServiceRunning(LongKeepSecondService.class, context)) {
						Intent intent = new Intent(context, LongKeepSecondService.class);
						startService(intent);
					};
					if(i>=5) {
						i = 0;
						checkReport();
					}
					if(reportTick>=reportInterval) {
						reportTick = 0;
						checkUpReport();
					}
					i++;reportTick++;
				}
				longTermHandler.sendEmptyMessageDelayed(0, 1000);
			}
		};
		longTermHandler.sendEmptyMessageDelayed(0, 1000);
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onDestroy() {
		isrunning = false;
		if(!Common.isServiceRunning(LongKeepSecondService.class, context)) {
			Intent intent = new Intent(context, LongKeepSecondService.class);
			startService(intent);
		};
		super.onDestroy();
	}


	private void checkReport() {
		SharedPreferences sp = context.getSharedPreferences("mdm_common",
				Context.MODE_PRIVATE);
		String stored = sp.getString("report", "");
		if(stored==null || "".equals(stored))
			return;

		//上报接口检查
		if(reportUrl==null) {
			try {
				InputStream is = context.getAssets().open("config.properties");
				Properties p = new Properties();
				p.load(is);
				String rIp = p.getProperty("Server");
				String rUrl = p.getProperty("ReportUrl");
				reportUrl = rIp + rUrl;
				is.close();
			} catch (Exception e) {
				Log.e("resolve config error", e.getMessage(), e);
			}
		}
		if(reportUrl==null)
			return;

		//上报
		final String reportContent = stored;
		StringRequest reportPost = new StringRequest(Method.POST,
				reportUrl, new Response.Listener<String>() {
			@Override
			public void onResponse(String response) {
				clearStoredReport();//成功之后清空上报内容
				Log.d("event report success", "response");
			}
		}, new Response.ErrorListener() {

			@Override
			public void onErrorResponse(VolleyError error) {
				Log.e("event report fail", error.getMessage(), error);
			}
		}) {
			@Override
			protected Map<String, String> getParams() {
				Map<String, String> map = new HashMap<String, String>();
				map.put("result", reportContent);
				return map;
			}
		};
		Common.getRequestQueue().add(reportPost);
	}

	private void checkUpReport() {
		//上报接口检查
		if(reportUrl==null) {
			try {
				InputStream is = context.getAssets().open("config.properties");
				Properties p = new Properties();
				p.load(is);
				String rIp = p.getProperty("Server");
				String rUrl = p.getProperty("ReportUrl");
				reportUrl = rIp + rUrl;
				is.close();
			} catch (Exception e) {
				Log.e("resolve config error", e.getMessage(), e);
			}
		}
		if(reportUrl==null)
			return;

		JSONObject collection = new JSONObject();
		//location info
		try{
			JSONObject locationResult = DataBaseManage.getLatestLocation();
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String dateValue = dateFormat.format(new Date());
			locationResult.put("date", dateValue);
			collection.put("LocationInfo", locationResult);
		}catch(Exception e) {
			Log.e("JSONException", e.getMessage(), e);
		}
		//crash info
		try{
			String crashPath = Environment.getExternalStorageDirectory()
					+ "/mdmlogs/crash.log";
			String crashResult = readFromFile(crashPath);
			if(crashResult!=null && crashResult.length()>0) {
				collection.put("CrashInfo", crashResult);
				new File(crashPath).delete();
			}
		}catch(Exception e) {
		}
		//log info
		try{
			String logPath = Environment.getExternalStorageDirectory()
					+ "/mdmlogs/mdm.log";
			String logResult = readFromFile(logPath);
			if(logResult!=null && logResult.length()>0) {
				collection.put("LogInfo", logResult);
				new File(logPath).delete();
			}
		}catch(Exception e) {
		}

		String result = null;
		result = collection.toString();
		//结果上报
		if(result!=null && reportUrl!=null && reportUrl.length()>0) {
			final String reportResult = result;
			StringRequest reportPost = new StringRequest(Method.POST,
					reportUrl, new Response.Listener<String>() {
				@Override
				public void onResponse(String response) {
					Log.d("command report success", "");
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
					map.put("result", reportResult);
					map.put("token", MDMService.getInstance().getDeviceToken(context));
					map.put("CommandType", "CurrentLocation");
					return map;
				}
			};
			Common.getRequestQueue().add(reportPost);
		}
	}

	private String readFromFile(String filePath) throws Exception {
		String result = "";
		if(!new File(filePath).exists())
			return result;

		FileInputStream fis = null;
		String s;
		fis = new FileInputStream(filePath);
		BufferedReader reader = new BufferedReader(new InputStreamReader(fis, "UTF-8"));
		while(true){
			s = reader.readLine();
			if(s == null)
				break;
			result += s;
		}
		if(reader!=null)
			reader.close();
		if(fis!=null)
			fis.close();
		return result.trim();
	}

	private void clearStoredReport() {
		SharedPreferences sp = context.getSharedPreferences("mdm_common",
				Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("report", "");
		editor.commit();
	}

}
