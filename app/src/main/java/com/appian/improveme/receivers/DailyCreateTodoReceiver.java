package com.appian.improveme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.appian.improveme.App;
import com.appian.improveme.activities.DetailActivity;

/**
 * Created by ywwynm on 2017/4/23.
 * A broadcast receiver that will open DetailActivity and create a reminder for user to write
 * their TODOs everyday.
 */
public class DailyCreateTodoReceiver extends BroadcastReceiver {

    public static final String TAG = "DailyCreateTodoReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent openIntent = DetailActivity.getOpenIntentForCreate(context, TAG, App.newThingColor);
        openIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        context.startActivity(openIntent);
    }

}
