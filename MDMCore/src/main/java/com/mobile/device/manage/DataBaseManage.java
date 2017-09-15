package com.mobile.device.manage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DataBaseManage {

	public static DataBaseHelper helper;
	public static SQLiteDatabase database;

	public static void init(Context context) {
		helper = new DataBaseHelper(context, "mdm", null, 1);
		database = helper.getReadableDatabase();
	}

	/**
	 * 地理位置存储
	 */
	public static void insertLocation(double longitude, double latitude) {
		long currentDate = new Date().getTime();

		//保证升序
		if(currentDate<=getLatestLocationRecordDate())
			return;

		String insert = "insert into location(date, longitude, latitude) values ('"
				+ String.valueOf(currentDate) + "','"
				+String.valueOf(longitude)+"','"
				+String.valueOf(latitude)+"')";
		database.execSQL(insert);
	}

	/**
	 * 得到最新的地理位置
	 * @return
	 */
	public static JSONObject getLatestLocation() {
		Cursor cursor = database.rawQuery("SELECT * FROM location ORDER BY id DESC", new String[] {});
		if(cursor==null)
			return null;

		JSONObject result = new JSONObject();
		try{
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			if(cursor.moveToNext()) {
				String date = cursor.getString(cursor.getColumnIndex("date"));
				String dateValue = dateFormat.format(new Date(Long.valueOf(date)));
				String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
				String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
				try {
					result.put("date", dateValue);
					result.put("longitude", longitude);
					result.put("latitude", latitude);
				} catch (JSONException e) {
				}
			}
		}finally{
			if(cursor!=null)
				cursor.close();
		}

		return result;
	}

	private static long getLatestLocationRecordDate() {
		long result = -1;
		Cursor cursor = database.rawQuery("SELECT * FROM location ORDER BY id DESC", new String[] {});
		if(cursor==null)
			return result;

		try{
			if(cursor.moveToFirst()) {
				String date = cursor.getString(cursor.getColumnIndex("date"));
				result = Long.valueOf(date);
			}
		}finally{
			if(cursor!=null)
				cursor.close();
		}
		return result;
	}

	/**
	 * 得到days天内的地理位置历史
	 * 从当前日期为第一天，向前统计共days天
	 * @return
	 */
	public static JSONArray getHistoryLoaction(int days) {
		GregorianCalendar cal = new GregorianCalendar();
		cal.setTime(new Date());
		cal.set(Calendar.HOUR_OF_DAY, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		long currentZeroTime = cal.getTimeInMillis();
		long endTime = currentZeroTime + 24*3600*1000;
		long startTime = endTime - 24*3600*1000*days;
		Cursor cursor = database.rawQuery("SELECT * FROM location WHERE " +
						"date>=? and date<? ORDER BY id ASC",
				new String[] {String.valueOf(startTime), String.valueOf(endTime)});//DESC ASC
		if(cursor==null)
			return null;

		//获取查询结果
		////////////////////////
//		JSONArray result = new JSONArray();
//		List<String> dateArray = new ArrayList<String>();
//		try{
//			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//			while(cursor.moveToNext()) {
//				JSONObject item = new JSONObject();
//				String date = cursor.getString(cursor.getColumnIndex("date"));
//
//				//去掉重复时间内容
//				if(dateArray.contains(date))
//					continue;
//				else
//					dateArray.add(date);
//
//				String dateValue = dateFormat.format(new Date(Long.valueOf(date)));
//				String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
//				String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
//				try {
//					item.put("date", dateValue);
//					item.put("longitude", longitude);
//					item.put("latitude", latitude);
//				} catch (JSONException e) {
//				}
//				result.put(item);
//			}
//		}finally {
//			if(cursor!=null)
//				cursor.close();
//		}
		////////////////////////
		JSONArray result = new JSONArray();
		endTime = System.currentTimeMillis();
		if(startTime>endTime) {
			try {
				result.put(0, "System time is wrongly set on devices.");
			} catch (JSONException e) {
			}
			return result;
		}
		//prepare
		long firstRecordTime = -1;
		List<JSONObject> recordList = new ArrayList<JSONObject>();
		try{
			while(cursor.moveToNext()) {
				JSONObject item = new JSONObject();
				try {
					String date = cursor.getString(cursor.getColumnIndex("date"));
					if(firstRecordTime==-1)
						firstRecordTime = Long.valueOf(date);
					String longitude = cursor.getString(cursor.getColumnIndex("longitude"));
					String latitude = cursor.getString(cursor.getColumnIndex("latitude"));
					item.put("date", date);
					item.put("longitude", longitude);
					item.put("latitude", latitude);
				} catch (JSONException e) {
					continue;
				}
				recordList.add(item);
			}
		}finally {
			if(cursor!=null)
				cursor.close();
		}
		if(recordList.size()==0 || firstRecordTime<=0)
			return result;
		//ready
		final long interval = 5*60*1000;//默认五分钟采集间隔
		long curTimeCount = startTime;//当前第一个结果时间点
		while(curTimeCount<firstRecordTime) {
			//调整第一个结果时间点
			curTimeCount += interval;
			if(curTimeCount>endTime)
				break;
		}
		if(curTimeCount>=endTime || curTimeCount<firstRecordTime)
			return result;
		//resolve from curTimeCount to <endTime
		int readCount = 0;
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		JSONObject beforeNode = recordList.get(readCount);
		JSONObject afterNode = recordList.get(readCount);
		while(curTimeCount<endTime) {
			curTimeCount += interval;
			if(curTimeCount>endTime)
				break;
			//check afterNode
			try {
				long afterTime = afterNode.getLong("date");
				if(curTimeCount>afterTime) {
					for(int i=readCount+1;i<recordList.size();i++) {
						readCount = i;
						afterNode = recordList.get(readCount);
						if(curTimeCount<=afterNode.getLong("date"))
							break;
					}
					if(readCount-1>=0)
						beforeNode = recordList.get(readCount-1);
				}
			} catch (JSONException e) {
			}
			//save
			try {
				JSONObject saveItem = new JSONObject();
				String dateValue = dateFormat.format(new Date(curTimeCount));
				saveItem.put("date", dateValue);
				saveItem.put("longitude", beforeNode.getString("longitude"));
				saveItem.put("latitude", beforeNode.getString("latitude"));
				result.put(saveItem);
			} catch (JSONException e) {
			}
		}
		////////////////////////

		return result;
	}

}
