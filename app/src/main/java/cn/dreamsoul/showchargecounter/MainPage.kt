package cn.dreamsoul.showchargecounter

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import cn.dreamsoul.showchargecounter.persistence.dataStore
import cn.dreamsoul.showchargecounter.persistence.saveMaxAndMin
import cn.dreamsoul.showchargecounter.viewmodel.BatteryInfoViewModel
import kotlinx.coroutines.runBlocking

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatteryInfoScreen(innerPadding: PaddingValues) {
    val context = LocalContext.current as Activity
//    val viewModel = BatteryInfoViewModel(context.dataStore)
    val viewModel = BatteryInfoViewModel.getInstance(context.dataStore)
    val minimumChargeCounterState by viewModel.minimumChargeCounterFlow.collectAsState(initial = 0)
    val maximumChargeCounterState by viewModel.maximumChargeCounterFlow.collectAsState(initial = 0)
    val chargeCounterState by viewModel.chargeCounterFlow.collectAsState(initial = 0)
    var maximumChargeCounter by remember { mutableStateOf("0") }
    var minimumChargeCounter by remember { mutableStateOf("0") }
    var currentChargeCounter by remember { mutableStateOf("0") }
    LaunchedEffect(maximumChargeCounterState, minimumChargeCounterState,viewModel.chargeCounterFlow) {
        maximumChargeCounter = maximumChargeCounterState.toString()
        minimumChargeCounter = minimumChargeCounterState.toString()
        viewModel.chargeCounterFlow.collect{newValue->
            currentChargeCounter = newValue.toString()
        }
        Log.d("UI","current: $currentChargeCounter")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("charge counter: ${currentChargeCounter}mAh",modifier= Modifier.padding(bottom= 16.dp))
        OverlayPermissionCard()
        Spacer(modifier = Modifier.padding(top = 32.dp))
        PowerSavingStrategyPermissionCard()
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("自动关机的ChargeCounter值")
            TextField(
                value = minimumChargeCounter,
                onValueChange = { newValue ->
                    // 限制输入为整数
                    if (newValue.all { it.isDigit() || it == '-' }) {
                        minimumChargeCounter = newValue
                    }
                },
                label = { Text("请输入整数") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top = 32.dp)
        ) {
            Text("满电量的ChargeCounter值")
            TextField(
                value = maximumChargeCounter,
                onValueChange = { newValue ->
                    // 限制输入为整数
                    if (newValue.all { it.isDigit() || it == '-' }) {
                        maximumChargeCounter = newValue
                    }
                },
                label = { Text("请输入整数") },
                keyboardOptions = KeyboardOptions.Default.copy(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
        }
        Spacer(modifier = Modifier.padding(top = 64.dp))
        Text(text = "当前配置:  ${minimumChargeCounterState}mAh~${maximumChargeCounterState}mAh")
        Button(onClick = {
            runBlocking {
                saveMaxAndMin(context, maximumChargeCounter, minimumChargeCounter)
            }
        }) {
            Text(text = "保存")
        }

    }

}


@Composable
fun OverlayPermissionCard() {

    val context = LocalContext.current as Activity

    var overlayPermisson by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context)
        )
    }
    val startOverlayPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (Settings.canDrawOverlays(context)) {
                // 用户授权
                overlayPermisson = true
                context.startService(Intent(context, FloatingTextService::class.java))
            }
        }
    }
    DisposableEffect(Unit) {
        if (overlayPermisson) {
            context.startService(Intent(context, FloatingTextService::class.java))
        }
        onDispose {
            // 执行清理操作
        }
    }
//    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M &&Settings.canDrawOverlays(context)) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        okCard(text = "您的手机系统 < Android6.0，没有悬浮窗权限控制，不需要申请权限。")
    } else if (overlayPermisson) {
        okCard(text = "已获取悬浮窗权限")
    } else {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column() {
                Text(
                    text = "本应用需要悬浮窗权限，使得电量可以悬浮在屏幕右上角显示。",
                    modifier = Modifier
                        .padding(16.dp)
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                            if (!Settings.canDrawOverlays(context)) {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                startOverlayPermissionRequest.launch(intent)
                            } else {
                                context.startService(
                                    Intent(
                                        context,
                                        FloatingTextService::class.java
                                    )
                                )
                            }
                        } else {
                            context.startService(Intent(context, FloatingTextService::class.java))
                        }
                    }, modifier = Modifier.padding(16.dp)) {
                        Text(text = "去申请")
                    }
                }
            }
        }
    }

}

@Composable
fun PowerSavingStrategyPermissionCard() {
    val context = LocalContext.current as Activity
    val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager
    var powerSavingStrategyPermission by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.M ||
                    powerManager.isIgnoringBatteryOptimizations(context.packageName)
        )
    }
    val startPowerSavingStrategyPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
                // 用户授权
                powerSavingStrategyPermission = true
            }
        }
    }
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        okCard(text = "您的手机系统 < Android6.0，没有电池优化功能，不需要申请权限。")
    } else if (powerSavingStrategyPermission) {
        okCard(text = "后台运行无限制")
    } else {
        Card(
            elevation = CardDefaults.cardElevation(
                defaultElevation = 6.dp
            )
        ) {
            Column() {
                Text(
                    text = "本应用需要把省电策略调整为无限制，使得该应用的后台服务不会在息屏几分钟后被杀死。",
                    modifier = Modifier
                        .padding(16.dp)
                )
                Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    Button(onClick = {
                        val powerManager =
                            context.getSystemService(Context.POWER_SERVICE) as PowerManager
                        val isBatteryOptimizationOn: Boolean =
                            powerManager.isIgnoringBatteryOptimizations(context.packageName)
                        if (isBatteryOptimizationOn) {
                            Log.d("T", "不限制用电")
                        } else {
                            Log.d("T", "准备开启省电策略页面")
                            val intent = Intent().apply {
                                action = Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS
                                data = android.net.Uri.parse("package:${context.packageName}")
                            }
                            startPowerSavingStrategyPermissionRequest.launch(intent)
//                            context.startActivity(intent)
                            Log.d("T", "省电策略")
                        }
                    }, modifier = Modifier.padding(16.dp)) {
                        Text(text = "去申请")
                    }
                }
            }
        }
    }
}

@Composable
fun okCard(text: String) {
    Card(
        elevation = CardDefaults.cardElevation(
            defaultElevation = 6.dp
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = text,
                modifier = Modifier
                    .padding(16.dp)
            )
            Image(
                painter = painterResource(id = R.drawable.correct), contentDescription = "已有权限",
                modifier = Modifier
                    .height(32.dp)
                    .padding(end = 16.dp)
            )
        }
    }

}