package dev.liquid.osamaclient.mixin;

import dev.liquid.osamaclient.event.ParticleSpawnEvent;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
  @Inject(method = "handleParticles", at = @At("HEAD"))
  public void handleParticles(S2APacketParticles packetIn, CallbackInfo ci){
    MinecraftForge.EVENT_BUS.post(new ParticleSpawnEvent(packetIn));
  }
}
