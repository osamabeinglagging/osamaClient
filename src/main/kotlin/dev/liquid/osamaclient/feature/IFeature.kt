package dev.liquid.osamaclient.feature

interface IFeature {
  fun disable()
  fun canEnable(): Boolean

  val featureName: String
  val isPassiveFeature: Boolean
}