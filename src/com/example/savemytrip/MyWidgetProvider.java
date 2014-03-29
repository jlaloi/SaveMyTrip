package com.example.savemytrip;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {

	public static final String LOG = "MyWidgetProvider";

	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		Log.d(LOG, "onUpdate");
		Factory.setRunning(!Factory.isRunning());
		ComponentName thisWidget = new ComponentName(context, MyWidgetProvider.class);
		int[] allWidgetIds = appWidgetManager.getAppWidgetIds(thisWidget);
		for (int widgetId : allWidgetIds) {
			RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
			if (!Factory.isRunning()) {
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

}
