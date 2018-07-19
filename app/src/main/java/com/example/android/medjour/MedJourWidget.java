package com.example.android.medjour;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.ui.NewEntryActivity;
import com.example.android.medjour.utils.JournalUtils;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class MedJourWidget extends AppWidgetProvider {

    static JournalDb dB;

    static void updateAppWidget(Context ctxt, AppWidgetManager widgetMan, int widgetId) {
        Timber.plant(new Timber.DebugTree());

        dB = JournalDb.getInstance(ctxt);

        RemoteViews views = new RemoteViews(ctxt.getPackageName(), R.layout.med_jour_widget);

        String widgetText = ctxt.getString(R.string.total_time_label)
                + String.valueOf(JournalUtils.toMinutes(JournalUtils.retrieveCumulativeTime(ctxt)))
                + "\n" + ctxt.getString(R.string.widget_last_login)
                + JournalUtils.retireveLastDate(ctxt);
        // Construct the RemoteViews object
        views.setTextViewText(R.id.widget_summary, widgetText);

        //setup click to open journal-flow
        Intent intent = new Intent(ctxt, NewEntryActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(ctxt, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget_entry_bt, pendingIntent);

        // Instruct the widget manager to update the widget
        widgetMan.updateAppWidget(widgetId, views);
    }

    @Override
    public void onUpdate(Context ctxt, AppWidgetManager widgetMan, int[] widgetIds) {
        // There may be multiple widgets active, so update all of them
        for (int appWidgetId : widgetIds) {
            updateAppWidget(ctxt, widgetMan, appWidgetId);
        }
    }

    @Override
    public void onEnabled(Context ctxt) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context ctxt) {
        // Enter relevant functionality for when the last widget is disabled
    }
}

