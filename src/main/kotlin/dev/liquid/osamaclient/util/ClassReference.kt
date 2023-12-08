package dev.liquid.osamaclient.util

import dev.liquid.osamaclient.OsamaClient
import dev.liquid.osamaclient.feature.FeatureManager
import net.minecraft.client.Minecraft


val osamaClient
  get() = OsamaClient.INSTANCE

val config
  get() = osamaClient.config

val mc
  get() = Minecraft.getMinecraft()

val player
  get() = mc.thePlayer

val world
  get() = mc.theWorld


// Stuff
val featureManager = FeatureManager.getInstance()