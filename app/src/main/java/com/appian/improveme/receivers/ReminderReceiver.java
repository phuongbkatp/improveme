package com.appian.improveme.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.util.Pair;
import android.widget.Toast;

import com.appian.improveme.App;
import com.appian.improveme.Def;
import com.appian.improveme.R;
import com.appian.improveme.activities.NoticeableNotificationActivity;
import com.appian.improveme.database.ReminderDAO;
import com.appian.improveme.helpers.RemoteActionHelper;
import com.appian.improveme.helpers.ThingDoingHelper;
import com.appian.improveme.model.Reminder;
import com.appian.improveme.model.Thing;
import com.appian.improveme.utils.DeviceUtil;
import com.appian.improveme.utils.SystemNotificationUtil;

import java.util.List;

/**
 * Created by ywwynm on 2015/9/8.
 * A subclass of {@link BroadcastReceiver} for {@link Reminder}.
 */

public class ReminderReceiver extends BroadcastReceiver {

    public static final String TAG = "ReminderReceiver";

    public ReminderReceiver() { }

    @Override
    public void onReceive(Context context, Intent intent) {
        long id = intent.getLongExtra(Def.Communication.KEY_ID, 0);
        ReminderDAO reminderDAO = ReminderDAO.getInstance(context);
        Pair<Thing, Integer> pair = App.getThingAndPosition(context, id, -1);
        Thing thing = pair.first;
        if (thing == null) {
            reminderDAO.delete(id);
            return;
        }
        int position = pair.second;

        @Thing.Type int typeBefore = thing.getType();

        Reminder reminder = reminderDAO.getReminderById(id);
        if (reminder == null) {
            RemoteActionHelper.correctIfNoReminder(context, thing, position, typeBefore);
            return;
        }

        if (reminder.getState() == Reminder.UNDERWAY) {

            ThingDoingHelper helper = new ThingDoingHelper(context, thing);
            boolean shouldAutoStartDoing = helper.shouldAutoStartDoing();
            if (thing.getState() == Thing.UNDERWAY
                    && App.getDoingThingId() == -1 && shouldAutoStartDoing) {
                updateReminderState(reminder, reminderDAO, context, thing, position,
                        typeBefore, Reminder.EXPIRED);
                helper.startDoingAuto(-1, -1);
                return;
            }

            List<Long> runningDetailActivities = App.getRunningDetailActivities();
            for (Long thingId : runningDetailActivities) {
                if (thingId == id) {
                    int reminderStateAfter = thing.getState() == Thing.UNDERWAY ?
                            Reminder.REMINDED : Reminder.EXPIRED;
                    updateReminderState(reminder, reminderDAO, context, thing, position,
                            typeBefore, reminderStateAfter);
                    Toast.makeText(context, R.string.notification_toast_checking_when_alarm_comes,
                            Toast.LENGTH_LONG).show();
                    return;
                }
            }

            if (thing.getState() == Thing.UNDERWAY) {
                updateReminderState(reminder, reminderDAO, context, thing, position,
                        typeBefore, App.getDoingThingId() == id ? Reminder.EXPIRED : Reminder.REMINDED);
                if (App.getDoingThingId() != id) { // doing another thing
                    if (shouldAutoStartDoing) {
                        Toast.makeText(context, R.string.auto_start_doing_notification_toast_doing_another,
                                Toast.LENGTH_LONG).show();
                    }
                    notifyUser(context, id, position, thing);
                } else { // doing this thing
                    Toast.makeText(context, R.string.start_doing_notification_toast_doing_this,
                            Toast.LENGTH_LONG).show();
                }
            } else {
                updateReminderState(reminder, reminderDAO, context, thing, position,
                        typeBefore, Reminder.EXPIRED);
            }
        }
    }

    private void updateReminderState(
            Reminder reminder, ReminderDAO reminderDAO,
            Context context, Thing thing, int position,
            @Thing.Type int typeBefore, int reminderStateAfter) {
        reminder.setState(reminderStateAfter);
        reminderDAO.update(reminder);
        RemoteActionHelper.updateUiEverywhere(
                context, thing, position, typeBefore,
                Def.Communication.RESULT_UPDATE_THING_DONE_TYPE_SAME);
    }

    private void notifyUser(Context context, long id, int position, Thing thing) {
        SharedPreferences sp = context.getSharedPreferences(
                Def.Meta.PREFERENCES_NAME, Context.MODE_PRIVATE);
        boolean moreNoticeable = sp.getBoolean(Def.Meta.KEY_NOTICEABLE_NOTIFICATION, true);
        notifyUserBySystemNotification(context, id, position, thing, moreNoticeable);
        if (moreNoticeable) {
            Intent intent = NoticeableNotificationActivity.getOpenIntentForReminder(context, id, position);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            context.startActivity(intent);
        }
    }

    private void notifyUserBySystemNotification(
            Context context, long id, int position, Thing thing, boolean moreNoticeable) {
        NotificationCompat.Builder builder = SystemNotificationUtil
                .newGeneralNotificationBuilder(context, TAG, id, position, thing, false);
        if (moreNoticeable && DeviceUtil.hasLollipopApi()) {
            // if we use a dialog to notify this alarm, we don't need to show heads-up notification
            builder.setPriority(NotificationCompat.PRIORITY_DEFAULT);
        }

        SystemNotificationUtil.addActionsForReminderNotification(
                builder, context, id, position, thing.getType());

        Intent deleteIntent = new Intent(context, ReminderNotificationActionReceiver.class);
        deleteIntent.setAction(Def.Communication.NOTIFICATION_ACTION_CANCEL);
        deleteIntent.putExtra(Def.Communication.KEY_ID, id);
        builder.setDeleteIntent(PendingIntent.getBroadcast(
                context, (int) id, deleteIntent, PendingIntent.FLAG_UPDATE_CURRENT));

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
        notificationManager.notify((int) id, builder.build());
    }
}
