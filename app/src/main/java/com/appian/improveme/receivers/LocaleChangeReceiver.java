package com.appian.improveme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.appian.improveme.BuildConfig;
import com.appian.improveme.Def;
import com.appian.improveme.appwidgets.AppWidgetHelper;

/**
 * Created by qiizhang on 2016/8/26.
 * Broadcast used to response to locale change
 */
public class LocaleChangeReceiver extends BroadcastReceiver {

    public static final String TAG = "LocaleChangeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Def.Communication.BROADCAST_ACTION_RESP_LOCALE_CHANGE.equals(intent.getAction())) {
            AppWidgetHelper.updateAllAppWidgets(context);

            if (BuildConfig.DEBUG) {
                Log.i(TAG, "App language has changed, app widgets are updated for that.");
            }
        }
    }
}
