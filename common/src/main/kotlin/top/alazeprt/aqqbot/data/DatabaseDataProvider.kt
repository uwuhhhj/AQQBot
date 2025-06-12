package top.alazeprt.aqqbot.data;

import taboolib.module.database.Host
import taboolib.module.database.Table
import top.alazeprt.aqqbot.AQQBot;
import top.alazeprt.aqqbot.profile.AOfflinePlayer
import javax.sql.DataSource

abstract class DatabaseDataProvider(val plugin: AQQBot): DataProvider {

    abstract var host: Host<*>

    abstract var table: Table<*, *>

    abstract var dataSource: DataSource

    override fun hasPlayer(player: AOfflinePlayer): Boolean {
        return table.select(dataSource) {
            rows("qq")
            where("uuid" eq player.getUUID().toString())
            limit(1)
        }.firstOrNull { getLong("qq") } != null
    }

    override fun hasQQ(qq: Long): Boolean {
        return table.select(dataSource) {
            rows("uuid")
            where("qq" eq qq)
            limit(1)
        }.firstOrNull { getString("uuid") } != null
    }

    override fun addPlayer(qq: Long, player: AOfflinePlayer) {
        if (hasPlayer(player)) {
            table.update(dataSource) {
                where("uuid" eq player.getUUID().toString())
                set("qq", qq)
            }
        } else {
            table.insert(dataSource, "uuid", "qq") {
                value(player.getUUID().toString(), qq)
            }
        }
    }

    override fun removePlayer(qq: Long) {
        table.delete(dataSource) {
            where("qq" eq qq)
        }
    }

    override fun removePlayer(player: AOfflinePlayer) {
        table.delete(dataSource) {
            where("uuid" eq player.getUUID().toString())
        }
    }

    override fun removePlayer(qq: Long, player: AOfflinePlayer) {
        if (getQQByPlayer(player) == qq) {
            removePlayer(player)
        }
    }

    override fun getQQByPlayer(player: AOfflinePlayer): Long? {
        return table.select(dataSource) {
            rows("qq")
            where("uuid" eq player.getUUID().toString())
            limit(1)
        }.firstOrNull { getLong("qq") }
    }

    override fun getPlayerByQQ(qq: Long): List<AOfflinePlayer> {
        val list = mutableListOf<AOfflinePlayer>()
        table.select(dataSource) {
            rows("uuid")
            where("qq" eq qq)
        }.map {
            list.add(plugin.adapter!!.getOfflinePlayer(java.util.UUID.fromString(getString("uuid"))))
        }
        return list
    }

}
