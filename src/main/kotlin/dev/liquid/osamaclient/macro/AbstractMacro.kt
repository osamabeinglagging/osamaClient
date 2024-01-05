package dev.liquid.osamaclient.macro

import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.util.LogUtil

abstract class AbstractMacro : IMacro{
  override var enabled: Boolean = false
  override var forceEnable: Boolean = false
  protected var timer = Timer(0)

  override fun info(message: String) {
    LogUtil.info(this.logMessage(message))
  }

  override fun log(message: String) {
    LogUtil.log(this.logMessage(message))
  }

  override fun note(message: String) {
    LogUtil.note(this.logMessage(message))
  }

  override fun error(message: String) {
    LogUtil.error(this.logMessage(message))
  }

  private fun logMessage(message: String): String{
    return "[${this.macroName}] $message"
  }
}