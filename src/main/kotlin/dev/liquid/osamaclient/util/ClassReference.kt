package dev.liquid.osamaclient.util

import dev.liquid.osamaclient.OsamaClient
import dev.liquid.osamaclient.feature.FeatureManager
import dev.liquid.osamaclient.feature.implementation.autoexperiments.AutoExperiments
import dev.liquid.osamaclient.feature.implementation.esp.ParticleESP
import dev.liquid.osamaclient.feature.implementation.foragingmacro.AutoChestRefill
import dev.liquid.osamaclient.feature.implementation.general.*
import dev.liquid.osamaclient.macro.MacroHandler
import dev.liquid.osamaclient.macro.macros.ForagingMacro
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

val gameSettings
  get() = mc.gameSettings

// Stuff
val featureManager = FeatureManager.getInstance()
val macroHandler = MacroHandler.getInstance()

val particleESP = ParticleESP.getInstance()
val autoExperiments = AutoExperiments.getInstance()

// General
val autoRotation = AutoRotation.getInstance()
val autoInventory = AutoInventory.getInstance()
val autoAbiphone = AutoAbiphone.getInstance()
val autoBazaar = AutoBazaar.getInstance()
val newRot = Rotation()
val autoChestRefill get() = AutoChestRefill.getInstance()

// Macros
val foragingMacro = ForagingMacro.getInstance()