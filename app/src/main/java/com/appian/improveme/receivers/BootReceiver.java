package com.appian.improveme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appian.improveme.appwidgets.AppWidgetHelper;
import com.appian.improveme.helpers.AlarmHelper;
import com.appian.improveme.utils.SystemNotificationUtil;

/**
 * Created by ywwynm on 2016/2/6.
 * Used to set alarms for reminders/habits/goals after device reboots.
 */
public class BootReceiver extends BroadcastReceiver {

    public static final String TAG = "BootReceiver";

    public BootReceiver() {}

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            Log.i(TAG, "Device boot, EverythingDone is responding...");

            final Context appContext = context.getApplicationContext();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    AlarmHelper.createAllAlarms(appContext, true);
                    Log.i(TAG, "Alarms set.");

                    SystemNotificationUtil.tryToCreateQuickCreateNotification(appContext);
                    Log.i(TAG, "Quick Create Notification created.");

                    SystemNotificationUtil.tryToCreateThingOngoingNotification(appContext);

                    AppWidgetHelper.updateAllAppWidgets(appContext);
                    Log.i(TAG, "App widgets updated.");

                    Log.i(TAG, "Everything Done after device boot.");
                }
            }).start();

        }
    }
}
