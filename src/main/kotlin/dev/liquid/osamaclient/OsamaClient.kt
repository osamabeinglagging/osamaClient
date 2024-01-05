package dev.liquid.osamaclient

import cc.polyfrost.oneconfig.utils.commands.CommandManager
import dev.liquid.osamaclient.command.Set
import dev.liquid.osamaclient.config.OsamaClientConfig
import dev.liquid.osamaclient.config.hud.ForagingMacroHUD
import dev.liquid.osamaclient.feature.FeatureManager
import dev.liquid.osamaclient.macro.MacroHandler
import dev.liquid.osamaclient.util.newRot
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

  lateinit var hud: ForagingMacroHUD
  lateinit var config: OsamaClientConfig private set

  @Mod.EventHandler
  private fun onInit(event: FMLInitializationEvent) {
    hud = ForagingMacroHUD()
    config = OsamaClientConfig()

    FeatureManager.getInstance().loadFeatures().forEach { feature -> MinecraftForge.EVENT_BUS.register(feature) }
    MacroHandler.getInstance().getMacros().forEach { macro -> MinecraftForge.EVENT_BUS.register(macro) }
    MinecraftForge.EVENT_BUS.register(hud)
    MinecraftForge.EVENT_BUS.register(newRot)

    val comm = Set()
    MinecraftForge.EVENT_BUS.register(comm)
    CommandManager.register(comm)
  }
}
