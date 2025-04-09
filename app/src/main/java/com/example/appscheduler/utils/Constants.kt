package com.example.appscheduler.utils

object Constants {
    object BroadcastReceiver {
        const val ACTION_APP_LAUNCH = "com.example.appscheduler.APP_LAUNCH"
        const val EXTRA_PACKAGE_NAME = "package_name"
        const val EXTRA_SCHEDULED_TIME = "scheduled_time"
        const val EXTRA_SCHEDULE_ID = "schedule_id"
    }
    object SharedPreferences {
        const val LOGGER_KEY = "log_key"
        const val SCHEDULE_LIST_KEY = "alarm_list"
        const val APP_SCHEDULER_SHARED_PREF = "app_scheduler_shared_pref"
    }
    object Notification {
        const val CHANNEL_ID = "notification_channel_id"
        const val CHANNEL_NAME = "App Launch Channel"
        const val CHANNEL_DESCRIPTION = "Channel for launching other apps"
    }
    object Formatting {
        const val DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm"
    }
}