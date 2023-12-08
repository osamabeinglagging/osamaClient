package dev.liquid.osamaclient.event

import net.minecraft.network.play.server.S2APacketParticles
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.fml.common.eventhandler.Event

class ParticleSpawnEvent(packet: S2APacketParticles) : Event() {
  private val x: Double = packet.xCoordinate
  private val y: Double = packet.yCoordinate
  private val z: Double = packet.zCoordinate

  val particleType: EnumParticleTypes = packet.particleType
  val xOffset: Float = packet.xOffset
  val yOffset: Float = packet.yOffset
  val zOffset: Float = packet.zOffset
  val particleSpeed: Float = packet.particleSpeed
  val particleCount: Int = packet.particleCount
  val longDistance: Boolean = packet.isLongDistance
  val particleArguments: IntArray = packet.particleArgs
  val particlePosition: Vec3 = Vec3(x, y, z)
}
