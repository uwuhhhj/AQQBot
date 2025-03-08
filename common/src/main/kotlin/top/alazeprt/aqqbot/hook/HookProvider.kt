package top.alazeprt.aqqbot.hook

import me.lucko.spark.api.Spark
import me.lucko.spark.api.SparkProvider
import org.geysermc.floodgate.api.FloodgateApi
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.util.LogLevel

interface HookProvider {

    var spark: Spark?

    var floodgateApi: FloodgateApi?

    var loadSparkCount: Int
    var loadFloodgateCount: Int

    fun loadSpark(plugin: AQQBot) {
        try {
            Class.forName("me.lucko.spark.api.SparkProvider")
            spark = SparkProvider.get()
            if (loadSparkCount > 0) {
                plugin.log(LogLevel.INFO, "Spark has been loaded successfully!")
            }
            loadSparkCount = 0
        } catch (e: ClassNotFoundException) {
            plugin.log(LogLevel.WARN, "You don't install soft dependency: Spark! You can't get server status via this plugin!")
        } catch (e: IllegalStateException) {
            if (loadSparkCount >= 5) {
                plugin.log(LogLevel.WARN, "After five attempts Spark still does not work and will stop trying, you can then retry via the /aqqbot reload")
                return
            }
            plugin.log(LogLevel.WARN, "Spark has not loaded yet! We'll try to load it 2 seconds later.")
            plugin.submitLaterAsync(40L) {
                loadSparkCount++
                loadSpark(plugin)
            }
        }
    }

    fun loadFloodgate(plugin: AQQBot) {
        try {
            Class.forName("org.geysermc.floodgate.api.FloodgateApi")
            floodgateApi = FloodgateApi.getInstance()
            if (loadFloodgateCount > 0) {
                plugin.log(LogLevel.INFO, "Floodgate has been loaded successfully!")
            }
            loadFloodgateCount = 0
        } catch (e: IllegalStateException) {
            if (loadFloodgateCount >= 3) {
                return
            }
            plugin.submitLaterAsync(40L) {
                loadFloodgateCount++
                loadFloodgate(plugin)
            }
        }
    }

    fun loadHook(plugin: AQQBot) {
        loadSpark(plugin)
        loadFloodgate(plugin)
    }
}