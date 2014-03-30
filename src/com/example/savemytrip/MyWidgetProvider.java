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

	private SharedPreferences settings;

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(LOG, "onUpdate");
		settings = context.getSharedPreferences(Utils.PREFS_NAME, 0);
		setRunning(!isRunning());
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			if (!isRunning()) {
				remoteViews.setTextViewText(R.id.current, context.getString(R.string.app_name));
				remoteViews.setTextViewText(R.id.saved, context.getString(R.string.off));
			}
			Intent widgetIntent = new Intent(context, MyWidgetProvider.class);
			widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
			PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, widgetIntent, PendingIntent.FLAG_UPDATE_CURRENT);
			remoteViews.setOnClickPendingIntent(R.id.saved, pendingIntent);
			remoteViews.setOnClickPendingIntent(R.id.current, pendingIntent);
			appWidgetManager.updateAppWidget(widgetId, remoteViews);
		}
		Intent intent = new Intent(context.getApplicationContext(), UpdateService.class);
		intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, allWidgetIds);
		context.startService(intent);
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
