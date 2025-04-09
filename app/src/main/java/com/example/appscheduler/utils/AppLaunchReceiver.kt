package com.example.appscheduler.utils

import android.Manifest
import android.R
import android.app.ActivityOptions
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.edit
import com.example.appscheduler.data.models.Logger
import com.example.appscheduler.data.models.Schedule
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject


class AppLaunchReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Constants.BroadcastReceiver.ACTION_APP_LAUNCH) {
            val packageName = intent.getStringExtra(Constants.BroadcastReceiver.EXTRA_PACKAGE_NAME)
            val scheduledTimeString =
                intent.getStringExtra(Constants.BroadcastReceiver.EXTRA_SCHEDULED_TIME)
            val scheduleId = intent.getStringExtra(Constants.BroadcastReceiver.EXTRA_SCHEDULE_ID)
            if (!packageName.isNullOrEmpty()) {
                launchApp(
                    context, packageName, scheduledTimeString.toString(), scheduleId.toString()
                )
            }
        }
    }

    private fun launchApp(
        context: Context, packageName: String, scheduledTimeString: String, scheduleId: String
    ) {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        val sharedPreferences: SharedPreferences = context.getSharedPreferences(
            Constants.SharedPreferences.APP_SCHEDULER_SHARED_PREF, Context.MODE_PRIVATE
        )
        val gson = GsonBuilder().registerTypeAdapter(
            LocalDateTime::class.java, LocalDateTimeAdapter()
        ).create()
        if (launchIntent != null) {
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.component =
                ComponentName(packageName, getMainActivityClassName(context, packageName))

            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            val builder: NotificationCompat.Builder =
                NotificationCompat.Builder(context, Constants.Notification.CHANNEL_ID)
                    .setSmallIcon(R.drawable.btn_star)
                    .setContentTitle(context.getString(com.example.appscheduler.R.string.notification_title)).setContentText(
                        context.getString(com.example.appscheduler.R.string.notification_content))
                    .setPriority(NotificationCompat.PRIORITY_HIGH).setContentIntent(pendingIntent)
                    .setAutoCancel(true)

            val notificationManager = NotificationManagerCompat.from(context)
            if (ActivityCompat.checkSelfPermission(
                    context, Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Toast.makeText(
                    context,
                    context.getString(com.example.appscheduler.R.string.permission_for_notification_not_allowed), Toast.LENGTH_SHORT
                ).show()
                return
            }
            notificationManager.notify(1001, builder.build())
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                val options = ActivityOptions.makeBasic()
                    .setPendingIntentBackgroundActivityStartMode(ActivityOptions.MODE_BACKGROUND_ACTIVITY_START_ALLOWED)
                    .toBundle()
                context.startActivity(launchIntent, options)
            } else {
                context.startActivity(launchIntent)
            }

            // Add to log in sharedPreferences that the application reminder was successfully executed
            var json = sharedPreferences.getString(Constants.SharedPreferences.LOGGER_KEY, null)
            if (json == null) {
                json = emptyList<Logger>().toString()
            }
            val type = object : TypeToken<List<Logger>>() {}.type
            val loggerList = gson.fromJson<List<Logger>>(json, type)
            val newLogger = Logger(
                packageName, scheduledTimeString, LocalDateTime.now(), true
            )
            val updatedLoggerList = loggerList.toMutableList()
            updatedLoggerList.add(newLogger)
            val updatedJson = gson.toJson(updatedLoggerList)
            sharedPreferences.edit {
                putString(
                    Constants.SharedPreferences.LOGGER_KEY, updatedJson
                )
            }

            // Remove the executed schedule from the list
            var scheduleJson = sharedPreferences.getString(
                Constants.SharedPreferences.SCHEDULE_LIST_KEY, null
            )
            if (scheduleJson != null) {
                val scheduleType = object : TypeToken<List<Schedule>>() {}.type
                val scheduleList = gson.fromJson<List<Schedule>>(scheduleJson, scheduleType)
                val updatedScheduleList = scheduleList.filter { it.id.toString() != scheduleId }
                val updatedScheduleJson = gson.toJson(updatedScheduleList)
                sharedPreferences.edit {
                    putString(
                        Constants.SharedPreferences.SCHEDULE_LIST_KEY, updatedScheduleJson
                    )
                }
            }
        } else {
            // Add to log in sharedPreferences that the application reminder was unsuccessful
            val json = sharedPreferences.getString(Constants.SharedPreferences.LOGGER_KEY, null)
            if (json != null) {
                val type = object : TypeToken<List<Logger>>() {}.type
                val loggerList = gson.fromJson<List<Logger>>(json, type)
                val newLogger = Logger(
                    packageName, scheduledTimeString, LocalDateTime.now(), false
                )
                val updatedLoggerList = loggerList.toMutableList()
                updatedLoggerList.add(newLogger)
                val updatedJson = gson.toJson(updatedLoggerList)
                sharedPreferences.edit {
                    putString(
                        Constants.SharedPreferences.LOGGER_KEY, updatedJson
                    )
                }
            }
            Log.e("AppLaunchReceiver", "Failed to launch app: $packageName")
        }
    }

    private fun getMainActivityClassName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName) ?: return ""

        val component = intent.component ?: return ""

        return component.className
    }
}

class AlarmScheduler @Inject constructor(@ApplicationContext private val context: Context) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    fun scheduleAppLaunch(schedule: Schedule) {
        val intent = Intent(context, AppLaunchReceiver::class.java).apply {
            action = Constants.BroadcastReceiver.ACTION_APP_LAUNCH
            putExtra(Constants.BroadcastReceiver.EXTRA_PACKAGE_NAME, schedule.packageName)
            putExtra(
                Constants.BroadcastReceiver.EXTRA_SCHEDULED_TIME, schedule.scheduledTime.toString()
            )
            putExtra(Constants.BroadcastReceiver.EXTRA_SCHEDULE_ID, schedule.id.toString())
        }

        val timeInMillis =
            schedule.scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        if (ActivityCompat.checkSelfPermission(
                context, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Toast.makeText(
                context,
                context.getString(com.example.appscheduler.R.string.permission_for_notification_not_allowed_app_might_not_behave_as_expected),
                Toast.LENGTH_LONG
            ).show()
            return
        }


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                )
            } else {
                Toast.makeText(
                    context,
                    context.getString(com.example.appscheduler.R.string.permission_of_alarms_and_reminders_not_given), Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
            )
        }
    }

    fun cancelAppLaunch(schedule: Schedule) {
        val intent = Intent(context, AppLaunchReceiver::class.java).apply {
            action = Constants.BroadcastReceiver.ACTION_APP_LAUNCH
            putExtra(Constants.BroadcastReceiver.EXTRA_PACKAGE_NAME, schedule.packageName)
            putExtra(
                Constants.BroadcastReceiver.EXTRA_SCHEDULED_TIME, schedule.scheduledTime.toString()
            )
            putExtra(Constants.BroadcastReceiver.EXTRA_SCHEDULE_ID, schedule.id.toString())
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.id.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}