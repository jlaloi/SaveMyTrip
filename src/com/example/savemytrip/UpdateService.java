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
import android.location.LocationProvider;
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
	private int gpsQueries, remaining;

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
		gpsQueries = 0;
		remaining = 0;
	}

	public void onStart(Intent intent, int startId) {
		Log.w(LOG, "onStart " + intent);
		if (intent != null) {
			if (!isRunning() && ACTION_UPDATE.equals(intent.getAction())) {
				locationManager.removeUpdates(this);
				remaining = 0;
				gpsQueries = 0;
			} else if (isRunning()) {
				update();
			}
		}
	}

	private void update() {
		Log.i(LOG, "update");
		if (isRunning()) {
			if (remaining == 0) {
				Criteria criteria = new Criteria();
				String provider = locationManager.getBestProvider(criteria, false);
				locationManager.requestLocationUpdates(provider, 1000, 0, this);
				remaining--;
			} else if (isGPSActivated()) {
				if (remaining > 0) {
					updateText(R.id.saved, getResources().getString(R.string.next, remaining + 1));
				} else {
					updateText(R.id.saved, getResources().getString(R.string.waiting_iteration, Math.abs(remaining + 1), Factory.gpsQueryNumber - gpsQueries));
				}
				remaining--;
			}
		}
	}

	private boolean isGPSActivated() {
		boolean result = false;
		LocationManager service = (LocationManager) getSystemService(LOCATION_SERVICE);
		result = service.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if (!result) {
			updateText(R.id.saved, getResources().getString(R.string.activiateGPS));
		}
		return result;
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
			remaining = Factory.cycleTime;
			gpsQueries = 0;
		} else {
			updateText(R.id.saved, getString(R.string.waiting_iteration, Math.abs(remaining + 1), Factory.gpsQueryNumber - gpsQueries));
		}
	}

	public boolean isRunning() {
		return settings.getBoolean(Configuration.running.toString(), false);
	}

	public void onProviderDisabled(String provider) {
		Log.i(LOG, "onProviderDisabled " + provider);
		update();
	}

	public void onProviderEnabled(String provider) {
		Log.i(LOG, "onProviderEnabled " + provider);
		gpsQueries = 0;
		update();
	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.e(LOG, "onStatusChanged " + provider + " - " + status);
		if (status == LocationProvider.OUT_OF_SERVICE || status == LocationProvider.TEMPORARILY_UNAVAILABLE) {
			gpsQueries = 0;
		}
	}

	public IBinder onBind(Intent intent) {
		return null;
	}

}
