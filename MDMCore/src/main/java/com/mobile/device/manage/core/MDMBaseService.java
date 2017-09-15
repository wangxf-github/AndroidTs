package com.mobile.device.manage.core;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Properties;
import java.util.UUID;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.location.Poi;
import com.baidu.location.LocationClientOption.LocationMode;
import com.mobile.device.manage.Common;
import com.mobile.device.manage.DataBaseManage;
import com.mobile.device.manage.LongKeepService;
import com.mobile.device.manage.command.CommandExecute;
import com.mobile.device.manage.download.core.DownloadManagerPro;
import com.mobile.device.manage.download.report.ReportStructure;
import com.mobile.device.manage.download.report.listener.DownloadManagerListener;
import com.mobile.device.manage.util.DeviceHelper;
import com.tencent.android.tpush.XGIOperateCallback;
import com.tencent.android.tpush.XGPushManager;

public class MDMBaseService {

    public Context staticContext;
    public RequestQueue mRequestQueue;
    private DownloadManagerPro downloadManage;

    //wake up
    public static WakeLock fullWakeLock, partialWakeLock;

    /**
     * 初始化MDM服务，需要在程序启动时主进程中调用
     *
     * @param context
     * @throws Exception
     */
    public void init(Context context) throws Exception {
        if (context == null)
            throw new Exception("Error: param context could not be null.");
        staticContext = context;

        //MDM 基础服务初始化

        CommandExecute.init(staticContext);
        createWakeLocks(staticContext);

        //数据库初始化
        DataBaseManage.init(staticContext);

        //long service
        if (!Common.isServiceRunning(LongKeepService.class, staticContext)) {
            Intent intent = new Intent(staticContext, LongKeepService.class);
            staticContext.startService(intent);
        }
    }

    protected void createWakeLocks(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        fullWakeLock = powerManager.newWakeLock(
                (PowerManager.SCREEN_BRIGHT_WAKE_LOCK | PowerManager.FULL_WAKE_LOCK
                        | PowerManager.ACQUIRE_CAUSES_WAKEUP), "Loneworker - FULL WAKE LOCK");
        partialWakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "Loneworker - PARTIAL WAKE LOCK");
        partialWakeLock.acquire();//CPU on, Screen off, Keyboard off
    }

    /**
     * 判断是否开启设备管理器权限
     *
     * @return
     */
    public boolean isAdminActive() {
        if (staticContext == null)
            return false;
        ComponentName component = new ComponentName(staticContext,
                DeviceAdmin.class);
        DevicePolicyManager manager = (DevicePolicyManager)
                staticContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!manager.isAdminActive(component))
            return false;
        return true;
    }

    /**
     * 启动设备管理器配置
     */
    public void activeAdmin(Activity activity) {
        if (staticContext == null || activity == null)
            return;
        ComponentName component = new ComponentName(staticContext,
                DeviceAdmin.class);
        DevicePolicyManager manager = (DevicePolicyManager)
                staticContext.getSystemService(Context.DEVICE_POLICY_SERVICE);
        if (!manager.isAdminActive(component)) {
            Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
            intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, component);
            activity.startActivity(intent);
        }
    }

    /**
     * 获取当前设备在push服务中获得的token
     *
     * @param context
     * @return
     */
    public String getDeviceToken(Context context) {
        SharedPreferences sp = context.getSharedPreferences("mdm_common",
                Context.MODE_PRIVATE);
        return sp.getString("token", "-1");
    }

    /**
     * 检查密码合规性
     *
     * @param context
     */
    public boolean checkPassword(Activity context) {
        boolean result = true;
        ComponentName component = new ComponentName(context,
                DeviceAdmin.class);
        DevicePolicyManager manager = (DevicePolicyManager)
                context.getSystemService(Context.DEVICE_POLICY_SERVICE);
        //判断是否具有管理员权限
        if (manager.isAdminActive(component)) {
            if (!manager.isActivePasswordSufficient()) {
                result = false;
                Intent passwordSetIntent = new Intent(DevicePolicyManager.ACTION_SET_NEW_PASSWORD);
                passwordSetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(passwordSetIntent);
            }
        }
        return result;
    }

    /**
     * Download Demo:
     * DownloadManagerPro downloader = MDMService.getInstance().getDownloadManager(context);
     * int token = downloader.addTask(downloadedName, downloadUrl, true, false);
     * downloader.startDownload(token);
     *
     * @param context
     * @return
     */
    public synchronized DownloadManagerPro getDownloadManager(Context context) {
        if (downloadManage == null) {
            //初始化下载管理器
            downloadManage = new DownloadManagerPro(context);
            downloadManage.init("MDMDownload/", 3, new DownloadManagerListener() {

                @Override
                public void OnDownloadStarted(long taskId) {
                    Log.d("getDownloadManager", "OnDownloadStarted");
                }

                @Override
                public void OnDownloadPaused(long taskId) {
                    Log.d("getDownloadManager", "OnDownloadPaused");
                }

                @Override
                public void onDownloadProcess(long taskId, double percent,
                                              long downloadedLength) {
                    Log.d("getDownloadManager", "onDownloadProcess");
                }

                @Override
                public void OnDownloadFinished(long taskId) {
                    Log.d("getDownloadManager", "OnDownloadFinished");
                }

                @Override
                public void OnDownloadRebuildStart(long taskId) {
                    Log.d("getDownloadManager", "OnDownloadRebuildStart");
                }

                @Override
                public void OnDownloadRebuildFinished(long taskId) {
                    Log.d("getDownloadManager", "OnDownloadRebuildFinished");
                }

                @Override
                public void OnDownloadCompleted(long taskId) {
                    Log.d("getDownloadManager", "OnDownloadCompleted");
                    if (popActivity == null)
                        return;

                    DownloadManagerPro downloader = getDownloadManager(staticContext);
                    ReportStructure structure = downloader.singleDownloadStatus((int) taskId);
                    String downloadedPath = structure.saveAddress;
                    //安装
                    if (new File(downloadedPath).exists()) {
                        Intent in = new Intent(Intent.ACTION_VIEW);
                        in.setDataAndType(Uri.fromFile(new File(downloadedPath)), "application/vnd.android.package-archive");
                        popActivity.startActivity(in);
                    }
                }

                @Override
                public void connectionLost(long taskId) {
                    Log.d("getDownloadManager", "connectionLost");
                }

            });

        }

        return downloadManage;
    }

    /**
     * 下载并安装应用
     *
     * @param context
     * @param appUrl, apk路径
     * @throws IOException
     */
    public void installApp(Activity context, String appUrl) throws IOException {
        popActivity = context;
        DownloadManagerPro downloader = getDownloadManager(context);
        String fileName = UUID.randomUUID().toString();
        int token = downloader.addTask(fileName, appUrl, true, false);
        downloader.startDownload(token);
    }

    /**
     * 下载并升级应用
     *
     * @param context
     * @param appUrl
     * @throws IOException
     */
    public void updateApp(Activity context, String appUrl) throws IOException {
        popActivity = context;
        DownloadManagerPro downloader = getDownloadManager(context);
        String fileName = UUID.randomUUID().toString();
        int token = downloader.addTask(fileName, appUrl, true, false);
        downloader.startDownload(token);
    }

    /**
     * 卸载应用
     *
     * @param context
     * @param packageName，包名
     * @throws Exception
     */
    public void uninstallApp(Activity context, String packageName) throws Exception {
        //判断应用是否存在
        PackageManager manager = context.getPackageManager();
        PackageInfo info = null;
        try {
            info = manager.getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
        }
        if (info == null)
            throw new Exception("No App Installed.");//当前未安装此包名的应用

        //卸载应用
        Uri appUri = Uri.parse("package:" + packageName);
        Intent in = new Intent(Intent.ACTION_DELETE, appUri);
        context.startActivity(in);
    }

    public void launchApp(Activity context, String packageName) {
        Intent in = new Intent();
        in.setData(Uri.parse("mdm://" + packageName));
        context.startActivity(in);
    }

    private Activity popActivity = null;

    /**
     * 长连接服务初始化
     *
     * @param context
     */
    public void pushInit(Context context,String account) {
        //push
        // 开启logcat输出，方便debug，发布时请关闭
//	    XGPushConfig.enableDebug(this, true);
        // 如果需要知道注册是否成功，请使用registerPush(getApplicationContext(), XGIOperateCallback)带callback版本
        // 如果需要绑定账号，请使用registerPush(getApplicationContext(),account)版本
        // 具体可参考详细的开发指南
        // 传递的参数为ApplicationContext
        XGPushManager.registerPush(staticContext,account, new XGIOperateCallback() {
            @Override
            public void onFail(Object arg0, int arg1, String arg2) {
                Log.e("xg register failure", arg2);
                SharedPreferences sp = staticContext.getSharedPreferences("mdm_common",
                        Context.MODE_PRIVATE);
                Editor editor = sp.edit();
                editor.putString("registErr", arg2.toString());
                editor.commit();
            }

            @Override
            public void onSuccess(Object arg0, int arg1) {
                if (staticContext != null) {
                    SharedPreferences sp = staticContext.getSharedPreferences("mdm_common",
                            Context.MODE_PRIVATE);
                    Editor editor = sp.edit();
                    editor.putString("token", arg0.toString());
                    editor.commit();
                }
                Log.d("xg register success", "token:" + String.valueOf(arg0));


            }
        });
        // 其它常用的API：
        // 绑定账号（别名）注册：registerPush(context,account)或registerPush(context,account, XGIOperateCallback)，其中account为APP账号，可以为任意字符串（qq、openid或任意第三方），业务方一定要注意终端与后台保持一致。
        // 取消绑定账号（别名）：registerPush(context,"*")，即account="*"为取消绑定，解绑后，该针对该账号的推送将失效
        // 反注册（不再接收消息）：unregisterPush(context)
        // 设置标签：setTag(context, tagName)
        // 删除标签：deleteTag(context, tagName)
    }


    /**
     * 定位服务初始化
     *
     * @param context
     */
    public final static int locatePeriod = 5 * 60;//定位周期，单位秒
    private static long lastSaveTime = -1;//上次获取存储的定位时间

    public void locationInit(Context context) {
        //LBS
        mLocationClient = new LocationClient(staticContext);     //声明LocationClient类
        mLocationClient.registerLocationListener(myListener);    //注册监听函数
        initLocation();
        mLocationClient.start();
    }

    public static LocationClient mLocationClient = null;
    public static BDLocationListener myListener = new MyLocationListener();

    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationMode.Hight_Accuracy
        );//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setCoorType("bd09ll");//可选，默认gcj02，设置返回的定位结果坐标系
        int span = locatePeriod * 1000;//定位周期
        option.setScanSpan(span);//可选，默认0，即仅定位一次，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true);//可选，默认false,设置是否使用gps
        option.setLocationNotify(true);//可选，默认false，设置是否当gps有效时按照1S1次频率输出GPS结果
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation.getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
//		option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
//		option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
//		option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤gps仿真结果，默认需要
        mLocationClient.setLocOption(option);
    }

    public static class MyLocationListener implements BDLocationListener {

        @Override
        public void onReceiveLocation(BDLocation location) {
            //Receive Location
            StringBuffer sb = new StringBuffer(256);
            sb.append("time : ");
            sb.append(location.getTime());
            sb.append("\nerror code : ");
            sb.append(location.getLocType());
            sb.append("\nlatitude : ");
            sb.append(location.getLatitude());
            sb.append("\nlontitude : ");
            sb.append(location.getLongitude());
            sb.append("\nradius : ");
            sb.append(location.getRadius());
            if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                sb.append("\nspeed : ");
                sb.append(location.getSpeed());// 单位：公里每小时
                sb.append("\nsatellite : ");
                sb.append(location.getSatelliteNumber());
                sb.append("\nheight : ");
                sb.append(location.getAltitude());// 单位：米
                sb.append("\ndirection : ");
                sb.append(location.getDirection());// 单位度
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                sb.append("\ndescribe : ");
                sb.append("gps定位成功");
                saveLocation(location.getLongitude(), location.getLatitude());
            } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                sb.append("\naddr : ");
                sb.append(location.getAddrStr());
                //运营商信息
                sb.append("\noperationers : ");
                sb.append(location.getOperators());
                sb.append("\ndescribe : ");
                sb.append("网络定位成功");
                saveLocation(location.getLongitude(), location.getLatitude());
            } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                sb.append("\ndescribe : ");
                sb.append("离线定位成功，离线定位结果也是有效的");
                saveLocation(location.getLongitude(), location.getLatitude());
            } else if (location.getLocType() == BDLocation.TypeServerError) {
                sb.append("\ndescribe : ");
                sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
            } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                sb.append("\ndescribe : ");
                sb.append("网络不同导致定位失败，请检查网络是否通畅");
            } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                sb.append("\ndescribe : ");
                sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
            }
            sb.append("\nlocationdescribe : ");
            sb.append(location.getLocationDescribe());// 位置语义化信息
            List<Poi> list = location.getPoiList();// POI数据
            if (list != null) {
                sb.append("\npoilist size = : ");
                sb.append(list.size());
                for (Poi p : list) {
                    sb.append("\npoi= : ");
                    sb.append(p.getId() + " " + p.getName() + " " + p.getRank());
                }
            }
            Log.i("BaiduLocationApiDem", sb.toString());
        }

        private synchronized void saveLocation(double longitude, double latitude) {
//			long currentTime = new Date().getTime();
//			if(lastSaveTime==-1) {
//				lastSaveTime = new Date().getTime();
//				DataBaseManage.insertLocation(longitude, latitude);
//			}else if((currentTime-lastSaveTime)>=(locatePeriod*1000-60*1000)) {
//				DataBaseManage.insertLocation(longitude, latitude);
//				lastSaveTime = currentTime;
//			}
            DataBaseManage.insertLocation(longitude, latitude);
        }
    }

    public void initServerConfig(Context context) {
        try {
            InputStream is = context.getAssets().open("config.properties");
            Properties p = new Properties();
            p.load(is);
            String rIp = p.getProperty("Server");
            String reportUrl = p.getProperty("ReportUrl");
            String requestUrl = p.getProperty("RequestUrl");
            reportUrl = rIp + reportUrl;
            requestUrl = rIp + requestUrl;
            SharedPreferences sharedPreferences = context.getSharedPreferences("mdm_common", Context.MODE_PRIVATE);
            Editor editor = sharedPreferences.edit();
            editor.putString("ReportUrl", reportUrl);
            editor.putString("RequestUrl", requestUrl);
            editor.commit();
            is.close();
        } catch (Exception e) {
            Log.e("resolve config error", e.getMessage(), e);
        }
    }

    /**
     * 初始化设备信息
     * @param context
     */
    public void initDevice(Context context) {
        SharedPreferences prefs = context
                .getSharedPreferences("mdm_common", 0);
        String id = prefs.getString("device_id", null);
        if (id != null && id != "") {
            return;
        } else {
            String device_id = DeviceHelper.getDeviceID(context);
            prefs.edit()
                    .putString("device_id", device_id)
                    .commit();
        }
    }
}
