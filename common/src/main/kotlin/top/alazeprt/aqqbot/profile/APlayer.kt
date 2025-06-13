package top.alazeprt.aqqbot.profile

interface APlayer: AOfflinePlayer, ASender {
    fun kick(reason: String)

    /**
     * 向玩家发送标题信息
     * @param title 主标题
     * @param subTitle 副标题
     */
    fun sendTitle(title: String, subTitle: String)
}