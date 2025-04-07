package com.example.appscheduler.ui.components

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ShowInstalledApps(
    modifier: Modifier = Modifier,
    onAppSelected: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val packageManager = LocalContext.current.packageManager
    var installedApps by remember { mutableStateOf(emptyList<ApplicationInfo>()) }
    var appIcons by remember { mutableStateOf(mapOf<String, ImageBitmap>()) }

    LaunchedEffect(key1 = Unit) {
        val apps = packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
            .filter { appInfo ->
                (appInfo.flags and (ApplicationInfo.FLAG_SYSTEM or ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) == 0
            }
        installedApps = apps
        val icons = withContext(Dispatchers.IO) {
            apps.associate { appInfo ->
                val iconBitmap = loadIconBitmap(packageManager, appInfo)
                appInfo.packageName to iconBitmap.asImageBitmap()
            }
        }
        appIcons = icons
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        },
        text = {
            Box(modifier = Modifier) {
                if (installedApps.isNotEmpty()) {
                    LazyColumn {
                        items(installedApps) { appInfo ->
                            InstalledAppItem(
                                appInfo = appInfo,
                                appIcon = appIcons[appInfo.packageName],
                                onAppSelected = onAppSelected
                            )
                        }
                    }
                } else {
                    Text(text = "No installed apps")
                }
            }
        }
    )
}

@Composable
fun InstalledAppItem(
    appInfo: ApplicationInfo,
    appIcon: ImageBitmap?,
    onAppSelected: (String) -> Unit
) {
    val packageManager = LocalContext.current.packageManager

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onAppSelected(appInfo.packageName) }
            .padding(8.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (appIcon != null) {
                Image(
                    bitmap = appIcon,
                    contentDescription = "App Icon",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = packageManager.getApplicationLabel(appInfo).toString(),
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = appInfo.packageName,
                    style = MaterialTheme.typography.titleMedium
                )
            }
        }
        HorizontalDivider(
            modifier = Modifier.padding(top = 8.dp),
            thickness = 1.dp,
            color = Color.LightGray
        )
    }
}

private fun loadIconBitmap(packageManager: PackageManager, appInfo: ApplicationInfo): Bitmap {
    return try {
        appInfo.loadIcon(packageManager).toBitmap()
    } catch (_: Exception) {
        createBitmap(1, 1)
    }
}