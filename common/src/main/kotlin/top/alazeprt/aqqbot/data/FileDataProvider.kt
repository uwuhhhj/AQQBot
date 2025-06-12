package top.alazeprt.aqqbot.data

import top.alazeprt.aconfiguration.file.FileConfiguration
import top.alazeprt.aconfiguration.file.YamlConfiguration
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import java.io.File

class FileDataProvider(val plugin: AQQBot) : DataProvider {

    private val file = File(plugin.getDataFolder(), "data.yml")
    private lateinit var dataConfig: FileConfiguration
    val dataMap: MutableMap<String, String> = mutableMapOf()

    override fun loadData(type: DataStorageType) {
        dataConfig = YamlConfiguration.loadConfiguration(file)
        dataConfig.getKeys(false).forEach {
            dataMap[it] = dataConfig.getString(it) ?: ""
        }
    }

    override fun getStorageType(): DataStorageType {
        return DataStorageType.FILE
    }

    override fun saveData(type: DataStorageType) {
        dataMap.forEach {
            dataConfig[it.key] = it.value
        }
        dataConfig.save(File(plugin.getDataFolder(), "data.yml"))
    }

    override fun hasPlayer(player: AOfflinePlayer): Boolean {
        return dataMap.containsKey(player.getUUID().toString())
    }

    override fun hasQQ(qq: Long): Boolean {
        return dataMap.values.any { it == qq.toString() }
    }

    override fun addPlayer(qq: Long, player: AOfflinePlayer) {
        dataMap[player.getUUID().toString()] = qq.toString()
    }

    override fun removePlayer(player: AOfflinePlayer) {
        dataMap.remove(player.getUUID().toString())
    }

    override fun removePlayer(qq: Long) {
        dataMap.entries.removeIf { it.value == qq.toString() }
    }

    override fun removePlayer(qq: Long, player: AOfflinePlayer) {
        if (dataMap[player.getUUID().toString()] == qq.toString()) {
            dataMap.remove(player.getUUID().toString())
        }
    }

    override fun getQQByPlayer(player: AOfflinePlayer): Long? {
        return dataMap[player.getUUID().toString()]?.toLong()
    }

    override fun getPlayerByQQ(qq: Long): List<AOfflinePlayer> {
        return dataMap.filterValues { it == qq.toString() }
            .map { plugin.adapter!!.getOfflinePlayer(java.util.UUID.fromString(it.key)) }
    }
}