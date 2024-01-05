package dev.liquid.osamaclient.util

import dev.liquid.osamaclient.feature.implementation.general.AutoBazaar

object ConfigUtil {

  fun getForagingMacroSaplingType(): String {
    val saplings = arrayOf("Jungle Sapling", "Dark Oak Sapling", "Spruce Sapling", "Acacia Sapling")
    return saplings[config.foragingMacroSaplingType]
  }

  fun getAutoSellType(): IntArray {
    return if (config.featureAutoSellType) {
      intArrayOf(AutoBazaar.SELL_INVENTORY)
    } else {
      intArrayOf(AutoBazaar.SELL_INVENTORY, AutoBazaar.SELL_SACK)
    }
  }
}