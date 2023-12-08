package dev.liquid.osamaclient.util

import net.minecraft.util.ChatComponentText

object LogUtil {
  private var lastDebugMessage = ""
  fun info(message: Any) {
    send("a§l$message")
  }

  fun note(message: Any) {
    send("e$message")
  }

  fun error(message: Any) {
    send("c$message")
  }

  fun log(message: Any) {
    if (!config.osamaClientDebugMode || message == lastDebugMessage) return
    lastDebugMessage = message.toString()
    send("7$message")
  }

  private fun send(message: String) {
    player.addChatMessage(ChatComponentText("§4[§6OC§4] §8» §$message"))
  }
}

