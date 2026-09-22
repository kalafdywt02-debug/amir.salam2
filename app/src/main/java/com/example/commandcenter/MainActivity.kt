package com.example.commandcenter

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.example.commandcenter.data.AppRepository
import com.example.commandcenter.data.InstalledAppInfo
import com.example.commandcenter.data.LauncherPrefsRepository
import com.example.commandcenter.data.RuleRepository
import com.example.commandcenter.ui.AmirSalamTheme
import com.example.commandcenter.ui.AmirSalamViewModel
import com.example.commandcenter.ui.MainScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val vm = AmirSalamViewModel(
            ruleRepo = RuleRepository(applicationContext),
            prefsRepo = LauncherPrefsRepository(applicationContext),
            appRepo = AppRepository(applicationContext)
        )
        setContent {
            AmirSalamTheme {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    MainScreen(
                        viewModel = vm,
                        launchApp = { app -> openApp(app) }
                    )
                }
            }
        }
    }

    private fun openApp(app: InstalledAppInfo) {
        try {
            val intent = Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
                component = ComponentName(app.packageName, app.activityName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
        } catch (e: Exception) {
            Toast.makeText(this, "این برنامه باز نشد", Toast.LENGTH_SHORT).show()
        }
    }

    // Pressing the system Home button while already on the home screen
    // should just stay here, not close the launcher like a normal app.
    override fun onBackPressed() {
        // no-op on the home tab; NavigationBar handles in-app navigation
    }
}
