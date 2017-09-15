package com.mobile.device.manage.command;

import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.KeyguardManager;
import android.app.KeyguardManager.KeyguardLock;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Environment;
import android.os.StatFs;
import android.telephony.TelephonyManager;
import android.telephony.gsm.GsmCellLocation;
import android.util.Log;

import com.mobile.device.manage.DataBaseManage;
import com.mobile.device.manage.core.ChargeReceiver;
import com.mobile.device.manage.core.DeviceAdmin;
import com.mobile.device.manage.core.MDMBaseService;

public class BaseMethod {

	/**
	 * 设备厂商
	 * @return
	 */
	public String getFactory() {
		return android.os.Build.MANUFACTURER;
	}

	/**
	 * 设备型号
	 * @return
	 */
	public String getDeviceModel() {
		return android.os.Build.MODEL;
	}

	/**
	 * 系统版本
	 * @return
	 */
	public String getSystemVersion() {
		return android.os.Build.VERSION.RELEASE;
	}

	/**
	 * 系统内存
	 * @return
	 * 单位M
	 */
	public String getMemory() {
		String memory = "";
		FileReader fr = null;
		BufferedReader br = null;
		try{
			fr = new FileReader("/proc/meminfo");
			br = new BufferedReader(fr, 1024*8);
			String read = br.readLine();
			String memoryCount = read.split("\\s+")[1];
			float result = Float.parseFloat(memoryCount)/1024;
			memory = String.valueOf(result);
		}catch(Exception e) {
			Log.e("getMemory error", e.getMessage(), e);
		}finally {
			try {
				if(fr!=null)
					fr.close();
				if(br!=null)
					br.close();
			} catch (IOException e) {
			}
		}
		return memory;
	}

	/**
	 * 获取存储总容量
	 * @return
	 * 单位M
	 */
	public float getStorageTotal() {
		File dataDirectory = Environment.getDataDirectory();
		StatFs fs = new StatFs(dataDirectory.getPath());
		return fs.getBlockSize()*(((float)fs.getBlockCount())/1024/1024);
	}

	/**
	 * 获取存储已使用容量
	 * @return
	 * 单位M
	 */
	public float getStorageUsed() {
		File dataDirectory = Environment.getDataDirectory();
		StatFs fs = new StatFs(dataDirectory.getPath());
		float totalCount = (float)fs.getBlockCount();
		float availableCount = (float)fs.getAvailableBlocks();
		return fs.getBlockSize()*((totalCount-availableCount)/1024/1024);
	}

	/**
	 * 获取电量剩余百分比
	 * @param context
	 * @return
	 */
	public String getCharge(Context context) {
		if(chargeReceiver!=null) {
			try{
				context.unregisterReceiver(chargeReceiver);
			}catch(Exception e) {
			}
		}
		chargeReceiver = new ChargeReceiver();
		context.registerReceiver(chargeReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

		SharedPreferences sp = context.getSharedPreferences("battery", Context.MODE_PRIVATE);
		return sp.getString("charge", "");
	}


	private TelephonyManager teleManager = null;

	/**
	 * 获取IMEI
	 * @param context
	 * @return
	 */
	public String getIMEI(Context context) {
		if(teleManager==null)
			teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if(teleManager.getPhoneType()==1)
			return teleManager.getDeviceId();
		return "";
	}

	/**
	 * 获取MEID
	 * @param context
	 * @return
	 */
	public String getMEID(Context context) {
		if(teleManager==null)
			teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		if(teleManager.getPhoneType()==2)
			return teleManager.getDeviceId();
		return "";
	}

	/**
	 * 获取手机号码
	 *
	 * 注意：
	 * 从技术层面而言，手机的SIM卡上并不会存储手机号码信息，只会存储IMSI
	 * （International Mobile Subscriber Identification Number）。
	 * 手机号码（MSISDN）都是登记在HLR（Home Location Register）中的，
	 * 在HLR中会把IMSI和MSISDN关联在一起。
	 *
	 * Attention:
	 * There is no guaranteed solution to this problem because the phone number is not
	 * physically stored on all SIM-cards, or broadcasted from the network to the phone.
	 * This is especially true in some countries which requires physical address verification,
	 * with number assignment only happening afterwards. Phone number assignment happens on the
	 * network - and can be changed without changing the SIM card or device
	 * (e.g. this is how porting is supported).
	 * (http://stackoverflow.com/questions/2480288/programmatically-obtain-the-phone-number-of-the-android-phone)
	 *
	 * @param context
	 * @return
	 */
	public String getPhoneNumber(Context context) {
		if(teleManager==null)
			teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return teleManager.getLine1Number();
	}

	/**
	 * 获取手机序列号
	 * @param context
	 * @return
	 */
	public String getSN(Context context) {
		if(teleManager==null)
			teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return teleManager.getSimSerialNumber();
	}

	/**
	 * 获取SIM运营商信息
	 * @param context
	 * @return
	 */
	public String getOperator(Context context) {
		if(teleManager==null)
			teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return teleManager.getSimOperatorName();
	}

	/**
	 * 获取网络类型：2G/3G/4G
	 * @param context
	 * @return
	 */
	public String getNetworkType(Context context) {
		if(teleManager==null)
			teleManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		int networkType = teleManager.getNetworkType();
		switch (networkType) {
			case TelephonyManager.NETWORK_TYPE_GPRS:
			case TelephonyManager.NETWORK_TYPE_EDGE:
			case TelephonyManager.NETWORK_TYPE_CDMA:
			case TelephonyManager.NETWORK_TYPE_1xRTT:
			case TelephonyManager.NETWORK_TYPE_IDEN:
				return "2G";
			case TelephonyManager.NETWORK_TYPE_UMTS:
			case TelephonyManager.NETWORK_TYPE_EVDO_0:
			case TelephonyManager.NETWORK_TYPE_EVDO_A:
			case TelephonyManager.NETWORK_TYPE_HSDPA:
			case TelephonyManager.NETWORK_TYPE_HSUPA:
			case TelephonyManager.NETWORK_TYPE_HSPA:
			case TelephonyManager.NETWORK_TYPE_EVDO_B:
			case TelephonyManager.NETWORK_TYPE_EHRPD:
			case TelephonyManager.NETWORK_TYPE_HSPAP:
				return "3G";
			case TelephonyManager.NETWORK_TYPE_LTE:
				return "4G";
			default:
				return "Unknown";
		}
	}

	/**
	 * 获取注册状态
	 * 1为注册，0为未注册
	 * @param context
	 * @return
	 */
	public String getRegisterStatus(Context context) {
		ComponentName component = new ComponentName(context,
				DeviceAdmin.class);
		DevicePolicyManager manager = (DevicePolicyManager)
				context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if(!manager.isAdminActive(component))
			return "0";
		return "1";
	}

	/**
	 * 获取锁定状态
	 * 1为锁定，0为未锁定
	 * @param context
	 * @return
	 */
	public String getLockStatus(Context context) {
		SharedPreferences sp = context.getSharedPreferences("common", Context.MODE_PRIVATE);
		return sp.getString("isLock", "0");
	}
	public void setLockStatus(Context context, String isLock) {
		SharedPreferences sp = context.getSharedPreferences("common", Context.MODE_PRIVATE);
		Editor editor = sp.edit();
		editor.putString("isLock", isLock);
		editor.commit();
	}

	/**
	 * 获取锁定状态
	 * 1为ROOT，0为未ROOT
	 * @param context
	 * @return
	 */
	public String getRootStatus(Context context) {
		String result = "0";

		String command = "/system/xbin/which";
		Process cess = null;
		try{
			cess = Runtime.getRuntime().exec(new String[]{command, "su"});
			BufferedReader reader = new BufferedReader(new InputStreamReader(cess.getInputStream()));
			if (reader.readLine()!=null)
				return "1";
		}catch(Throwable t){
		}finally{
			if(cess!=null)
				cess.destroy();
		}

		String[] paths = { "/system/app/Superuser.apk", "/sbin/su", "/system/bin/su", "/data/local/bin/su", "/system/sd/xbin/su",
				"/system/xbin/su", "/data/local/su", "/data/local/xbin/su", "/system/bin/failsafe/su"};
		for(String path:paths) {
			if(new File(path).exists())
				return "1";
		}

		return result;
	}

	/**
	 * 判断网络类型
	 * "0"-未联网，"1"-wifi联网，"2"-移动数据联网
	 * @param context
	 * @return
	 */
	public String getNetworkStatus(Context context) {
		if(isWifiConnected(context))
			return "1";
		if(isDataConnected(context))
			return "2";
		return "0";
	}
	/**
	 * 判断wifi是否连接
	 * @param context
	 * @return
	 */
	public boolean isWifiConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo wifiNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		if(wifiNetwork.isConnected())
			return true;
		return false;
	}
	/**
	 * 判断移动网络是否连接
	 * @param context
	 * @return
	 */
	public boolean isDataConnected(Context context) {
		ConnectivityManager manager = (ConnectivityManager)
				context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mobileNetwork = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
		if(mobileNetwork.isConnected())
			return true;
		return false;
	}

	/**
	 * 获取当前WIFI连接的SSID信息
	 * @param context
	 * @return
	 */
	public String getCurrentWifi(Context context) {
		if(!isWifiConnected(context))
			return "当前设备未连接wifi!";
		WifiManager wifiManager = (WifiManager)context.getSystemService(Context.WIFI_SERVICE);
		WifiInfo info = wifiManager.getConnectionInfo();
		return info.getSSID();
	}

	/**
	 * 获取基站编号
	 * @param context
	 * @return
	 */
	public String getCurrentBaseStation(Context context) {
		int cellid = -1;
		try{
			TelephonyManager manager = (TelephonyManager)
					context.getSystemService(Context.TELEPHONY_SERVICE);
			GsmCellLocation location = (GsmCellLocation) manager.getCellLocation();
			cellid = location.getCid();
		}catch(Exception e){
			Log.e("getCurrentBaseStation error", e.getMessage(), e);
		}
		return String.valueOf(cellid);
	}

	/**
	 * 获取当前应用版本
	 * @param context
	 * @return
	 * @throws NameNotFoundException
	 */
	public String getCurrentVersion(Context context) throws NameNotFoundException {
		PackageManager manager = context.getPackageManager();
		PackageInfo info = manager.getPackageInfo(context.getPackageName(), 0);
		return info.versionName;
	}

	/**
	 * 获取最新的定位结果
	 * 返回值JSONObject
	 * 	date-采集时间
	 * 	longitude-经度
	 * 	latitude-纬度
	 * @return
	 */
	public JSONObject getLatestLocation() {
		return DataBaseManage.getLatestLocation();
	}

	/**
	 * 获取几天内的定位信息
	 * 如果为1，则代表返回当前内的定位信息
	 * 结果为JSON数组，按采集时间逆序排序
	 * @param days
	 * @return
	 */
	public JSONArray getHistoryLocation(int days) {
		return DataBaseManage.getHistoryLoaction(days);
	}

	/**
	 * 锁定，如果成功返回true
	 * @param context
	 * @return
	 */
	public boolean lock(Context context) {
		if(context==null)
			return false;
		ComponentName component = new ComponentName(context,
				DeviceAdmin.class);
		DevicePolicyManager manager = (DevicePolicyManager)
				context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		if(manager.isAdminActive(component)) {
			manager.lockNow();
			return true;
		}
		return false;
	}

	/**
	 * 唤醒
	 * @param context
	 * @return
	 */
	public boolean wakeup(Context context) {
		//launch wakeup activity
		if(context==null)
			return false;
		try{
			Intent intent = new Intent("com.mobile.deivce.manage.wakeup");
			context.startActivity(intent);
		}catch(Throwable e){
			Log.e("wakeup error", e.getMessage(), e);
		}

		//try to launch screen
		if(MDMBaseService.fullWakeLock==null)
			return false;

		//before 这里应该放在onResume
		if(MDMBaseService.fullWakeLock.isHeld()){
			MDMBaseService.fullWakeLock.release();
		}
		if(MDMBaseService.partialWakeLock.isHeld()){
			MDMBaseService.partialWakeLock.release();
		}
		//before 这里应该放在onPause
		MDMBaseService.partialWakeLock.acquire();

		//execute
		MDMBaseService.fullWakeLock.acquire();//CPU on, Screen bright, Keyboard bright

		KeyguardManager keyguardManager = (KeyguardManager)
				context.getSystemService(Context.KEYGUARD_SERVICE);
		KeyguardLock keyguardLock = keyguardManager.newKeyguardLock("TAG");
		keyguardLock.disableKeyguard();
		return true;
	}

	/**
	 * 重置密码
	 * 如果newPasscode参数为null或空，则清空密码，否则将其设置为最新密码
	 * 如果新密码不符合设备固有密码策略，则设置失败，如最小密码位数等。
	 * @param context
	 * @param newPasscode
	 * @return
	 */
	public boolean resetPasscode(Context context, String newPasscode) {
		if(context==null)
			return false;

		if(newPasscode==null)
			newPasscode = "";
		newPasscode = newPasscode.trim();

		ComponentName component = new ComponentName(context,
				DeviceAdmin.class);
		DevicePolicyManager manager = (DevicePolicyManager)
				context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		//判断是否具有管理员权限
		if(manager.isAdminActive(component)) {
			manager.setPasswordQuality(component, DevicePolicyManager.PASSWORD_QUALITY_UNSPECIFIED);
			manager.setPasswordMinimumLength(component, 0);
			return manager.resetPassword(newPasscode, 0);
		}

		return false;
	}

	/**
	 * 数据擦除
	 * 设备恢复出厂，清除用户数据
	 * @param context
	 * @return
	 */
	public boolean wipeData(Context context) {
		ComponentName component = new ComponentName(context,
				DeviceAdmin.class);
		DevicePolicyManager manager = (DevicePolicyManager)
				context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		//判断是否具有管理员权限
		if(manager.isAdminActive(component)) {
			manager.wipeData(0);
			return true;
		}
		return false;
	}

	/**
	 * 设置密码强度
	 * params可以包含参数为：
	 * PasswordMinimumLength - 最短密码长度
	 * PasswordMinimumLetters - 最短字母个数
	 * PasswordMinimumLowerCase - 最短小写字母个数
	 * PasswordMinimumUpperCase - 最短大写字母个数
	 * PasswordMinimumNonLetter - 最短非字母个数
	 * PasswordMinimumNumeric - 最短数字个数
	 * PasswordMinimumSymbols - 最短特殊符号个数
	 * PasswordExpirationTimeout - 密码过期时间timeout
	 *
	 * @param params
	 * @return
	 * @throws JSONException
	 * @throws NumberFormatException
	 */
	public boolean setPasswordQuality(Context context, JSONObject params)
			throws NumberFormatException, JSONException {
		ComponentName component = new ComponentName(context,
				DeviceAdmin.class);
		DevicePolicyManager manager = (DevicePolicyManager)
				context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		//判断是否具有管理员权限
		if(manager.isAdminActive(component)) {
			//PasswordMinimumLength - 最短密码长度
			if(params.has("PasswordMinimumLength")) {
				manager.setPasswordMinimumLength(component,
						Integer.valueOf(params.getString("PasswordMinimumLength")));
			}
			//PasswordMinimumLetters - 最短字母个数
			if(params.has("PasswordMinimumLetters")) {
				manager.setPasswordMinimumLetters(component,
						Integer.valueOf(params.getString("PasswordMinimumLetters")));
			}
			//PasswordMinimumLowerCase - 最短小写字母个数
			if(params.has("PasswordMinimumLowerCase")) {
				manager.setPasswordMinimumLowerCase(component,
						Integer.valueOf(params.getString("PasswordMinimumLowerCase")));
			}
			//PasswordMinimumUpperCase - 最短大写字母个数
			if(params.has("PasswordMinimumUpperCase")) {
				manager.setPasswordMinimumUpperCase(component,
						Integer.valueOf(params.getString("PasswordMinimumUpperCase")));
			}
			//PasswordMinimumNonLetter - 最短非字母个数
			if(params.has("PasswordMinimumNonLetter")) {
				manager.setPasswordMinimumNonLetter(component,
						Integer.valueOf(params.getString("PasswordMinimumNonLetter")));
			}
			//PasswordMinimumNumeric - 最短数字个数
			if(params.has("PasswordMinimumNumeric")) {
				manager.setPasswordMinimumNumeric(component,
						Integer.valueOf(params.getString("PasswordMinimumNumeric")));
			}
			//PasswordMinimumSymbols - 最短特殊符号个数
			if(params.has("PasswordMinimumSymbols")) {
				manager.setPasswordMinimumSymbols(component,
						Integer.valueOf(params.getString("PasswordMinimumSymbols")));
			}
			//PasswordExpirationTimeout - 密码过期时间timeout
			if(params.has("PasswordExpirationTimeout")) {
				manager.setPasswordExpirationTimeout(component,
						Long.valueOf(params.getString("PasswordExpirationTimeout")));
			}
			//PasswordHistoryLength - 历史密码允许长度
			if(params.has("PasswordHistoryLength")) {
				manager.setPasswordHistoryLength(component,
						Integer.valueOf(params.getString("PasswordHistoryLength")));
			}
			return true;
		}

		return false;
	}

	/**
	 * 查询密码强度设置
	 * PasswordMinimumLength - 最短密码长度
	 * PasswordMinimumLetters - 最短字母个数
	 * PasswordMinimumLowerCase - 最短小写字母个数
	 * PasswordMinimumUpperCase - 最短大写字母个数
	 * PasswordMinimumNonLetter - 最短非字母个数
	 * PasswordMinimumNumeric - 最短数字个数
	 * PasswordMinimumSymbols - 最短特殊符号个数
	 * PasswordExpirationTimeout - 密码失效超时时间
	 * PasswordHistoryLength - 历史密码允许长度
	 *
	 * @param context
	 * @return
	 * @throws JSONException
	 */
	public JSONObject getPasswordQuality(Context context) throws JSONException {
		JSONObject quality = new JSONObject();
		ComponentName component = new ComponentName(context,
				DeviceAdmin.class);
		DevicePolicyManager manager = (DevicePolicyManager)
				context.getSystemService(Context.DEVICE_POLICY_SERVICE);
		//判断是否具有管理员权限
		if(manager.isAdminActive(component)) {
			quality.put("PasswordMinimumLength", manager.getPasswordMinimumLength(component));
			quality.put("PasswordMinimumLetters", manager.getPasswordMinimumLetters(component));
			quality.put("PasswordMinimumLowerCase", manager.getPasswordMinimumLowerCase(component));
			quality.put("PasswordMinimumUpperCase", manager.getPasswordMinimumUpperCase(component));
			quality.put("PasswordMinimumNonLetter", manager.getPasswordMinimumNonLetter(component));
			quality.put("PasswordMinimumNumeric", manager.getPasswordMinimumNumeric(component));
			quality.put("PasswordMinimumSymbols", manager.getPasswordMinimumSymbols(component));
			quality.put("PasswordExpirationTimeout", manager.getPasswordExpirationTimeout(component));
			quality.put("PasswordHistoryLength", manager.getPasswordHistoryLength(component));
		}
		return quality;
	}

	/**
	 * 获取设备安装应用列表及基本信息
	 *
	 * packagename-应用包名
	 * name-应用名称
	 * versionname-版本名称
	 * versioncode-版本号
	 * size-大小（单位：M）
	 * firstInstallTime-应用安装时间
	 * lastUpdateTime-应用更新时间
	 *
	 * @param context
	 * @return
	 */
	public JSONArray getInstalledApp(Context context) {
		JSONArray result = new JSONArray();
		PackageManager manager = context.getPackageManager();
		List<PackageInfo> infoList = manager.getInstalledPackages(0);
		for(PackageInfo info : infoList) {
			JSONObject item = new JSONObject();
			try {
				item.put("packagename", info.packageName);
				item.put("name", info.applicationInfo.loadLabel(manager).toString());
				item.put("versionname", info.versionName);
				item.put("versioncode", info.versionCode);
				File file = new File(info.applicationInfo.publicSourceDir);
				if(file.exists()) {
					float size = file.length()/((float)1024)/((float)1024);
					item.put("size", size);
				}
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				item.put("firstInstallTime", sdf.format(new Date(info.firstInstallTime)));
				item.put("lastUpdateTime", sdf.format(new Date(info.lastUpdateTime)));
			} catch (JSONException e) {
				Log.e("getInstalledApp error", e.getMessage(), e);
			}
			result.put(item);
		}
		return result;
	}


	/**
	 * 其他
	 */
	public static BroadcastReceiver chargeReceiver;

	/**
	 * 初始化
	 * @param context
	 */
	public static void commandInit(Context context) {
		if(chargeReceiver!=null) {
			try{
				context.unregisterReceiver(chargeReceiver);
			}catch(Exception e) {
			}
		}
		chargeReceiver = new ChargeReceiver();
		context.registerReceiver(chargeReceiver,
				new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
	}

}
