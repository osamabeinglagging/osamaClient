package dev.liquid.osamaclient.feature

import dev.liquid.osamaclient.feature.implementation.esp.ParticleESP
import java.util.*
import java.util.function.Consumer

class FeatureManager {
  // Singleton Getter
  companion object {
    private var instance: FeatureManager? = null
    fun getInstance(): FeatureManager {
      if (instance == null) {
        instance = FeatureManager()
      }
      return instance!!
    }
  }

  private val features = mutableListOf<IFeature>()
  fun loadFeatures(): List<IFeature> {
    val features = listOf<IFeature>(
      ParticleESP.getInstance(),
    )
    this.features.addAll(features)
    return this.features
  }
  fun disableFeatures(disablePassiveFeatures: Boolean) {
    features.forEach(Consumer { feature: IFeature ->
      if (!feature.isPassiveFeature || feature.isPassiveFeature && disablePassiveFeatures) {
        feature.disable()
      }
    })
  }
}

