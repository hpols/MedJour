package com.example.android.medjour;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.widget.RemoteViews;

import com.example.android.medjour.model.EntryExecutor;
import com.example.android.medjour.model.data.JournalDb;
import com.example.android.medjour.utils.JournalUtils;

import java.text.DateFormat;
import java.util.Date;

import timber.log.Timber;

/**
 * Implementation of App Widget functionality.
 */
public class MedJourWidget extends AppWidgetProvider {

    static JournalDb dB;

    static void updateAppWidget(Context ctxt, AppWidgetManager widgetMan, int widgetId) {
        Timber.plant(new Timber.DebugTree());

        dB = JournalDb.getInstance(ctxt);

        final String[] widgetSummary = new String[2];
        EntryExecutor.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                widgetSummary[0] = JournalUtils.toMinutes(JournalUtils.getCumulativeTime(dB));
                Date lastEntry = dB.journalDao().getLastEntryDate();
                widgetSummary[1] = DateFormat.getDateInstance().format(lastEntry);
                Timber.v("cumulative: " + widgetSummary[0] +  "; date:" + widgetSummary[1]);
            }
        });

        String widgetText = ctxt.getString(R.string.total_time_label) + widgetSummary[0] + "\n" +
                ctxt.getString(R.string.widget_last_login) + widgetSummary[1];
        // Construct the RemoteViews object
        RemoteViews views = new RemoteViews(ctxt.getPackageName(), R.layout.med_jour_widget);
        views.setTextViewText(R.id.widget_summary, widgetText);

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

