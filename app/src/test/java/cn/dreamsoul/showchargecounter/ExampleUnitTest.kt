package cn.dreamsoul.showchargecounter

import org.junit.Test

import org.junit.Assert.*
import java.util.Timer
import java.util.TimerTask

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun timerTest() {
        val timer = Timer()
        val context = this
        val task = object : TimerTask() {
            var i = 0
            override fun run() {
                i++
                // 任务逻辑
                println("周期任务${i}")
            }
        }
        timer.schedule(task, 0, 1000)
        readLine()
    }
}