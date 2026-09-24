package com.arashi

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.arashi.zybos.ZybosService
import com.arashi.xposed.SettingsProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import top.yukonga.miuix.kmp.basic.Card
import top.yukonga.miuix.kmp.basic.Text
import top.yukonga.miuix.kmp.basic.Surface
import top.yukonga.miuix.kmp.theme.MiuixTheme

class MiuixMainActivity : ComponentActivity() {
    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        setContent { MiuixHome(this) }
    }
}

@androidx.compose.runtime.Composable
private fun MiuixHome(activity: ComponentActivity) {
    var zybos by remember { mutableStateOf("检测中") }
    var lsposed by remember { mutableStateOf("检测中") }
    var supported by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        val result = withContext(Dispatchers.IO) {
            val brand = try {
                Runtime.getRuntime().exec(arrayOf("/system/bin/getprop", "ro.product.system.brand"))
                    .inputStream.bufferedReader().readLine()?.trim().orEmpty()
            } catch (_: Throwable) {
                ""
            }
            val usable = try {
                ZybosService.detect(activity)
                ZybosService.isAvailable()
            } catch (_: Throwable) {
                false
            }
            val boot = try {
                Settings.Global.getInt(activity.contentResolver, "boot_count", -1)
            } catch (_: Throwable) {
                -1
            }
            val reportedBoot = activity.getSharedPreferences(
                SettingsProvider.STATUS_PREF_NAME,
                ComponentActivity.MODE_PRIVATE,
            ).getInt(SettingsProvider.KEY_SYSTEM_SERVER_BOOT, -1)
            val hookActive = boot >= 0 && reportedBoot == boot
            Triple(brand, usable, hookActive)
        }
        supported = result.first == "ZYB"
        zybos = if (result.second) "可用" else "不可用"
        lsposed = if (result.third) "已激活" else "未确认"
    }
    MiuixTheme {
        val colors = MiuixTheme.colorScheme
        Surface(modifier = Modifier.fillMaxSize(), color = colors.background) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 28.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Text("Arashi", fontSize = 34.sp, fontWeight = FontWeight.Bold)
                Text("版本 ${BuildConfig.VERSION_NAME}", color = colors.onBackgroundVariant)
                if (!supported) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Text("当前设备不支持 Arashi")
                    }
                } else {
                    StatusCard(lsposed, zybos)
                    ActionCard("Hook 功能设置", "控制 Hook 是否启用") {
                        activity.startActivity(Intent(activity, HookSettings::class.java))
                    }
                    ActionCard("系统特权工具", "利用 ZybosSysApiManager 执行系统操作") {
                        activity.startActivity(Intent(activity, ZybosTools::class.java))
                    }
                    ActionCard("应用偏好设置", "配置主题、外观和安装器") {
                        activity.startActivity(Intent(activity, ArashiSettings::class.java))
                    }
                    ActionCard("关于 Arashi", "版本、作者和项目信息") {
                        activity.startActivity(Intent(activity, About::class.java))
                    }
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
private fun StatusCard(lsposed: String, zybos: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Text("系统状态", fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("LSPosed")
            Text(lsposed, color = if (lsposed == "已激活") Color(0xFF32A852) else Color(0xFFD93025))
        }
        Spacer(Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("ZybosSysApiManager")
            Text(zybos, color = if (zybos == "可用") Color(0xFF32A852) else Color(0xFFD93025))
        }
    }
}

@androidx.compose.runtime.Composable
private fun ActionCard(title: String, summary: String, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        insideMargin = PaddingValues(horizontal = 20.dp, vertical = 17.dp),
        onClick = onClick,
        showIndication = true,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(5.dp))
                Text(summary, color = MiuixTheme.colorScheme.onSurfaceSecondary)
            }
            Text("›", fontSize = 28.sp, color = MiuixTheme.colorScheme.onSurfaceVariantActions)
        }
    }
}
