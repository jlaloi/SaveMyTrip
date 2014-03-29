package com.example.savemytrip;

import java.io.File;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.savemytrip.Utils.Configuration;

public class UpdateService extends Service implements LocationListener {

	private final static IntentFilter sIntentFilter;

	private final static int counter = 16;

	private LocationManager locationManager;
	private String provider;
	private File saveFile;
	private SharedPreferences settings;

	private final static String LOG = "UpdateService";

	static {
		sIntentFilter = new IntentFilter();
		sIntentFilter.addAction(Intent.ACTION_TIME_TICK);
	}

	public void onCreate() {
		super.onCreate();
		try {
			PackageManager pm = getPackageManager();
			if (pm != null) {
				pm.setComponentEnabledSetting(new ComponentName(this, UpdateService.class), PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);
			}
		} catch (Exception ignore) {
		}
		settings = getSharedPreferences(Utils.PREFS_NAME, 0);
		registerReceiver(mTimeChangedReceiver, sIntentFilter);
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		saveFile = new File(Environment.getExternalStorageDirectory() + File.separator + getResources().getString(R.string.filename));
	}

	public void onStart(Intent intent, int startId) {
		if (intent != null) {
			update();
		}
	}

	public void onDestroy() {
		super.onDestroy();
		unregisterReceiver(mTimeChangedReceiver);
	}

	private void update() {
		Log.d(LOG, "update");
		if (isRunning()) {
			LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				updateText(R.id.saved, getResources().getString(R.string.activiateGPS));
			} else {
				int remaining = settings.getInt(Configuration.remaining.toString(), 0) - 1;
				Log.i(LOG, "update reamining " + remaining);
				if (remaining <= 0) {
					Criteria criteria = new Criteria();
					provider = locationManager.getBestProvider(criteria, false);
					locationManager.requestLocationUpdates(provider, 10000, 50, this);
					updateText(R.id.saved, getResources().getString(R.string.waiting));
				} else {
					updateText(R.id.saved, getResources().getString(R.string.next, remaining));
					setRemaining(remaining);
				}
			}
		}else{
			setRemaining(counter);
		}
	}

	private void updateText(int field, String text) {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this.getApplicationContext());
		ComponentName thisWidget = new ComponentName(getApplicationContext(), MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(this.getApplicationContext().getPackageName(), R.layout.widget_layout);
			remoteViews.setTextViewText(field, text);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}

	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.w(LOG, "onReceive");
			update();
		}
	};

	public void onLocationChanged(Location location) {
		Log.w(LOG, "onLocationChanged " + location);
		Utils.addToFile(saveFile, location);
		locationManager.removeUpdates(this);
		setRemaining(counter);
		update();
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public boolean isRunning() {
		return settings.getBoolean(Configuration.running.toString(), false);
	}

	public void setRunning(boolean bool) {
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(Configuration.running.toString(), bool);
		editor.commit();
	}

	public int getRemaininge() {
		return settings.getInt(Configuration.remaining.toString(), counter);
	}

	public void setRemaining(int time) {
		Log.i(LOG, "Set saved time " + time);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(Configuration.remaining.toString(), time);
		editor.commit();
	}

}
