package com.mobile.device.manage.core;

import com.mobile.device.manage.Common;
import com.mobile.device.manage.LongKeepService;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class JobScheduleService extends JobService {

	@Override
    public boolean onStartJob(JobParameters params) {
		Log.w("---", "---");
		try{
			Context context = JobScheduleService.this;
			if(!Common.isServiceRunning(LongKeepService.class, context)) {
				Intent intent = new Intent(context, LongKeepService.class);
				startService(intent);
			}
		}catch(Exception e){
		}
        return false;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
