package dev.liquid.osamaclient.event;

import net.minecraft.network.play.server.S2APacketParticles;
import net.minecraft.util.EnumParticleTypes;
import lombok.Getter;
import net.minecraft.util.Vec3;
import net.minecraftforge.fml.common.eventhandler.Event;

@Getter
public class ParticleSpawnEvent extends Event {
  private final EnumParticleTypes particleType;
  private final double xCoord;
  private final double yCoord;
  private final double zCoord;
  private final float xOffset;
  private final float yOffset;
  private final float zOffset;
  private final float particleSpeed;
  private final int particleCount;
  private final boolean longDistance;
  private final int[] particleArguments;
  private final Vec3 particlePosition;

  public ParticleSpawnEvent(S2APacketParticles packet) {
    this.particleType = packet.getParticleType();
    this.xCoord = packet.getXCoordinate();
    this.yCoord = packet.getYCoordinate();
    this.zCoord = packet.getZCoordinate();
    this.xOffset = packet.getXOffset();
    this.yOffset = packet.getYOffset();
    this.zOffset = packet.getZOffset();
    this.particleSpeed = packet.getParticleSpeed();
    this.particleCount = packet.getParticleCount();
    this.longDistance = packet.isLongDistance();
    this.particleArguments = packet.getParticleArgs();
    this.particlePosition = new Vec3(this.xCoord, this.yCoord, this.zCoord);
  }
}
