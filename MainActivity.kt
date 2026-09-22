package com.example.commandcenter

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.OnBackPressedCallback
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

    private lateinit var vm: AmirSalamViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        vm = AmirSalamViewModel(
            ruleRepo = RuleRepository(applicationContext),
            prefsRepo = LauncherPrefsRepository(applicationContext),
            appRepo = AppRepository(applicationContext)
        )

        // On a launcher, Back should never close the app (there is nothing
        // to return to); it should just take the user to the home tab
        // if they were somewhere else, like Settings.
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (vm.currentTab.value != 0) vm.setTab(0)
            }
        })

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
}
