package dev.liquid.osamaclient.feature

import dev.liquid.osamaclient.event.PacketEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

interface IFeature {
  var enabled: Boolean
  val featureName: String
  val isPassiveFeature: Boolean
  var forceEnable: Boolean
  var failed: Boolean
  var succeeded: Boolean

  fun disable()
  fun canEnable(): Boolean
  fun setSuccessStatus(succeeded: Boolean = true)
  fun hasSucceeded(): Boolean
  fun hasFailed(): Boolean

  fun onTick(event: TickEvent.ClientTickEvent)
  fun onRenderWorldLastEvent(event: RenderWorldLastEvent)
  fun onPacketReceive(event: PacketEvent.Received)
  fun onChatReceive(event: ClientChatReceivedEvent)
}
