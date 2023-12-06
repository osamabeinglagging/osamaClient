package dev.liquid.osamaclient.util;

import dev.liquid.osamaclient.OsamaClient;
import dev.liquid.osamaclient.feature.FeatureManager;
import dev.liquid.osamaclient.feature.impl.esp.ParticleESP;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;

public class ClassReference {

  public static final OsamaClient osamaClient = OsamaClient.getInstance();
  public static final Minecraft mc = Minecraft.getMinecraft();
  public static final EntityPlayerSP player = mc.thePlayer;

  // Features
  public static final FeatureManager featureManager = FeatureManager.getInstance();

  public static final ParticleESP particleESP = ParticleESP.getInstance();
}
