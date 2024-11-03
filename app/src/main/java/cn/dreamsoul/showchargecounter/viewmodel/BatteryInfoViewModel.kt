package cn.dreamsoul.showchargecounter.viewmodel

import android.content.Context
import android.os.BatteryManager
import androidx.compose.runtime.mutableStateOf
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cn.dreamsoul.showchargecounter.persistence.MAX_CHARGECOUNTER
import cn.dreamsoul.showchargecounter.persistence.MIN_CHARGECOUNTER
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

//class BatteryInfoViewModel(private val dataStore: DataStore<Preferences>) : ViewModel() {
class BatteryInfoViewModel private constructor(private val dataStore: DataStore<Preferences>) :
    ViewModel() {
    private val _chargeCounter = mutableStateOf<Long?>(null)
    val chargeCounter: Long?
        get() = _chargeCounter.value

    // 如果不加.value，那么是一个val的常量
    private val _chargeCounterFlow = MutableStateFlow(_chargeCounter.value)
    val chargeCounterFlow = _chargeCounterFlow.asStateFlow()
    val maximumChargeCounterFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[MAX_CHARGECOUNTER] ?: "0"
        }
    val minimumChargeCounterFlow: Flow<String> = dataStore.data
        .map { preferences ->
            preferences[MIN_CHARGECOUNTER] ?: "0"
        }

    // 电池的真实剩余电量
    val remainingChargeCounterFlow: Flow<Int> =
        _chargeCounterFlow.combine(minimumChargeCounterFlow) { chargeCounter, minimumChargeCounter ->
            (chargeCounter?.minus(minimumChargeCounter.toLong()))?.toInt() ?: 0
        }

    // 电池的真实最大容量
    private val chargeCounterDifferenceFlow: Flow<Int> =
        maximumChargeCounterFlow.combine(minimumChargeCounterFlow) { a, b ->
            a.toInt() - b.toInt()
        }
    val remainingBatteryPercentFlow: Flow<Int> =
        remainingChargeCounterFlow.combine(chargeCounterDifferenceFlow) { a, b ->
            (a.toDouble() / b * 100).toInt()
        }


    fun getChargeCounter(context: Context) {
        viewModelScope.launch {
            val batteryManager =
                context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            if (batteryManager != null && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.P) {
                _chargeCounter.value =
                    batteryManager.getLongProperty(BatteryManager.BATTERY_PROPERTY_CHARGE_COUNTER) / 1000
                _chargeCounterFlow.value = _chargeCounter.value
            } else {
                _chargeCounter.value = -1L
            }
        }
    }

    companion object {
        @Volatile
        private var instance: BatteryInfoViewModel? = null
        fun getInstance(dataStore: DataStore<Preferences>): BatteryInfoViewModel {
            return instance ?: synchronized(this) { instance ?: BatteryInfoViewModel(dataStore).also { instance = it } }
        }
    }
}
