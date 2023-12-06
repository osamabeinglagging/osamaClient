package dev.liquid.osamaclient.feature.impl.esp;

import dev.liquid.osamaclient.config.OsamaClientConfig;
import dev.liquid.osamaclient.event.ParticleSpawnEvent;
import dev.liquid.osamaclient.feature.IFeature;
import dev.liquid.osamaclient.util.RenderUtil;
import lombok.Getter;
import net.minecraft.util.EnumParticleTypes;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.awt.*;
import java.util.Iterator;
import java.util.LinkedList;

public class ParticleESP implements IFeature {
  public static ParticleESP instance;
  private final LinkedList<Particle> particleRenderList = new LinkedList<>();

  public static ParticleESP getInstance() {
    if (instance == null) {
      instance = new ParticleESP();
    }
    return instance;
  }

  @Override
  public String getFeatureName() {
    return "Particle Esp";
  }

  @Override
  public boolean canEnable() {
    return OsamaClientConfig.particleEsp && this.isPassiveFeature();
  }

  @Override
  public void disable() {
    if (!this.canEnable()) return;
    OsamaClientConfig.particleEsp = false;
  }

  @Override
  public boolean isPassiveFeature() {
    return true;
  }

  @SubscribeEvent
  public synchronized void onParticleSpawn(ParticleSpawnEvent event) {
    if (!this.canEnable()) return;

    EnumParticleTypes targetParticleType = EnumParticleTypes.getParticleFromId(OsamaClientConfig.particleEspParticleID);
    if (event.getParticleType() != targetParticleType) return;
    this.particleRenderList.push(new Particle(event.getParticlePosition(), OsamaClientConfig.particleEspParticleTime));
  }

  @SubscribeEvent
  public synchronized void onRenderWorldLast(RenderWorldLastEvent event) {
    if (!this.canEnable()) return;
    if (this.particleRenderList.isEmpty()) return;

    Iterator<Particle> iterator = particleRenderList.iterator();
    while (iterator.hasNext()) {
      Particle particle = iterator.next();
      RenderUtil.drawPoint(particle.getPos(),
          OsamaClientConfig.particleEspEspColor.toJavaColor(),
          OsamaClientConfig.particleEspParticleSize);
      particle.duration--;
      if (particle.duration <= 0) {
        iterator.remove();
      }
    }
  }
}

@Getter
class Particle {
  private final Vec3 pos;
  int duration;

  public Particle(Vec3 pos, int duration) {
    this.pos = pos;
    this.duration = duration;
  }
}