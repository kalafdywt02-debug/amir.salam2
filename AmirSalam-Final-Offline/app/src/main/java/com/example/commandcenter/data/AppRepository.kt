package com.example.commandcenter.data

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.compose.ui.graphics.asImageBitmap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

// Reads the list of launchable apps once. Icons are rasterized off the
// main thread since PackageManager and drawable decoding can be slow.
class AppRepository(private val context: Context) {

    suspend fun loadApps(): List<InstalledAppInfo> = withContext(Dispatchers.Default) {
        val pm = context.packageManager
        val intent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LAUNCHER)
        pm.queryIntentActivities(intent, 0)
            .filter { it.activityInfo.packageName != context.packageName }
            .distinctBy { it.activityInfo.packageName }
            .map { resolveInfo ->
                val activityInfo = resolveInfo.activityInfo
                InstalledAppInfo(
                    label = resolveInfo.loadLabel(pm).toString(),
                    packageName = activityInfo.packageName,
                    activityName = activityInfo.name,
                    icon = drawableToBitmap(resolveInfo.loadIcon(pm)).asImageBitmap()
                )
            }
            .sortedBy { it.label.lowercase() }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        val size = 128
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, size, size)
        drawable.draw(canvas)
        return bitmap
    }
}
