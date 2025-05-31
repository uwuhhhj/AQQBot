# AQQBot

[ENGLISH](https://github.com/alazeprt/AQQBot/blob/master/README.md) | 简体中文

---

## 📝 介绍

**AQQBot** 是一个基于 OneBot v11 协议开发的 QQ 群与服务器互联插件，允许用户通过 QQ 群执行多种操作（如绑定游戏账号、查询服务器状态等）。

---

## 🚀 功能一览

- **账户绑定**：玩家需在 QQ 群中绑定账号后才能进入服务器。
- **信息查询**：在 QQ 群发送特定命令可查询账号游戏状态（如步行距离）及服务器状态（如 TPS）。
- **群服互联**：QQ群与服务器消息互通，双向转发。

---

## 🌟 插件特点

- **轻量级**：插件本体不足 600KB。
- **高度定制**：可通过配置文件自由开关各项功能。
- **强大兼容性**：支持所有基于 Spigot/Paper 的服务端及 Velocity。

---

## ⚙️ 安装指南

1. 安装支持 OneBot v11 协议的后端（如 Lagrange.OneBot、LLOneBot）。
2. 启用后端的正向 WS（WebSocket）转发功能，并记下端口号。
3. 安装插件，启动服务器后编辑 `bot.yml` 文件：

    ```yaml
    # 正向 WebSocket 配置
    ws:
      # 主机地址
      host: "localhost"

      # 端口
      port: 3001

    # 在机器人后端配置的 access_token
    # 若未配置请留空
    access_token: ""

    # 定时检查 WebSocket 连接的间隔时长（单位：秒）
    # ! 若配置为 <0 的数值则不启动定时检查
    check_interval: 60

    # 启用插件的群号
    groups:
      - "114514"
    ```

    - `ws.host`：主机地址，若后端与服务器同机无需修改。
    - `ws.port`：端口号，填写记下的端口。
    - `access_token`：访问令牌，按需填写。
    - `check_interval`：定时检查间隔，按需调整。
    - `groups`：填写需启用机器人的 QQ 群号。

4. 重启服务器，无报错即连接成功。

---

## 📄 协议

本插件基于 **LGPL-2.1** 协议开源，请遵守相关规定。最终解释权归 *alazeprt* 所有。

---
