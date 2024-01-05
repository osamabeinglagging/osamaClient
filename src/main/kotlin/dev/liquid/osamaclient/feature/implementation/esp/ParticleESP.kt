package dev.liquid.osamaclient.feature.implementation.esp

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.event.ParticleSpawnEvent
import dev.liquid.osamaclient.feature.AbstractFeature
import dev.liquid.osamaclient.util.RenderUtil.drawPoint
import dev.liquid.osamaclient.util.config
import net.minecraft.util.EnumParticleTypes
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*

class ParticleESP : AbstractFeature() {
  // Singleton Creator
  companion object {
    private var instance: ParticleESP? = null
    fun getInstance(): ParticleESP {
      if (instance == null) {
        instance = ParticleESP()
      }
      return instance!!
    }
  }

  override val featureName = "Particle ESP"
  override val isPassiveFeature = true
  private val particleRenderList = LinkedList<Particle>()

  override fun canEnable() = config.particleEsp && isPassiveFeature
  override fun disable() {
    if (!this.canEnable()) return
    config.particleEsp = false
  }


  @SubscribeEvent
  @Synchronized
  fun onParticleSpawn(event: ParticleSpawnEvent) {
    if (!this.canEnable()) return
    val targetParticleType = EnumParticleTypes.getParticleFromId(config.particleEspParticleID)
    if (event.particleType !== targetParticleType) return
    this.particleRenderList.push(Particle(event.particlePosition, config.particleEspParticleTime))
  }

  @SubscribeEvent
  @Synchronized
  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
    if (!this.canEnable()) return
    if (this.particleRenderList.isEmpty()) return
    val iterator = this.particleRenderList.iterator()
    while (iterator.hasNext()) {
      val particle = iterator.next()
      drawPoint(
        particle.pos,
        config.particleEspEspColor.toJavaColor(),
        config.particleEspParticleSize
      )
      particle.duration--
      if (particle.duration <= 0) {
        iterator.remove()
      }
    }
  }

  override fun onTick(event: TickEvent.ClientTickEvent) {
  }
  override fun onPacketReceive(event: PacketEvent.Received) {
  }
  override fun onChatReceive(event: ClientChatReceivedEvent) {
  }
}

data class Particle(val pos: Vec3, var duration: Int)