package com.example.android.medjour.widget;

import android.app.IntentService;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

public class WidgetService extends IntentService {

    private final static String ACTION_UPDATE_WIDGET = "com.example.android.medjour.action.update_widget";

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     */
    public WidgetService() {
        super("WidgetService");
    }

    public static void startHandleActionUpdateWidget(Context ctxt) {
        Intent updateIntent = new Intent(ctxt, WidgetService.class);
        updateIntent.setAction(ACTION_UPDATE_WIDGET);
        ctxt.startService(updateIntent);
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (action.equals(ACTION_UPDATE_WIDGET)) {
                handleActionUpdateWidget();
            }
        }
    }

    private void handleActionUpdateWidget() {
        AppWidgetManager widgetMan = AppWidgetManager.getInstance(this);
        int[] widgetIds = widgetMan.getAppWidgetIds(new ComponentName(this,
                MedJourWidget.class));
        //Now update all widgets
        MedJourWidget.updateMedJourWidgets(this, widgetMan, widgetIds);
    }
}
