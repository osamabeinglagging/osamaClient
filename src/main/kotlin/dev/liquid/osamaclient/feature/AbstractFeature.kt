package dev.liquid.osamaclient.feature

import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.util.LogUtil

abstract class AbstractFeature : IFeature {
  override var forceEnable = false
  override var failed: Boolean = false
  override var succeeded: Boolean = false
  override var enabled: Boolean = false
  protected var timer = Timer(0)

  override fun canEnable() = this.forceEnable || this.enabled
  override fun setSuccessStatus(succeeded: Boolean) {
    this.failed = !succeeded
    this.succeeded = succeeded
  }

  override fun hasSucceeded() = !this.enabled && this.succeeded
  override fun hasFailed() = !this.enabled && this.failed

  fun info(message: String){
    LogUtil.info("[${this.featureName}] $message")
  }

  fun log(message: String) {
    LogUtil.log("[${this.featureName}] $message")
  }

  fun note(message: String) {
    LogUtil.note("[${this.featureName}] $message")
  }

  fun error(message: String) {
    LogUtil.error("[${this.featureName}] $message")
  }
}