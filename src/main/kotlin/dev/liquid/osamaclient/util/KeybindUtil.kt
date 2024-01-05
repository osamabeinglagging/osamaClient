package dev.liquid.osamaclient.util

import dev.liquid.osamaclient.mixin.MinecraftInvoker
import net.minecraft.client.Minecraft
import net.minecraft.client.settings.KeyBinding

object KeybindUtil {
  private val invokedMC = (Minecraft.getMinecraft() as MinecraftInvoker)
  fun lefClick(){
    invokedMC.leftClick()
  }

  fun rightClick(){
    invokedMC.rightClick()
  }

  fun setKeyBindState(key: KeyBinding, pressed: Boolean) {
    if (pressed) {
      if (mc.currentScreen != null) {
        realSetKeyBindState(key, false)
        return
      }
    }
    realSetKeyBindState(key, pressed)
  }

  private fun realSetKeyBindState(key: KeyBinding, pressed: Boolean) {
    if (pressed) {
      if (!key.isKeyDown) {
        KeyBinding.onTick(key.keyCode)
      }
      KeyBinding.setKeyBindState(key.keyCode, true)
    } else {
      KeyBinding.setKeyBindState(key.keyCode, false)
    }
  }

  fun toggleLeftclick(hold: Boolean = true){
    setKeyBindState(gameSettings.keyBindAttack, hold)
  }

  fun toggleRightclick(hold: Boolean = true){
    setKeyBindState(gameSettings.keyBindUseItem, hold)
  }

  fun updateMovement(forward: Boolean = false, backward: Boolean = false, left: Boolean = false, right: Boolean = false, sneak: Boolean = false){
    setKeyBindState(gameSettings.keyBindForward, forward)
    setKeyBindState(gameSettings.keyBindBack, backward)
    setKeyBindState(gameSettings.keyBindLeft, left)
    setKeyBindState(gameSettings.keyBindRight, right)
    setKeyBindState(gameSettings.keyBindSneak, sneak)
  }
}