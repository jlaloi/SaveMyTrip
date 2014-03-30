package com.example.savemytrip;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.RemoteViews;

import com.example.savemytrip.Utils.Configuration;

public class MyWidgetProvider extends AppWidgetProvider {

	public static final String LOG = "MyWidgetProvider";

	public static String CLICK_ACTION = "CLICK_ACTION";

	private SharedPreferences settings;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(LOG, "onUpdate");
		Intent intent = new Intent(context, MyWidgetProvider.class);
		intent.setAction(CLICK_ACTION);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			remoteViews.setOnClickPendingIntent(R.id.widgetLinearLayout, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		Intent serviceIntent = new Intent(context.getApplicationContext(), UpdateService.class);
		serviceIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		context.startService(serviceIntent);
	}

	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		if (intent.getAction().equals(CLICK_ACTION)) {
			Log.i(LOG, CLICK_ACTION);
			settings = context.getSharedPreferences(Utils.PREFS_NAME, 0);
			setRunning(!isRunning());
			if (!isRunning()) {
				AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
				ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
				int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
				for (int widgetId : allWidgetIds) {
					RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
					remoteViews.setTextViewText(R.id.current, context.getString(R.string.app_name));
					remoteViews.setTextViewText(R.id.saved, context.getString(R.string.off));
					appWidgetManager.updateAppWidget(widgetId, remoteViews);
				}
			}
			context.startService(new Intent(UpdateService.ACTION_UPDATE));
		}
	}

	public boolean isRunning() {
		return settings.getBoolean(Configuration.running.toString(), true);
	}

	public void setRunning(boolean bool) {
		Log.i(LOG, "Set running " + bool);
		SharedPreferences.Editor editor = settings.edit();
		editor.putBoolean(Configuration.running.toString(), bool);
		editor.commit();
	}

}
