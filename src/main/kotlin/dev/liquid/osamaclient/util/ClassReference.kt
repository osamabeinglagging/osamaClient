package dev.liquid.osamaclient.util

import dev.liquid.osamaclient.OsamaClient
import dev.liquid.osamaclient.feature.FeatureManager
import dev.liquid.osamaclient.feature.implementation.autoexperiments.AutoExperiments
import dev.liquid.osamaclient.feature.implementation.esp.ParticleESP
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

val playerController
  get() = mc.playerController

// Stuff
val featureManager = FeatureManager.getInstance()

val particleESP = ParticleESP.getInstance()
val autoExperiments = AutoExperiments.getInstance()

// Cache
val cachedWorld = osamaClient.cachedWorld

val worldScanner = osamaClient.worldScanner