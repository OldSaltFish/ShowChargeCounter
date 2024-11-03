package cn.dreamsoul.showchargecounter
import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import cn.dreamsoul.showchargecounter.persistence.dataStore
import cn.dreamsoul.showchargecounter.viewmodel.BatteryInfoViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class FloatingTextService : Service() {

    private lateinit var windowManager: WindowManager
    private lateinit var floatingView: View
    private var isInitialized = false
//    private val batteryInfoViewModel = BatteryInfoViewModel(this.dataStore)
    private val batteryInfoViewModel = BatteryInfoViewModel.getInstance(this.dataStore)
//    private val batteryInfoViewModel by activityViewModels<BatteryInfoViewModel>()
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.Main + job)
    var remainingBattery:Long = 0L
    var remainingBatteryPercent:Int = 0
    private val handler = Handler(Looper.getMainLooper())
    private val runnable = object : Runnable {
        override fun run() {
            getOrRefreshChargeCounter()
            // 每分钟（60000 毫秒）后再次运行这个 Runnable
            // 5秒刷新一次显示
            handler.postDelayed(this, 5000)
        }
    }
    // 刷新电量
    private fun getOrRefreshChargeCounter(){
        Log.d("Battery","电量刷新")
        batteryInfoViewModel.getChargeCounter(this)

//        (floatingView.findViewById<TextView>(R.id.floatingTextView)).text = "${batteryInfoViewModel.chargeCounter?.div(
//            1000
//        )}mAh"
        (floatingView.findViewById<TextView>(R.id.floatingTextView)).text = "$remainingBattery mAh ${remainingBatteryPercent}%"

    }
    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        // 创建一个 Handler 和 Runnable 来处理定时任务
        handler.post(runnable)
        createFloatingView()
        scope.launch {
            launch {
//                batteryInfoViewModel.minimumChargeCounterFlow.collect { newValue ->
//                    remainingBattery =  batteryInfoViewModel.chargeCounter?.minus(newValue.toLong())?:0L
//                }
                batteryInfoViewModel.remainingChargeCounterFlow.collect { newValue ->
                    remainingBattery =  newValue.toLong()
                }
            }
            launch {
                batteryInfoViewModel.remainingBatteryPercentFlow.collect { newValue ->
//                    println("满电量：$newValue  剩余: $remainingBattery")
                    remainingBatteryPercent =  newValue
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (isInitialized) {
            windowManager.removeView(floatingView)
        }
        handler.removeCallbacksAndMessages(null)
    }
    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)

        // 检测到应用被划掉时的处理逻辑
        // 停止服务（可选）
        stopSelf()
    }

    private fun createFloatingView() {
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                WindowManager.LayoutParams.TYPE_PHONE
            },
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        )

        layoutParams.gravity = Gravity.TOP or Gravity.END

        floatingView = LayoutInflater.from(this).inflate(R.layout.floating_text_view, null)

        if (!isInitialized) {
            windowManager.addView(floatingView, layoutParams)
            isInitialized = true
        }
    }
}