package dev.liquid.osamaclient.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Event;


public class PacketEvent {
  public static class Received extends Event {
    public Packet<?> packet;

    public Received(Packet<?> packet) {
      this.packet = packet;
    }
  }

  public static class Sent extends Event {
    public Packet<?> packet;

    public Sent(Packet<?> packet) {
      this.packet = packet;
    }
  }
}
