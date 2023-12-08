package dev.liquid.osamaclient.event

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;

class PacketEvent {
  class Received(var packet: Packet<*>) : Event()
  class Sent(var packet: Packet<*>) : Event()
}
