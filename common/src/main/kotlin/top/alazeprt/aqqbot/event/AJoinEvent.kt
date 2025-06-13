package top.alazeprt.aqqbot.event

import org.geysermc.floodgate.api.FloodgateApi
import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.event.AEventUtil.playerStatusHandler
import top.alazeprt.aqqbot.event.AEventUtil.whitelistHandler
import top.alazeprt.aqqbot.profile.APlayer
import top.alazeprt.aqqbot.util.AFormatter
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

        if (plugin.generalConfig.getBoolean("whitelist.enable") && 
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
                    val messageParams = mapOf("code" to verifyCode)
                    p.sendMessage(AFormatter.pluginToChat(plugin.getMessageManager().get("game.not_bind_reminder.message", messageParams)))
                    p.sendTitle(
                        AFormatter.pluginToChat(plugin.getMessageManager().get("game.not_bind_reminder.title", messageParams)),
                        AFormatter.pluginToChat(plugin.getMessageManager().get("game.not_bind_reminder.subtitle", messageParams))
                    )
                    Thread.sleep(100L * 50L)
                }
            }
        }

        playerStatusHandler(plugin, player, true)
        if (plugin.configNeedUpdate() && player.hasPermission("aqqbot.admin")) {
            plugin.submitLater(10) {
                player.sendMessage(AFormatter.pluginToChat(plugin.getMessageManager().get("game.config_update.outdated")))
                player.sendMessage(AFormatter.pluginToChat(plugin.getMessageManager().get("game.config_update.instruction")))
            }
        }
    }
}