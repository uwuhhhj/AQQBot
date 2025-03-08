package top.alazeprt.aqqbot.event

import top.alazeprt.aqqbot.AQQBot
import top.alazeprt.aqqbot.event.AEventUtil.playerStatusHandler
import top.alazeprt.aqqbot.event.AEventUtil.whitelistHandler
import top.alazeprt.aqqbot.profile.APlayer

class AJoinEvent(val plugin: AQQBot, private val player: APlayer) : AEvent {
    override fun handle() {
        if (plugin.configNeedUpdate() && player.hasPermission("aqqbot.admin")) {
            player.sendMessage("§a检测到你正在使用 AQQBot 的低版本配置文件, 这可能会引起一些问题")
            player.sendMessage("§a插件已自动释放新版本配置文件并命名为 config_new.yml, 请根据你的旧版本配置文件 (config.yml) 修改该文件并重命名为 config.yml, 最后执行 /aqqbot reload 应用修改")
        }
        var handle2 = false
        val handle1 = whitelistHandler(plugin, player.getName()) { it ->
            if (plugin.floodgateApi?.isFloodgatePlayer(player.getUUID()) == true) {
                if (plugin.floodgateApi?.getPlayer(player.getUUID())?.correctUsername.isNullOrBlank()) {
                    player.kick(it)
                    return@whitelistHandler
                }
                handle2 = whitelistHandler(plugin,
                    plugin.floodgateApi?.getPlayer(player.getUUID())?.correctUsername!!) {
                    player.kick(it)
                }
            } else {
                player.kick(it)
            }
        }

        if (handle1 || handle2) playerStatusHandler(plugin, player.getName(), true)
    }


}