package com.example.appscheduler.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.appscheduler.data.models.Schedule
import dagger.hilt.android.qualifiers.ApplicationContext
import java.time.LocalDateTime
import java.time.ZoneId
import javax.inject.Inject

class AppLaunchReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("AMI", "onReceive: e ashchi")
        if (intent.action == "com.example.appscheduler.APP_LAUNCH") {
            Log.d("AMI", "onReceive: If bhitor dhuksi")
            val packageName = intent.getStringExtra("package_name")
            if (!packageName.isNullOrEmpty()) {
                Log.d("AMI", "onReceive: App launch er try marsi for $packageName")
                launchApp(context, packageName)
            }
        }
    }

    private fun launchApp(context: Context, packageName: String) {
        val packageManager = context.packageManager
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        Log.d("AMI", "launchApp: $launchIntent")
        if (launchIntent != null) {
            Log.d("AMI", "launchApp: trying to LAUNCH inside if")
            launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            launchIntent.component =
                ComponentName(packageName, getMainActivityClassName(context, packageName))
            context.startActivity(launchIntent)
        } else {
            Log.d("AMI", "launchApp: I failed")
            Log.e("AppLaunchReceiver", "Failed to launch app: $packageName")
        }
    }

    private fun getMainActivityClassName(context: Context, packageName: String): String {
        val packageManager = context.packageManager
        val intent = packageManager.getLaunchIntentForPackage(packageName)
            ?: return "" // Handle case where no launch intent is found

        val component = intent.component
            ?: return "" // Handle case where component is null

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
                Constants.BroadcastReceiver.EXTRA_SCHEDULED_TIME,
                schedule.scheduledTime.toString()
            )
            putExtra(Constants.BroadcastReceiver.EXTRA_SCHEDULE_ID, schedule.id)
        }

        val timeInMillis = schedule.scheduledTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (alarmManager.canScheduleExactAlarms()) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent
                )
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
                Constants.BroadcastReceiver.EXTRA_SCHEDULED_TIME,
                schedule.scheduledTime.toString()
            )
            putExtra(Constants.BroadcastReceiver.EXTRA_SCHEDULE_ID, schedule.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            schedule.hashCode(),
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)
    }
}