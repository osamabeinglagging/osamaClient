package dev.liquid.osamaclient.macro

import dev.liquid.osamaclient.macro.macros.ForagingMacro
import dev.liquid.osamaclient.util.UnGrabUtil
import dev.liquid.osamaclient.util.config

class MacroHandler {
  companion object {
    private var instance: MacroHandler? = null
    fun getInstance(): MacroHandler {
      if (instance == null) {
        instance = MacroHandler()
      }
      return instance!!
    }
  }

  private var macros = listOf<IMacro>()
  private val activeMacro: IMacro get() = this.findActiveMacro()

  fun toggle() {
    if (this.activeMacro.enabled) {
      this.disable()
    } else {
      this.enable()
    }
  }

  fun enable() {
    UnGrabUtil.unGrabMouse()
    this.activeMacro.enable()
  }

  fun disable() {
    UnGrabUtil.grabMouse()
    this.activeMacro.disable()
  }

  fun pause() {
    this.activeMacro.pause()
  }

  fun resume() {
    this.activeMacro.resume()
  }

  fun necessaryItems(): Pair<List<String>, List<String>> {
    return this.activeMacro.necessaryItems()
  }

  fun getMacros(): List<IMacro> {
    val macros = listOf(
      ForagingMacro.getInstance()
    )
    this.macros = macros
    return this.macros
  }

  private fun findActiveMacro(): IMacro {
    return when (config.activeMacro) {
      0 -> ForagingMacro.getInstance()
      else -> ForagingMacro.getInstance()
    }
  }
}