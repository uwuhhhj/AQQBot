package top.alazeprt.aqqbot.event

import org.geysermc.floodgate.api.FloodgateApi
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.event.AEventUtil.playerStatusHandler
import top.alazeprt.aqqbot.event.AEventUtil.whitelistHandler
import top.alazeprt.aqqbot.profile.APlayer
import java.util.UUID

class AJoinEvent(val plugin: AQQBot, private val player: APlayer) : AEvent {
    override fun handle() {
        var handle2 = false
        val handle1 = whitelistHandler(plugin, player.getName()) { it ->
            if (plugin.floodgateApi && FloodgateApi.getInstance()?.isFloodgatePlayer(player.getUUID()) == true) {
                if (FloodgateApi.getInstance()?.getPlayer(player.getUUID())?.username.isNullOrBlank()) {
                    player.kick(it)
                    return@whitelistHandler
                }
                handle2 = whitelistHandler(plugin,
                    FloodgateApi.getInstance()?.getPlayer(player.getUUID())?.username!!) {
                    player.kick(it)
                }
            } else {
                player.kick(it)
            }
        }

        if (!handle1 || !handle2) {
            if (!plugin.generalConfig.getBoolean("whitelist.need_bind_to_login") &&
                plugin.generalConfig.getBoolean("whitelist.login_server_is_allowed_but_limit") &&
                !plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(player.getName()))) {
                plugin.unboundPlayers.add(player.getName())
            }
            if (!plugin.generalConfig.getBoolean("whitelist.need_bind_to_login") &&
                plugin.generalConfig.getBoolean("whitelist.login_server_is_allowed_but_remind") &&
                !plugin.hasPlayer(plugin.adapter!!.getOfflinePlayer(player.getName()))) {
                val verifyCode = plugin.verifyCodeMap[player.getName()]?.first ?: UUID.randomUUID().toString().substring(0, 6)
                if (!plugin.verifyCodeMap.containsKey(player.getName())) {
                    plugin.verifyCodeMap[player.getName()] = Pair(verifyCode, System.currentTimeMillis())
                }
                plugin.submitAsync {
                    while (plugin.unboundPlayers.contains(player.getName())) {
                        val p = plugin.adapter!!.getOnlinePlayer(player.getName()) ?: break
                        p.sendMessage("§c您未绑定账号，请加群发送绑定码\n§e绑定码: $verifyCode")
                        p.sendTitle("§c您未绑定账号", "§e绑定码: $verifyCode")
                        Thread.sleep(100L * 50L)
                    }
                }
            }
            playerStatusHandler(plugin, player, true)
            if (plugin.configNeedUpdate() && player.hasPermission("aqqbot.admin")) {
                plugin.submitLater(10) {
                    player.sendMessage("§a检测到你正在使用 AQQBot 的低版本配置文件, 这可能会引起一些问题")
                    player.sendMessage("§a插件已自动释放新版本配置文件并命名为 config_new.yml, 请根据你的旧版本配置文件 (config.yml) 修改该文件并重命名为 config.yml, 最后执行 /aqqbot reload 应用修改")
                }
            }
        }
    }


}