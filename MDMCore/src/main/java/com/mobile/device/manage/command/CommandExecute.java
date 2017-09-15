package com.mobile.device.manage.command;

import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.mobile.device.manage.Common;
import com.mobile.device.manage.command.extend.CommonDeviceMethod;
import com.mobile.device.manage.command.extend.OS6DeviceMethod;
import com.mobile.device.manage.command.extend.SumsangDeviceMethod;
import com.mobile.device.manage.core.DataResolve;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.util.Log;

public class CommandExecute {

    private static BaseMethod baseMethod;

    public static BaseMethod getBaseMethod() {
        return baseMethod;
    }

    public static void init(Context context) {
        BaseMethod.commandInit(context);

        //Android兼容性适配——根据不同厂商、机型、操作系统扩展不同的定制实现
        if ("Samsung".equalsIgnoreCase(android.os.Build.MANUFACTURER)) {
            //三星机型适配
            baseMethod = new SumsangDeviceMethod();
        } else if ("6.0".equals(android.os.Build.VERSION.RELEASE)) {
            //操作系统为6.0适配
            baseMethod = new OS6DeviceMethod();
        } else {
            //通用设备
            baseMethod = new CommonDeviceMethod();
        }
    }


    /**
     * 1.获取设备类型信息
     * factory - 厂商
     * model - 设备型号
     *
     * @return
     */
    public static JSONObject getDeviceType() {
        JSONObject collection = new JSONObject();
        try {
            collection.put("factory", baseMethod.getFactory());
            collection.put("model", baseMethod.getDeviceModel());
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 2.获取设备系统信息
     * system_type - 系统类型，默认为android
     * system_version - 系统版本
     * memory - 内存，单位M
     * storage_total - 总存储，单位M
     * storage_used - 已占用存储，单位M
     * charge - 电量剩余，百分比
     *
     * @return
     */
    public static JSONObject getDeviceSystem(Context context) {
        JSONObject collection = new JSONObject();
        try {
            collection.put("system_type", "android");//系统类型，默认为android
            collection.put("system_version", baseMethod.getSystemVersion());
            collection.put("memory", baseMethod.getMemory());
            collection.put("storage_total", baseMethod.getStorageTotal());
            collection.put("storage_used", baseMethod.getStorageUsed());
            collection.put("charge", baseMethod.getCharge(context));
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 3.获取SIM信息
     * imei/meid - IMEI或MEID
     * sn - SN
     * phone_number - 电话号码，只有部分设备能拿到
     * operator - 运营商
     * type - 网络类型
     * <p>
     * 关于电话号码的获取：
     * 从技术层面而言，手机的SIM卡上并不会存储手机号码信息，只会存储IMSI
     * （International Mobile Subscriber Identification Number）。
     * 手机号码（MSISDN）都是登记在HLR（Home Location Register）中的，
     * 在HLR中会把IMSI和MSISDN关联在一起。
     * <p>
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
    public static JSONObject getSIM(Context context) {
        //卡类型(4G/3G/2G)
        JSONObject collection = new JSONObject();
        try {
            collection.put("imei", baseMethod.getIMEI(context));
            collection.put("meid", baseMethod.getMEID(context));
            collection.put("sn", baseMethod.getSN(context));
            collection.put("phone_number", baseMethod.getPhoneNumber(context));
            collection.put("operator", baseMethod.getOperator(context));
            collection.put("type", baseMethod.getNetworkType(context));
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 4.获取设备状态
     * isRegister："1"-注册，"0"-未注册
     * isLock: "1"-锁定，"0"-未锁定
     *
     * @param context
     * @return
     */
    public static JSONObject getDeviceStatus(Context context) {
        JSONObject collection = new JSONObject();
        try {
            collection.put("isRegister", baseMethod.getRegisterStatus(context));
            collection.put("isLock", baseMethod.getLockStatus(context));
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 5.获取设备安全状况
     * isRoot："1"-ROOT，"0"-未ROOT
     *
     * @param context
     * @return
     */
    public static JSONObject getSecureStatus(Context context) {
        JSONObject collection = new JSONObject();
        try {
            collection.put("isRoot", baseMethod.getRootStatus(context));
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 6.获取联网方式
     * network_type: "0"-未联网，"1"-wifi联网，"2"-移动数据联网
     * wifi_identity: 连接的wifi
     * basestation_identity: 连接的数据网络
     *
     * @return
     */
    public static JSONObject getNetworkType(Context context) {
        JSONObject collection = new JSONObject();
        try {
            collection.put("network_type", baseMethod.getNetworkStatus(context));
            collection.put("wifi_identity", baseMethod.getCurrentWifi(context));
            collection.put("basestation_identity",
                    baseMethod.getCurrentBaseStation(context));
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 7.获取客户端版本
     * app_version: 当前MDM应用程序版本号
     *
     * @return
     */
    public static JSONObject getAppVersion(Context context) {
        JSONObject collection = new JSONObject();
        try {
            collection.put("app_version", baseMethod.getCurrentVersion(context));
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 8.获取当前定位信息
     * result - 定位结果(JSONObject格式)
     * date-位置采集时间
     * longitude-经度
     * latitude-纬度
     *
     * @param context
     * @return
     */
    public static JSONObject getCurrentLocation(Context context) {
        JSONObject collection = new JSONObject();
        try {
            collection.put("result", baseMethod.getLatestLocation());
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 9.获取定位历史信息
     * 返回当天定位信息
     *
     * @param context
     * @return
     */
    public static JSONObject getHistoryLocation(Context context, int days) {
        JSONObject collection = new JSONObject();
        try {
            collection.put("result", baseMethod.getHistoryLocation(days));
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 获取设备脱离设备管理器历史（用户卸载行为查询上报）
     * result - 历史查询（JSONArray格式，如果返回值为"0"-用户未曾脱离设备管理器）
     * JSONArray中每个JSONObject对象记录一次违规操作：
     * type - 固定值"AdminDisabled"
     * date - 时间
     *
     * @param context
     * @return
     */
    public static JSONObject getBreakAdminHistory(Context context) {
        JSONObject collection = new JSONObject();
        try {
            SharedPreferences sp = context.getSharedPreferences("mdm_common",
                    Context.MODE_PRIVATE);
            String stored = sp.getString("report", "0");
            collection.put("result", stored);
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return collection;
    }

    /**
     * 10.锁定，如果执行成功返回结果"success"，否则返回"failure".
     *
     * @param context
     * @return
     */
    public static JSONObject lock(Context context) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", baseMethod.lock(context) ? "success" : "failure");
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 11.唤醒，如果唤醒成功返回"success"，否则返回"failure".
     *
     * @param context
     * @return
     */
    public static JSONObject wakeup(Context context) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", baseMethod.wakeup(context) ? "success" : "failure");
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 12.清除设备密码
     *
     * @param context
     * @return 返回设置成功或者失败
     */
    public static JSONObject clearDevicePasscode(Context context) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", baseMethod.resetPasscode(context, null) ? "success" : "failure");
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 13.设置设备密码
     * 如果newPasscode参数为null或空，则清空密码，否则将其设置为最新密码。
     * 如果新密码不符合设备固有密码策略，则设置失败，如最小密码位数等。
     *
     * @param context
     * @return 返回设置成功或者失败
     */
    public static JSONObject setDevicePasscode(Context context, String passcode) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", baseMethod.resetPasscode(context, passcode) ? "success" : "failure");
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 14.数据擦除
     * 设备恢复出厂，清除用户数据
     *
     * @param context
     * @return
     */
    public static JSONObject wipeData(Context context) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", baseMethod.wipeData(context) ? "success" : "failure");
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 15.查询设备安全状态
     * 返回值："1"-不安全(Rooted)，"0"-安全(非Rooted)
     *
     * @param context
     * @return
     */
    public static JSONObject getSecurityStatus(Context context) {
        JSONObject result = new JSONObject();
        try {
            result.put("Rooted", baseMethod.getRootStatus(context));
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 16.设置密码配置
     * 设置设备的密码配置，设置成功之后，客户端检查当前用户密码设置，如果设置不符合配置强度，强制弹出密码设置框，提示用户重新设置密码。
     * 只有当前用户密码设置符合服务端密码配置强度，才认为是安全合规的。
     * 配置各项说明如下：
     * <p>
     * JSONObject参数可包含：
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
     * @param params
     * @return 成功返回"1", 失败返回"0"
     */
    public static JSONObject setPasswordQuality(Context context, JSONObject params) {
        JSONObject result = new JSONObject();
        try {
            result.put("result", baseMethod.setPasswordQuality(context, params) ? "1" : "0");
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 17.查询密码配置
     * 查询当前设备已经成功设置的密码配置强度。
     * <p>
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
     */
    public static JSONObject getPasswordQuality(Context context) {
        try {
            return baseMethod.getPasswordQuality(context);
        } catch (Exception e) {
            Log.e("getPasswordQuality", e.getMessage(), e);
        }
        return null;
    }

    /**
     * 获取设备安装的应用
     * <p>
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
    public static JSONObject getInstalledApps(Context context) {
        JSONObject result = new JSONObject();
        try {
            result.put("apps", baseMethod.getInstalledApp(context));
        } catch (Exception e) {
            Log.e("JSONException", e.getMessage(), e);
        }
        return result;
    }

    /**
     * 保存发送给企业应用的配置信息
     *
     * @param context
     */
    public static void saveAppConfiguration(Context context, JSONObject content) {
        try {
            String packageName = content.getString("packagename");
            String config = content.getString("value");
            if (packageName == null || packageName.isEmpty() || config == null || config.isEmpty())
                return;
            StringBuffer sb = new StringBuffer();
            sb.append(config);
            String path = Environment.getExternalStorageDirectory() + "/mdmlogs/" + packageName;
            FileOutputStream fos = new FileOutputStream(path, false);
            fos.write(sb.toString().getBytes());
            fos.close();
        } catch (Exception e) {
            Log.e("Exception", e.getMessage(), e);
        }
    }

}
