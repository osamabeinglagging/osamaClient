package dev.liquid.osamaclient

import cc.polyfrost.oneconfig.utils.commands.CommandManager
import dev.liquid.osamaclient.command.Set
import dev.liquid.osamaclient.config.OsamaClientConfig
import dev.liquid.osamaclient.feature.FeatureManager
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.common.Mod
import net.minecraftforge.fml.common.event.FMLInitializationEvent

@Mod(modid = "osamaclient", name = "osamaClient", version = "%%VERSION%%")
class OsamaClient {

  // Singleton Getter
  companion object {
    @Mod.Instance("osamaclient")
    lateinit var INSTANCE: OsamaClient private set
  }

  lateinit var config: OsamaClientConfig private set

  @Mod.EventHandler
  private fun onInit(event: FMLInitializationEvent) {
    config = OsamaClientConfig()

    FeatureManager.getInstance().loadFeatures().forEach { feature -> MinecraftForge.EVENT_BUS.register(feature) }
    CommandManager.register(Set())
  }
}
