package com.example.savemytrip;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.savemytrip.Utils.Configuration;

public class UpdateService extends Service implements LocationListener {

	public static final String ACTION_UPDATE = "com.example.savemytrip.action.UPDATE";
	public static final String LOG = "UpdateService";

	private LocationManager locationManager;
	private SharedPreferences settings;
	private int gpsQueries = 0;

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
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		setRemaining(1);
	}

	public void onStart(Intent intent, int startId) {
		Log.w(LOG, "onStart " + intent);
		if (intent != null && ACTION_UPDATE.equals(intent.getAction())) {
			if (!isRunning()) {
				locationManager.removeUpdates(this);
				setRemaining(1);
				gpsQueries = 0;
			} else {
				update();
			}
		} else if (intent != null) {
			update();
		}
	}

	private void update() {
		Log.d(LOG, "update");
		if (isRunning()) {
			int remaining = settings.getInt(Configuration.remaining.toString(), 0) - 1;
			LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
			if (!service.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
				updateText(R.id.saved, getResources().getString(R.string.activiateGPS));
			} else {
				if (remaining == 0) {
					Criteria criteria = new Criteria();
					String provider = locationManager.getBestProvider(criteria, false);
					locationManager.requestLocationUpdates(provider, 1000, 0, this);
					updateText(R.id.saved, getResources().getString(R.string.waiting));
				} else if (remaining > 0) {
					updateText(R.id.saved, getResources().getString(R.string.next, remaining));
				} else {
					updateText(R.id.saved, getResources().getString(R.string.waiting_iteration, Math.abs(remaining)));
				}
				setRemaining(remaining);
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

	public void onLocationChanged(Location location) {
		Log.w(LOG, "onLocationChanged " + location + " (" + gpsQueries + ")");
		gpsQueries++;
		if (gpsQueries == Factory.gpsQueryNumber) {
			Utils.addToFile(Factory.getSaveFile(), location);
			locationManager.removeUpdates(this);
			updateText(R.id.current, Utils.formatLocation(location));
			updateText(R.id.saved, getString(R.string.saved));
			setRemaining(Factory.cycleTime);
			gpsQueries = 0;
		}
	}

	public boolean isRunning() {
		return settings.getBoolean(Configuration.running.toString(), false);
	}

	public int getRemaining() {
		return settings.getInt(Configuration.remaining.toString(), 1);
	}

	public void setRemaining(int time) {
		Log.i(LOG, "Set remaining time " + time);
		SharedPreferences.Editor editor = settings.edit();
		editor.putInt(Configuration.remaining.toString(), time);
		editor.commit();
	}

	public void onProviderDisabled(String provider) {
	}

	public void onProviderEnabled(String provider) {
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

}
