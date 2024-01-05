package dev.liquid.osamaclient.feature.implementation.general

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.feature.AbstractFeature
import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.util.*
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent

class AutoAbiphone : AbstractFeature() {
  companion object {
    private var instance: AutoAbiphone? = null
    fun getInstance(): AutoAbiphone {
      if (instance == null) {
        instance = AutoAbiphone()
      }
      return instance!!
    }
  }

  private val ABIPHONE = "Abiphone";

  override val featureName: String = "AutoAbiphone"
  override val isPassiveFeature: Boolean = false

  private var contactName: String = ""
  private var state = State.STARTING

  private var clickableSlot = -1
  private var lastPageLore = ""

  fun call(contactName: String, forceEnable: Boolean = false) {
    if (this.enabled) return

    this.enabled = true
    this.forceEnable = forceEnable
    this.state = State.STARTING
    this.contactName = contactName
    this.clickableSlot = -1
    this.lastPageLore = ""

    info("Enabling")
  }

  override fun disable() {
    if (!this.enabled) return

    this.enabled = false
    this.contactName = ""
    this.lastPageLore = ""
    this.clickableSlot = -1
    this.forceEnable = false
    this.state = State.STARTING
    InventoryUtil.closeOpenGUI()

    log("Disabling")
  }

  private fun stop(message: String) {
    log(message)
    this.setSuccessStatus(false)
    this.disable()
  }

  private fun changeState(state: State, time: Int = 0) {
    log("Old: ${this.state}, new: $state, time: $time")
    this.state = state
    this.timer = Timer(time)
  }

  @SubscribeEvent
  override fun onTick(event: TickEvent.ClientTickEvent) {
    if (player == null || world == null || !this.canEnable()) return

    when (this.state) {
      State.STARTING -> {
        if (!InventoryUtil.setHotbarSlotForItem(ABIPHONE) || !InventoryUtil.setHotbarSlotForItem("iphone")) {
          this.stop("Cannot hold Abiphone")
          return
        }

        this.changeState(State.USE_ABIPHONE, 300)
      }

      State.USE_ABIPHONE -> {
        if (!this.timer.hasEnded()) return
        KeybindUtil.rightClick()
        this.changeState(State.ABIPHONE_GUI_VERIFY, 2000)
      }

      State.ABIPHONE_GUI_VERIFY -> {
        if (mc.currentScreen is GuiChest && InventoryUtil.getOpenContainerDisplayName()
            .contains("iphone")
        ) {
          log("Opened Abiphone GUI.")
          this.changeState(State.FIND_CONTACT, 250)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Could not open Abiphone GUI in time.")
        }
      }

      State.FIND_CONTACT -> {
        if (!this.timer.hasEnded()) return

        val guiSize = mc.thePlayer.openContainer.inventorySlots.size - 37
        this.clickableSlot = InventoryUtil.getContainerSlotForItem(this.contactName)

        if (clickableSlot != -1 && clickableSlot < guiSize) {
          log("Found Contact.")
          this.changeState(State.CLICK_CONTACT, 300)
          return
        }

        this.clickableSlot = InventoryUtil.getContainerSlotForItem("Next Page")

        if (clickableSlot != -1 && clickableSlot < guiSize) {
          this.changeState(State.CLICK_ON_NEXT_PAGE, 300)
          log("Could not find Contact on this page. Clicking on next page")
          return
        }

        this.stop("Cannot find neither ${this.contactName} nor Next Page Button.")
      }

      State.CLICK_CONTACT -> {
        if (!this.timer.hasEnded()) return

        InventoryUtil.clickSlot(this.clickableSlot)
        this.changeState(State.VERIFY_CALL, 2000)
      }

      State.CLICK_ON_NEXT_PAGE -> {
        if (!this.timer.hasEnded()) return
        this.lastPageLore = InventoryUtil.getLore(this.clickableSlot)

        InventoryUtil.clickSlot(this.clickableSlot)
        this.changeState(State.NEXT_PAGE_VERIFY, 2000)
      }

      State.NEXT_PAGE_VERIFY -> {
        val currentPageLore = InventoryUtil.getLore(this.clickableSlot)
        if (this.lastPageLore != currentPageLore) {
          log("Page Changed")
          this.changeState(State.FIND_CONTACT, 250)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Could not swap pages.")
        }
      }

      State.VERIFY_CALL -> {
        if (!this.timer.hasEnded()) return
        this.stop("Could not verify call")
      }
    }
  }

  @SubscribeEvent
  override fun onChatReceive(event: ClientChatReceivedEvent) {
    if (event.type.toInt() != 0) return

    val message = StringUtils.stripControlCodes(event.message.unformattedText).lowercase()
    if (message.contains("ring.") && this.state == State.VERIFY_CALL) {
      log("Call Successful")
      this.setSuccessStatus(true)
      this.disable()
    }
  }

  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
    TODO("Not yet implemented")
  }

  override fun onPacketReceive(event: PacketEvent.Received) {
    TODO("Not yet implemented")
  }


  enum class State {
    STARTING,
    USE_ABIPHONE,
    ABIPHONE_GUI_VERIFY,
    FIND_CONTACT,
    CLICK_CONTACT,
    CLICK_ON_NEXT_PAGE,
    NEXT_PAGE_VERIFY,
    VERIFY_CALL
  }
}