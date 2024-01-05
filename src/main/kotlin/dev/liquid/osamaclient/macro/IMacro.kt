package dev.liquid.osamaclient.macro

import dev.liquid.osamaclient.event.PacketEvent
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent

interface IMacro {
  val macroName: String
  var enabled: Boolean
  var forceEnable: Boolean
  fun canEnable(): Boolean
  fun toggle()
  fun enable(forceEnable: Boolean = false)
  fun disable()
  fun pause()
  fun resume()
  fun resetStates()
  fun necessaryItems(): Pair<List<String>, List<String>>

  fun info(message: String)
  fun log(message: String)
  fun note(message: String)
  fun error(message: String)

  fun onTick(event: ClientTickEvent)
  fun onRenderWorldLastEvent(event: RenderWorldLastEvent)
  fun onChatMessageReceive(event: ClientChatReceivedEvent)
  fun onPacketReceive(event: PacketEvent.Received)
}