package dev.liquid.osamaclient.feature.implementation.autoexperiments

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.feature.AbstractFeature
import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.util.*
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.network.play.server.S2FPacketSetSlot
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.GuiOpenEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent
import java.util.LinkedList
import java.util.Queue
import java.util.TreeMap

class AutoExperiments : AbstractFeature() {
  // Singleton Creator
  companion object {
    private var instance: AutoExperiments? = null
    fun getInstance(): AutoExperiments {
      if (instance == null) {
        instance = AutoExperiments()
      }
      return instance!!
    }
  }

  override val featureName = "Auto Experiments"
  override val isPassiveFeature = false

  private var isSolving = false
  private var shouldEnd = false
  private var lastItemName = ""
  private var clickDelayTimer = Timer(0)
  private var sequencerClickQueue: Queue<Int> = LinkedList()
  private var chronomatronClickQueue: Queue<Int> = LinkedList()

  override fun disable() {
    if (this.currentExperiment == Experiment.NONE) return

    this.isSolving = false
    this.shouldEnd = false
    this.lastItemName = ""
    this.sequencerClickQueue.clear()
    this.chronomatronClickQueue.clear()
    this.currentExperiment = Experiment.NONE
  }

  override fun canEnable() = config.autoExperiment

  enum class Experiment {
    CHRONOMATRON,
    SEQUENCER,
    NONE
  }

  private var currentExperiment = Experiment.NONE

  @SubscribeEvent
  fun onGuiOpen(event: GuiOpenEvent) {
    if (!this.canEnable()) return
    if (event.gui !is GuiChest) return

    this.disable() // More like reset

    val guiName = InventoryUtil.getOpenContainerDisplayName((event.gui as GuiChest).inventorySlots)
    when {
      guiName.contains("Chronomatron (", ignoreCase = true) -> {
        log("Starting Chronomatron")
        this.currentExperiment = Experiment.CHRONOMATRON
      }

      guiName.contains("Ultrasequencer (", ignoreCase = true) -> {
        log("Starting Ultrasequencer")
        this.currentExperiment = Experiment.SEQUENCER
      }
    }
  }

  @SubscribeEvent
  override fun onPacketReceive(event: PacketEvent.Received) {
    if (this.currentExperiment == Experiment.NONE || !this.canEnable()) return
    if (event.packet !is S2FPacketSetSlot) return
    val slot = (event.packet as S2FPacketSetSlot)

    val slotIndex = slot.func_149173_d()
    val slotStack = slot.func_149174_e()

    if (slotStack == null || slotStack.displayName == null) return
    if (slotIndex > player.openContainer.inventorySlots.size - 37) return

    val slotName = StringUtils.stripControlCodes(slotStack.displayName)

    if (slotName.contains("Remember", ignoreCase = true) && this.isSolving) {
      log("Started")
      this.lastItemName = ""
      this.isSolving = false
      this.chronomatronClickQueue.clear()
      this.sequencerClickQueue.clear()
      if (this.shouldEnd) {
        InventoryUtil.closeOpenGUI()
      }
    }

    if (slotName.contains("Timer", ignoreCase = true) && !this.isSolving) {
      log("Ended")
      log("Slots: ${if (currentExperiment == Experiment.CHRONOMATRON) chronomatronClickQueue else sequencerClickQueue}")

      this.isSolving = true
      this.lastItemName = ""
      this.clickDelayTimer = Timer(config.autoExperimentClickDelay)
      this.shouldEnd =
        (this.chronomatronClickQueue.size >= config.autoExperimentChronomatronMax || this.sequencerClickQueue.size >= config.autoExperimentSequencerMax)
    }

    if (this.currentExperiment != Experiment.CHRONOMATRON) return

    if (slotName == this.lastItemName && !slotStack.isItemEnchanted) {
      this.lastItemName = ""
      return
    }

    if (this.lastItemName.isEmpty() && slotStack.isItemEnchanted) {
      this.lastItemName = slotName
      this.chronomatronClickQueue.add(slotIndex)
      log("Name: $slotName")
    }
  }


  @SubscribeEvent
  override fun onTick(event: ClientTickEvent) {
    if (this.currentExperiment != Experiment.SEQUENCER || !this.canEnable()) return
    if (player == null || world == null) return
    if (this.isSolving) return
    if (!this.sequencerClickQueue.isEmpty()) return
    if (mc.currentScreen !is GuiChest) return

    val indexArray = TreeMap<Int, Int>()
    for (i in 0..player.openContainer.inventorySlots.size - 37) {
      val slotObj = player.openContainer.inventorySlots[i]
      if (!slotObj.hasStack || slotObj.stack.displayName == null) return
      val index = StringUtils.stripControlCodes(slotObj.stack.displayName).toIntOrNull()
      if (index != null) {
        indexArray[index] = slotObj.slotNumber
      }
    }
    this.sequencerClickQueue.addAll(indexArray.values)
  }


  @SubscribeEvent
  fun onTickReplay(event: ClientTickEvent) {
    if (this.currentExperiment == Experiment.NONE || !this.canEnable()) return
    if (player == null || world == null || mc.currentScreen == null) return
    if (!this.isSolving) return
    if (!this.clickDelayTimer.hasEnded()) return
    if (this.chronomatronClickQueue.isEmpty() && this.sequencerClickQueue.isEmpty()) return

    val polledSlot =
      if (this.currentExperiment == Experiment.CHRONOMATRON) this.chronomatronClickQueue.poll()
      else this.sequencerClickQueue.poll()
    InventoryUtil.clickSlot(polledSlot)
    this.clickDelayTimer = Timer(config.autoExperimentClickDelay)
  }

  override fun onChatReceive(event: ClientChatReceivedEvent) {
  }

  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
  }
}
