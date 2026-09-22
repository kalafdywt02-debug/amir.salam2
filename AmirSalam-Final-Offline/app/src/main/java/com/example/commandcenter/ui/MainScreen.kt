package com.example.commandcenter.ui

import android.content.ActivityNotFoundException
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items as gridItems
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.commandcenter.data.InstalledAppInfo
import com.example.commandcenter.data.Rule

@Composable
fun MainScreen(viewModel: AmirSalamViewModel, launchApp: (InstalledAppInfo) -> Unit) {
    val tab by viewModel.currentTab.collectAsState()
    val context = LocalContext.current
    val apps by viewModel.displayedApps.collectAsState()
    val rules by viewModel.rules.collectAsState()
    val appearance by viewModel.appearance.collectAsState()
    val query by viewModel.search.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadApps(context) }

    Surface(Modifier.fillMaxSize(), color = Color(0xFF0B0D12)) {
        Column(Modifier.fillMaxSize()) {
            Box(Modifier.weight(1f)) {
                when (tab) {
                    0 -> HomeTab(
                        apps = apps,
                        columns = appearance.columns,
                        showSearchBar = appearance.showSearchBar,
                        query = query,
                        onQuery = viewModel::setQuery,
                        onOpen = launchApp
                    )
                    else -> SettingsTab(
                        rules = rules,
                        columns = appearance.columns,
                        showSearchBar = appearance.showSearchBar,
                        onToggle = viewModel::toggle,
                        onDelete = viewModel::delete,
                        onDuplicate = viewModel::duplicate,
                        onAdd = viewModel::add,
                        onColumnsChange = viewModel::setColumns,
                        onShowSearchChange = viewModel::setShowSearchBar
                    )
                }
            }
            NavigationBar(containerColor = Color(0xFF11141B)) {
                NavigationBarItem(tab == 0, { viewModel.setTab(0) }, icon = { Icon(Icons.Default.Home, null) }, label = { Text("خانه") })
                NavigationBarItem(tab == 1, { viewModel.setTab(1) }, icon = { Icon(Icons.Default.Settings, null) }, label = { Text("تنظیمات") })
            }
        }
    }
}

@Composable
private fun HomeTab(
    apps: List<Pair<String, InstalledAppInfo>>,
    columns: Int,
    showSearchBar: Boolean,
    query: String,
    onQuery: (String) -> Unit,
    onOpen: (InstalledAppInfo) -> Unit
) {
    Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
        Spacer(Modifier.height(16.dp))
        Text("امیر.سلام", style = MaterialTheme.typography.headlineSmall, color = Color.White, modifier = Modifier.padding(start = 4.dp))
        if (showSearchBar) {
            OutlinedTextField(
                value = query,
                onValueChange = onQuery,
                modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
                singleLine = true,
                label = { Text("جست‌وجوی برنامه") }
            )
        } else {
            Spacer(Modifier.height(12.dp))
        }
        LazyVerticalGrid(columns = GridCells.Fixed(columns), modifier = Modifier.weight(1f)) {
            gridItems(apps, key = { it.second.packageName }) { (name, app) ->
                Column(
                    modifier = Modifier
                        .padding(8.dp)
                        .clickable { onOpen(app) },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        bitmap = app.icon,
                        contentDescription = name,
                        modifier = Modifier.size(56.dp).clip(RoundedCornerShape(14.dp))
                    )
                    Spacer(Modifier.height(6.dp))
                    Text(
                        name,
                        color = Color.White,
                        maxLines = 1,
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsTab(
    rules: List<Rule>,
    columns: Int,
    showSearchBar: Boolean,
    onToggle: (Rule) -> Unit,
    onDelete: (Rule) -> Unit,
    onDuplicate: (Rule) -> Unit,
    onAdd: (String, String, Boolean, Boolean) -> Unit,
    onColumnsChange: (Int) -> Unit,
    onShowSearchChange: (Boolean) -> Unit
) {
    val context = LocalContext.current
    var showAdd by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("تنظیمات", color = Color.White, style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        SettingsSection(title = "ظاهر صفحه اصلی") {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("تعداد ستون‌ها: $columns", color = Color.White)
                Row {
                    OutlinedButton(onClick = { if (columns > 3) onColumnsChange(columns - 1) }) { Text("−") }
                    Spacer(Modifier.width(8.dp))
                    OutlinedButton(onClick = { if (columns < 6) onColumnsChange(columns + 1) }) { Text("+") }
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("نمایش نوار جست‌وجو", color = Color.White)
                Switch(checked = showSearchBar, onCheckedChange = onShowSearchChange)
            }
        }

        Spacer(Modifier.height(16.dp))

        SettingsSection(title = "دسترسی سریع") {
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_SETTINGS))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "تنظیمات گوشی پیدا نشد", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("باز کردن تنظیمات گوشی") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = {
                    try {
                        context.startActivity(Intent(Settings.ACTION_HOME_SETTINGS))
                    } catch (e: ActivityNotFoundException) {
                        Toast.makeText(context, "این گزینه روی این گوشی موجود نیست", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("تغییر صفحه اصلی پیش‌فرض") }
        }

        Spacer(Modifier.height(16.dp))

        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Text("مدیریت اسم‌ها", color = Color.White, style = MaterialTheme.typography.titleMedium)
            Button(onClick = { showAdd = true }) { Text("+ افزودن اسم") }
        }
        Spacer(Modifier.height(8.dp))
        LazyColumn(Modifier.weight(1f, fill = false)) {
            items(rules, key = { it.id }) { rule ->
                Card(
                    Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF151922))
                ) {
                    Column(Modifier.padding(14.dp)) {
                        Text(rule.source, color = Color.White, style = MaterialTheme.typography.titleSmall)
                        Text("↓", color = Color(0xFF7B8797))
                        Text(rule.replacement, color = Color(0xFF8DE1BE))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            TextButton(onClick = { onToggle(rule) }) { Text(if (rule.enabled) "فعال" else "غیرفعال") }
                            TextButton(onClick = { onDuplicate(rule) }) { Text("تکثیر") }
                            TextButton(onClick = { onDelete(rule) }) { Text("حذف") }
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        SupportRow()
    }
    if (showAdd) AddDialog({ showAdd = false }, onAdd)
}

@Composable
private fun SettingsSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Card(Modifier.fillMaxWidth(), colors = CardDefaults.cardColors(containerColor = Color(0xFF151922))) {
        Column(Modifier.padding(16.dp)) {
            Text(title, color = Color(0xFF9AA4B2), style = MaterialTheme.typography.labelLarge)
            Spacer(Modifier.height(10.dp))
            content()
        }
    }
}

private const val SUPPORT_EMAIL = "zwla54329@gmail.com"

@Composable
private fun SupportRow() {
    val context = LocalContext.current
    SettingsSection(title = "پشتیبانی") {
        Text(SUPPORT_EMAIL, color = Color(0xFF8DE1BE))
        Spacer(Modifier.height(8.dp))
        Row {
            TextButton(onClick = {
                val intent = Intent(Intent.ACTION_SENDTO).apply {
                    data = Uri.parse("mailto:")
                    putExtra(Intent.EXTRA_EMAIL, arrayOf(SUPPORT_EMAIL))
                    putExtra(Intent.EXTRA_SUBJECT, "پشتیبانی امیر.سلام")
                }
                try {
                    context.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    Toast.makeText(context, "برنامه ایمیل پیدا نشد", Toast.LENGTH_LONG).show()
                }
            }) { Text("ارسال ایمیل") }
            TextButton(onClick = {
                val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("email", SUPPORT_EMAIL))
                Toast.makeText(context, "ایمیل کپی شد", Toast.LENGTH_SHORT).show()
            }) { Text("کپی آدرس") }
        }
    }
}

@Composable
private fun AddDialog(close: () -> Unit, save: (String, String, Boolean, Boolean) -> Unit) {
    var source by remember { mutableStateOf("") }
    var replacement by remember { mutableStateOf("") }
    var exact by remember { mutableStateOf(false) }
    var sensitive by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = close,
        title = { Text("افزودن اسم جدید") },
        text = {
            Column {
                OutlinedTextField(source, { source = it }, label = { Text("نام اصلی برنامه") })
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(replacement, { replacement = it }, label = { Text("نام دلخواه") })
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(exact, { exact = it }); Text("تطبیق دقیق")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(sensitive, { sensitive = it }); Text("حساس به حروف")
                }
            }
        },
        confirmButton = {
            Button(onClick = {
                save(source, replacement, exact, sensitive)
                close()
            }) { Text("ذخیره") }
        },
        dismissButton = { TextButton(onClick = close) { Text("لغو") } }
    )
}

@Composable
fun AmirSalamTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = darkColorScheme(
            primary = Color(0xFF8DE1BE),
            background = Color(0xFF0B0D12),
            surface = Color(0xFF151922)
        ),
        content = content
    )
}
