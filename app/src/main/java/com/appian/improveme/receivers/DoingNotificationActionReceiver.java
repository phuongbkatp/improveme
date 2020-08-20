package com.appian.improveme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.util.Pair;

import com.appian.improveme.App;
import com.appian.improveme.Def;
import com.appian.improveme.activities.DoingActivity;
import com.appian.improveme.helpers.RemoteActionHelper;
import com.appian.improveme.model.DoingRecord;
import com.appian.improveme.model.Thing;
import com.appian.improveme.services.DoingService;

/**
 * Created by qiizhang on 2016/11/3.
 * receiver for doing notification actions
 */
public class DoingNotificationActionReceiver extends BroadcastReceiver {

    public static final String TAG = "DoingNotificationActionReceiver";

    public static final String ACTION_FINISH       = TAG + ".finish";
    public static final String ACTION_USER_CANCEL  = TAG + ".user_cancel";
    public static final String ACTION_STOP_SERVICE = TAG + ".stop_service";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (!ACTION_FINISH.equals(action)
                && !ACTION_USER_CANCEL.equals(action)
                && !ACTION_STOP_SERVICE.equals(action)) {
            return;
        }

        if (ACTION_FINISH.equals(action)) {
            DoingService.sStopReason = DoingRecord.STOP_REASON_FINISH;
            long thingId = intent.getLongExtra(Def.Communication.KEY_ID, -1L);
            Pair<Thing, Integer> pair = App.getThingAndPosition(context, thingId, -1);
            Thing thing = pair.first;
            if (thing != null) {
                @Thing.Type int thingType = thing.getType();
                if (thingType == Thing.HABIT) {
                    long hrTime = intent.getLongExtra(Def.Communication.KEY_TIME, -1);
                    if (!RemoteActionHelper.finishHabitOnce(context, thing, pair.second, hrTime)) {
                        DoingService.sStopReason = DoingRecord.STOP_REASON_CANCEL_USER;
                    }
                } else {
                    RemoteActionHelper.finishReminder(context, thing, pair.second);
                }
            }
        } else if (ACTION_USER_CANCEL.equals(action)) {
            DoingService.sStopReason = DoingRecord.STOP_REASON_CANCEL_USER;
        }

        context.sendBroadcast(new Intent(DoingActivity.BROADCAST_ACTION_JUST_FINISH));
        context.stopService(new Intent(context, DoingService.class));
    }
}
