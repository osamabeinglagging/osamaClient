package dev.liquid.osamaclient;

import dev.liquid.osamaclient.config.OsamaClientConfig;
import net.minecraft.client.Minecraft;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;

import static dev.liquid.osamaclient.util.ClassReference.featureManager;

@Mod(modid = OsamaClient.MODID, name = OsamaClient.NAME, version = OsamaClient.VERSION)
public class OsamaClient {
  public static final String MODID = "osamaclient";
  public static final String NAME = "osamaClient";
  public static final String VERSION = "%%VERSION%%";
  public static OsamaClientConfig config;
  @Mod.Instance(MODID)
  private static OsamaClient instance;
  public static OsamaClient getInstance() {
    if(instance == null){
      instance = new OsamaClient();
    }
    return instance;
  }
  @Mod.EventHandler
  private void onInit(FMLInitializationEvent event) {
    config = new OsamaClientConfig();

    featureManager.loadFeatures().forEach(MinecraftForge.EVENT_BUS::register);
  }
}
