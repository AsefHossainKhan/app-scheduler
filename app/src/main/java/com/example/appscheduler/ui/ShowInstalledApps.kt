package com.example.appscheduler.ui

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.drawable.toBitmap


@Composable
fun ShowInstalledApps(modifier: Modifier) {
    val packageManager = LocalContext.current.packageManager
    val installedApps = remember { mutableStateOf(listOf<ApplicationInfo>()) }

    LaunchedEffect(key1 = Unit) {
        installedApps.value = packageManager.getInstalledApplications(PackageManager.GET_META_DATA).filter { appInfo ->
            (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0
        }
    }

    Box(modifier = modifier) {
        if (installedApps.value.isNotEmpty()) {
            LazyColumn {
                items(installedApps.value) { appInfo ->
                    InstalledAppItem(appInfo, packageManager)
                }
            }
        } else {
            Text(text = "No installed apps")
        }
    }
}

@Composable
fun InstalledAppItem(appInfo: ApplicationInfo, packageManager: PackageManager) {
    val appName = packageManager.getApplicationLabel(appInfo).toString()
    val packageName = appInfo.packageName
    val icon = appInfo.loadIcon(packageManager).current.toBitmap().asImageBitmap()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Image(
                bitmap = icon,
                contentDescription = "App Icon",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(text = appName, style = MaterialTheme.typography.bodySmall)
                Text(text = packageName, style = MaterialTheme.typography.titleMedium)
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )
    }
}