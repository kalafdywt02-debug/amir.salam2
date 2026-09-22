package com.example.commandcenter.data

import androidx.compose.ui.graphics.ImageBitmap

data class InstalledAppInfo(
    val label: String,
    val packageName: String,
    val activityName: String,
    val icon: ImageBitmap
)
