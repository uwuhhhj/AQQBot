package top.alazeprt.aqqbot

import com.alessiodp.libby.BukkitLibraryManager
import com.alessiodp.libby.Library
import com.alessiodp.libby.LibraryManager
import me.clip.placeholderapi.PlaceholderAPI
import net.kyori.adventure.platform.bukkit.BukkitAudiences
import org.bstats.bukkit.Metrics
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.adapter.*
import top.alazeprt.aqqbot.command.ACommand
import top.alazeprt.aqqbot.data.DataProvider
import top.alazeprt.aqqbot.debug.ADebug
import top.alazeprt.aqqbot.event.BukkitEventHandler
import top.alazeprt.aqqbot.hook.AQQBotExpansion
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.util.ACustom
import top.alazeprt.aqqbot.util.AExecution
import top.alazeprt.aqqbot.util.AFormatter
import top.alazeprt.aqqbot.util.LogLevel
import top.alazeprt.aqqbot.task.CancelableFuture
import java.io.File
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future


class AQQBotBukkit : JavaPlugin(), AQQBot {
    override var debugModule: ADebug? = null

    override var adapter: AQQBotAdapter? = BukkitAdapter

    override val verifyCodeMap: MutableMap<String, Pair<String, Long>> = mutableMapOf()

    override val bindCooldownMap: MutableMap<String, Long> = mutableMapOf()
    override val unbindCooldownMap: MutableMap<String, Long> = mutableMapOf()

    override val unboundPlayers: MutableSet<String> = mutableSetOf()

    override lateinit var dataProvider: DataProvider

    override lateinit var enableGroups: MutableList<String>

    override lateinit var toGameFormatter: AFormatter
    override lateinit var toGroupFormatter: AFormatter

    override lateinit var libraryManager: LibraryManager

    override lateinit var customCommands: MutableList<ACustom>
    override lateinit var generalConfig: FileConfiguration
    override lateinit var messageConfig: FileConfiguration
    override lateinit var botConfig: FileConfiguration
    override lateinit var customConfig: FileConfiguration

    private val pluginId = 24071

    override var spark: Boolean = false
    override var floodgateApi: Boolean = false

    override var loadSparkCount: Int = 0
    override var loadFloodgateCount: Int = 0

    private val tasks = mutableListOf<Future<*>>()

    companion object {
        lateinit var audience: BukkitAudiences
    }

    override fun onEnable() {
        libraryManager = BukkitLibraryManager(this)
        this.enable()
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
        } catch (e: ClassNotFoundException) {
            log(LogLevel.WARN, "You don't install soft dependency PlaceholderAPI! You cannot use placeholder in anywhere!")
        }
        audience = BukkitAudiences.create(this);
        server.pluginManager.registerEvents(BukkitEventHandler(this), this)
        val metrics = Metrics(this, pluginId)
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            AQQBotExpansion(this).register()
        }
    }

    override fun onDisable() {
        cancelAll()
        this.disable()
        audience.close()
    }

    override fun setPlaceholders(player: APlayer, message: String): String {
        try {
            Class.forName("me.clip.placeholderapi.PlaceholderAPI")
            val bukkitPlayer = player as BukkitPlayer
            return PlaceholderAPI.setPlaceholders(bukkitPlayer.player, message)
        } catch (e: ClassNotFoundException) {
            return message
        }
    }

    override fun loadAdapter(): AQQBotAdapter {
        return adapter!!
    }

    override fun log(level: LogLevel, message: String) {
        when (level) {
            LogLevel.TRACE -> logger.finest(message)
            LogLevel.DEBUG -> logger.fine(message)
            LogLevel.INFO -> logger.info(message)
            LogLevel.WARN -> logger.warning(message)
            LogLevel.ERROR -> logger.severe(message)
            LogLevel.FATAL -> logger.severe(message)
        }
    }

    override fun registerCommand(command: String, handler: ACommand) {
        getCommand(command)?.setExecutor { commandSender, _, s, strings ->
            handler.onCommand(s, BukkitSender(commandSender), strings.toList())
            false
        }
        getCommand(command)?.setTabCompleter { _, _, _, strings ->
            handler.onComplete(strings.toList())
        }
    }

    override fun submit(task: Runnable): Future<*> {
        val bukkitTask = Bukkit.getScheduler().runTask(this, task)
        val future = CancelableFuture { bukkitTask.cancel() }
        tasks.add(future)
        return future
    }

    override fun submitAsync(task: Runnable): Future<*> {
        var bukkitTask: org.bukkit.scheduler.BukkitTask? = null
        val future = CancelableFuture { bukkitTask?.cancel() }
        bukkitTask = Bukkit.getScheduler().runTaskAsynchronously(this, Runnable {
            future.runningThread = Thread.currentThread()
            task.run()
        })
        tasks.add(future)
        return future
    }

    override fun submitLater(delay: Long, task: Runnable): Future<*> {
        val bukkitTask = Bukkit.getScheduler().runTaskLater(this, task, delay)
        val future = CancelableFuture { bukkitTask.cancel() }
        tasks.add(future)
        return future
    }

    override fun submitLaterAsync(delay: Long, task: Runnable): Future<*> {
        var bukkitTask: org.bukkit.scheduler.BukkitTask? = null
        val future = CancelableFuture { bukkitTask?.cancel() }
        bukkitTask = Bukkit.getScheduler().runTaskLaterAsynchronously(this, Runnable {
            future.runningThread = Thread.currentThread()
            task.run()
        }, delay)
        tasks.add(future)
        return future
    }

    override fun submitTimer(delay: Long, period: Long, task: Runnable): Future<*> {
        val bukkitTask = Bukkit.getScheduler().runTaskTimer(this, task, delay, period)
        val future = CancelableFuture { bukkitTask.cancel() }
        tasks.add(future)
        return future
    }

    override fun submitTimerAsync(delay: Long, period: Long, task: Runnable): Future<*> {
        var bukkitTask: org.bukkit.scheduler.BukkitTask? = null
        val future = CancelableFuture { bukkitTask?.cancel() }
        bukkitTask = Bukkit.getScheduler().runTaskTimerAsynchronously(this, Runnable {
            future.runningThread = Thread.currentThread()
            task.run()
        }, delay, period)
        tasks.add(future)
        return future
    }

    override fun submitCommand(command: String): CompletableFuture<AExecution> {
        val sender = BukkitConsoleSender(this)
        submit {
            sender.execute(command)
        }
        return CompletableFuture.supplyAsync {
            try {
                Thread.sleep(1000L * generalConfig.getInt("command_execution.delay"))
            } catch (e: InterruptedException) {
                // 任务被取消或线程被中断，安全退出
                return@supplyAsync sender
            }
            sender
        }
    }

    fun getAdventure(): BukkitAudiences {
        return audience
    }

    override fun loadCustomConfig() {
        val file = File(dataFolder, "custom.yml")
        if (!file.exists()) {
            saveResource("custom.yml", false)
        }
        customCommands = mutableListOf()
        customConfig = YamlConfiguration.loadConfiguration(file)
        customConfig.getKeys(false).forEach {
            if (customConfig.getBoolean("$it.enable")) {
                val command = customConfig.getStringList("$it.command")
                val execute = customConfig.getStringList("$it.execute")
                val unbind_execute = customConfig.getStringList("$it.unbind_execute")
                val output = customConfig.getStringList("$it.output")
                val unbind_output = customConfig.getStringList("$it.unbind_output")
                val format = customConfig.getBoolean("$it.format")
                val choose_account = if (customConfig.getInt("$it.chooseAccount") == 0) 1
                else customConfig.getInt("$it.chooseAccount")
                customCommands.add(ABukkitCustom(this, command, execute, unbind_execute, output, unbind_output, format, choose_account))
            }
        }
    }

    override fun loadDependencies() {
        val adventureBukkitLib = Library.builder()
            .groupId("net{}kyori")
            .artifactId("adventure-platform-bukkit")
            .version("4.3.4")
            .resolveTransitiveDependencies(true)
            .build()
        val databaseLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("taboolib-database")
            .version("1.0.4")
            .relocate("com{}google{}common", "top{}alazeprt{}aqqbot{}lib{}com{}google{}common")
            .build()
        val hikaricpLib = Library.builder()
            .groupId("com{}zaxxer")
            .artifactId("HikariCP")
            .version("4.0.3")
            .resolveTransitiveDependencies(true)
            .build()
        val guavaLib = Library.builder()
            .groupId("com{}google{}guava")
            .artifactId("guava")
            .version("21.0")
            .relocate("com{}google{}common", "top{}alazeprt{}aqqbot{}lib{}com{}google{}common")
            .resolveTransitiveDependencies(true)
            .build()
        val sqliteLib = Library.builder()
            .groupId("org{}xerial")
            .artifactId("sqlite-jdbc")
            .version("3.49.0.0")
            .resolveTransitiveDependencies(true)
            .build()
        val aconfigurationLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("AConfiguration")
            .version("1.2")
            .build()
        val mysqlLib = Library.builder()
            .groupId("com{}mysql")
            .artifactId("mysql-connector-j")
            .version("8.3.0")
            .resolveTransitiveDependencies(true)
            .build()
        val aonebotLib = Library.builder()
            .groupId("com{}github{}alazeprt")
            .artifactId("AOneBot")
            .version("1.0.11-beta")
            .relocate("com{}google{}code{}gson", "top{}alazeprt{}aonebot{}lib{}com{}google")
            .resolveTransitiveDependencies(true)
            .build()

        libraryManager.addRepository("https://maven.aliyun.com/repository/public")
        libraryManager.addMavenCentral()
        libraryManager.addJitPack()
        libraryManager.loadLibraries(adventureBukkitLib, guavaLib, hikaricpLib, sqliteLib, mysqlLib, aconfigurationLib, databaseLib, aonebotLib)
    }

    override fun cancelAll() {
        log(LogLevel.DEBUG, "Cancelling ${'$'}{tasks.size} tasks")
        tasks.forEach { it.cancel(true) }
        tasks.clear()
    }
}