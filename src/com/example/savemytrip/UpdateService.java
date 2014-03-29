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

	private LocationManager locationManager;
	private String provider;
	private Location currentPosition;
	private boolean listening = false;
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
		Log.d(LOG, "onStart");
		if (intent != null) {
			update();
			updateDisplayedLocation();
		}
	}

	public void onDestroy() {
		Log.d(LOG, "onDestroy");
		super.onDestroy();
		unregisterReceiver(mTimeChangedReceiver);
	}

	public void manageLocationManager() {
		if (isRunning() && !listening) {
			Criteria criteria = new Criteria();
			provider = locationManager.getBestProvider(criteria, false);
			locationManager.requestLocationUpdates(provider, 10000, 50, this);
			listening = true;
		} else if (!isRunning() && listening) {
			locationManager.removeUpdates(this);
			listening = false;
			currentPosition = null;
			setSavedTime(-1);
		}

	}

	private void update() {
		Log.d(LOG, "update");
		manageLocationManager();
		if (isRunning()) {
			if (currentPosition != null) {
				if (getSavedTime() != currentPosition.getTime()) {
					Utils.addToFile(saveFile, currentPosition);
					updateText(R.id.saved, Utils.formatDate(currentPosition));
					setSavedTime(currentPosition.getTime());
				}
			} else {
				LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
				if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
					updateText(R.id.saved, getResources().getString(R.string.activiateGPS));
				} else {
					updateText(R.id.saved, getResources().getString(R.string.waiting));
				}
			}
		}
	}

	private void updateDisplayedLocation() {
		Log.d(LOG, "updateDisplayedLocation");
		if (isRunning()) {
			if (currentPosition != null) {
				updateText(R.id.current, Utils.formatLocation(currentPosition));
				if (getSavedTime() == -1) {
					update();
				}
			} else {
				updateText(R.id.current, getResources().getString(R.string.notavailable));
			}
			if (getSavedTime() == -1 && currentPosition != null) {
				update();
			}
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

	private void updateDisplayedLocation(Location location) {
		Log.d(LOG, "updateDisplayedLocation " + location);
		currentPosition = location;
		updateDisplayedLocation();
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

	private final BroadcastReceiver mTimeChangedReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			Log.d(LOG, "onReceive");
			update();
		}
	};

	public void onLocationChanged(Location location) {
		Log.d(LOG, "onLocationChanged " + location);
		updateDisplayedLocation(location);
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

	public long getSavedTime() {
		return settings.getLong(Configuration.lastSaveTime.toString(), -1);
	}

	public void setSavedTime(long time) {
		Log.i(LOG, "Set saved time " + time);
		SharedPreferences.Editor editor = settings.edit();
		editor.putLong(Configuration.lastSaveTime.toString(), time);
		editor.commit();
	}

}
