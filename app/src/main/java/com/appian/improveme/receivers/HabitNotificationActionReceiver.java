package com.appian.improveme.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.Pair;
import android.widget.Toast;

import com.appian.improveme.App;
import com.appian.improveme.Def;
import com.appian.improveme.R;
import com.appian.improveme.activities.AuthenticationActivity;
import com.appian.improveme.activities.NoticeableNotificationActivity;
import com.appian.improveme.activities.StartDoingActivity;
import com.appian.improveme.database.HabitDAO;
import com.appian.improveme.helpers.RemoteActionHelper;
import com.appian.improveme.model.HabitReminder;
import com.appian.improveme.model.Thing;
import com.appian.improveme.services.DoingService;

public class HabitNotificationActionReceiver extends BroadcastReceiver {

    public static final String TAG = "HabitNotificationActionReceiver";

    public HabitNotificationActionReceiver() { }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        long hrId = intent.getLongExtra(Def.Communication.KEY_ID, -1);
        long thingId;
        if (hrId == -1) { // ongoing Habit
            thingId = context.getSharedPreferences(
                    Def.Meta.PREFERENCES_NAME, Context.MODE_PRIVATE).getLong(
                    Def.Meta.KEY_ONGOING_THING_ID, -1L);
        } else {
            HabitDAO habitDAO = HabitDAO.getInstance(context);
            HabitReminder hr = habitDAO.getHabitReminderById(hrId);
            thingId = hr.getHabitId();

            NotificationManagerCompat nmc = NotificationManagerCompat.from(context);
            nmc.cancel((int) hrId);
        }

        for (Long dId : App.getRunningDetailActivities()) if (dId == thingId) {
            Toast.makeText(context, R.string.notification_toast_checking_action,
                    Toast.LENGTH_LONG).show();
            return;
        }

        int position = intent.getIntExtra(Def.Communication.KEY_POSITION, -1);
        Pair<Thing, Integer> pair = App.getThingAndPosition(context, thingId, position);
        Thing thing = pair.first;
        if (thing == null) {
            return;
        }
        position = pair.second;

        context.sendBroadcast(
                new Intent(NoticeableNotificationActivity.BROADCAST_ACTION_JUST_FINISH)
                        .putExtra(Def.Communication.KEY_ID, thing.getId()));

        long hrTime = intent.getLongExtra(Def.Communication.KEY_TIME, -1);
        if (action.equals(Def.Communication.NOTIFICATION_ACTION_FINISH)) {
            if (thing.isPrivate()) {
                Intent actionIntent = AuthenticationActivity.getOpenIntent(
                        context, TAG, thingId, position,
                        Def.Communication.AUTHENTICATE_ACTION_FINISH,
                        context.getString(R.string.act_finish));
                actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                actionIntent.putExtra(Def.Communication.KEY_TIME, hrTime);
                context.startActivity(actionIntent);
            } else {
                RemoteActionHelper.finishHabitOnce(context, thing, position, hrTime);
            }
        } else if (action.equals(Def.Communication.NOTIFICATION_ACTION_START_DOING)) {
            if (thingId == App.getDoingThingId()) {
                // this only influences actions clicked from a thing ongoing notification
                Toast.makeText(context, R.string.start_doing_doing_this_thing,
                        Toast.LENGTH_LONG).show();
                return;
            }

            Intent actionIntent;
            if (thing.isPrivate()) {
                actionIntent = AuthenticationActivity.getOpenIntent(
                        context, TAG, thingId, position,
                        Def.Communication.AUTHENTICATE_ACTION_START_DOING,
                        context.getString(R.string.start_doing_full_title));
                actionIntent.putExtra(Def.Communication.KEY_TIME, hrTime);
            } else {
                actionIntent = StartDoingActivity.getOpenIntent(
                        context, thing.getId(), position, thing.getColor(),
                        DoingService.START_TYPE_ALARM, hrTime);
            }
            actionIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            context.startActivity(actionIntent);
        }
    }
}
