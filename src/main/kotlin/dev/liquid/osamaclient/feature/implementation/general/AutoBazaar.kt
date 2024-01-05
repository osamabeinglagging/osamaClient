package dev.liquid.osamaclient.feature.implementation.general

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.feature.AbstractFeature
import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.util.InventoryUtil
import dev.liquid.osamaclient.util.helper.SignUtil
import dev.liquid.osamaclient.util.mc
import dev.liquid.osamaclient.util.player
import dev.liquid.osamaclient.util.world
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiEditSign
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.*
import java.util.regex.Pattern

class AutoBazaar : AbstractFeature() {
  companion object {
    private var instance: AutoBazaar? = null
    fun getInstance(): AutoBazaar {
      if (instance == null) {
        instance = AutoBazaar()
      }
      return instance!!
    }

    const val SELL_INVENTORY = 0
    const val SELL_SACK = 1
  }

  override val featureName: String = "AutoBazaar"
  override val isPassiveFeature: Boolean = false

  private val instabuyAmountPattern = Pattern.compile("amount:\\s(\\d+)x")
  private val totalCostPattern = Pattern.compile("price:\\s(\\d+(.\\d+)?)\\scoins")
  private val instaSellBtn = arrayOf("Sell inventory now", "Sell sacks now")

  private var mainState = MainState.BUY_FROM_BZ
  private var buyState = BuyState.STARTING

  private var clickedButton = ""

  // Buy
  private var itemToBuy = ""
  private var buyAmount = -1
  private var spendLimit = -1
  private var buySlot = -1

  // Sell
  private var sellState = SellState.STARTING
  private var instasellClickQueue: Queue<Int> = LinkedList()

  fun buy(itemName: String, amount: Int, spendLimit: Int = 0, forceEnable: Boolean = false) {
    if (this.enabled) return

    this.enabled = true
    this.forceEnable = forceEnable
    this.itemToBuy = itemName
    this.buyAmount = amount
    this.spendLimit = spendLimit
    this.clickedButton = ""
    this.buySlot = -1
    this.failed = false
    this.succeeded = false
    this.resetStates()

    note("Enabling")
  }

  fun sell(vararg sellTo: Int, forceEnable: Boolean = false) {
    if (this.enabled) return

    sellTo.forEach {
      if (it < 2) this.instasellClickQueue.add(it)
    }
    if (this.instasellClickQueue.isEmpty()) {
      error("No slots to click to instasell. Report this to dev")
      return
    }

    this.enabled = true
    this.forceEnable = forceEnable
    this.sellState = SellState.STARTING
    this.mainState = MainState.SELL_TO_BZ
    this.failed = false
    this.succeeded = false

    note("Instantly Selling To Bazaar")
  }

  override fun disable() {
    if (!this.enabled) return

    this.enabled = false
    this.forceEnable = false
    this.itemToBuy = ""
    this.buyAmount = -1
    this.spendLimit = -1
    this.clickedButton = ""
    this.buySlot = -1
    this.resetStates()
    InventoryUtil.closeOpenGUI()

    note("Disabling")
  }

  private fun getButtonToClickOn(inventoryName: String): String? {
    return when {
      inventoryName.contains("Bazaar ➜ \"") -> this.itemToBuy
      inventoryName.contains("➜ ${this.itemToBuy}") -> "Buy Instantly"
      inventoryName.contains("${this.itemToBuy} ➜ Instant Buy") || inventoryName.contains("Confirm Instant Buy") -> "Custom Amount"
      else -> null
    }
  }

  private fun resetStates() {
    this.mainState = MainState.BUY_FROM_BZ
    this.buyState = BuyState.STARTING
    this.sellState = SellState.STARTING
  }

  @SubscribeEvent
  override fun onTick(event: TickEvent.ClientTickEvent) {
    if (player == null || world == null || !this.canEnable()) return

    when (this.mainState) {
      MainState.BUY_FROM_BZ -> this.handleBuyFromBZ()
      MainState.SELL_TO_BZ -> this.handleSellToBZ()
    }
  }


  private fun stop(message: String, succeeded: Boolean = false) {
    error(message)
    this.setSuccessStatus(succeeded)
    this.disable()
  }

  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
  }

  override fun onPacketReceive(event: PacketEvent.Received) {
  }

  @SubscribeEvent
  override fun onChatReceive(event: ClientChatReceivedEvent) {
    if (event.message == null) return

    val message = StringUtils.stripControlCodes(event.message.unformattedText).replace(",", "")
    val boughtMessage = "[Bazaar] Bought ${buyAmount}x ${this.itemToBuy} for"
    val sellMessage = "[Bazaar] Executing instant sell..."

    if (message.startsWith(boughtMessage) && this.buyState == BuyState.CONFIRM_PURCHASE) {
      this.stop("Bought items successfully.", true)
    }

    if (message.startsWith(sellMessage) && this.sellState == SellState.SELL_VERIFY) {
      log("Sold items successfully")
      this.changeState(SellState.VERIFY_END, 500)
    }
  }

  // HandleBuy
  private fun changeState(buyState: BuyState, time: Int = 0) {
    log("Changing from ${this.buyState} to $buyState, time: $time")
    this.buyState = buyState
    this.timer = Timer(time)
  }

  private fun handleBuyFromBZ() {
    when (this.buyState) {
      BuyState.STARTING -> {
        if (!this.timer.hasEnded()) return

        if (this.itemToBuy.isEmpty()) {
          this.stop("No item specified to buy.")
          return
        }

        player.sendChatMessage("/bz ${this.itemToBuy}")
        this.changeState(BuyState.BZ_VERIFY, 2000)
      }

      BuyState.BZ_VERIFY -> {
        if (mc.currentScreen != null && InventoryUtil.getOpenContainerDisplayName().startsWith("Bazaar ➜ \"")) {
          log("Opened BZ")
          this.changeState(BuyState.NAVIGATE_PAGES, 500)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Failed to open bz. Retrying")
        }
      }

      BuyState.NAVIGATE_PAGES -> {
        if (!this.timer.hasEnded()) return

        val buttonToClick = this.getButtonToClickOn(InventoryUtil.getOpenContainerDisplayName())
        if (buttonToClick == null) {
          this.stop("Could not find button to click. Retrying")
          return
        }

        val buttonSlot = InventoryUtil.getContainerSlotForItem(buttonToClick, true)

        if (buttonSlot == -1) {
          this.stop("Could not find $buttonToClick slot. Retrying")
          return
        }

        this.clickedButton = buttonToClick
        InventoryUtil.clickSlot(buttonSlot)
        this.changeState(BuyState.NAVIGATE_VERIFY, 2000)
      }

      BuyState.NAVIGATE_VERIFY -> {
        val inventoryName = InventoryUtil.getOpenContainerDisplayName()
        val buyPage = "${this.itemToBuy} ➜ Instant Buy"
        log("Inventory Name: " + inventoryName)
        if (buyPage.contains(inventoryName)) {
          log("Buy Page")
          this.changeState(BuyState.FIND_BUY_SLOT, 2000)
          return
        }

        if (this.getButtonToClickOn(inventoryName) != this.clickedButton) {
          log("Changed Page")
          this.changeState(BuyState.NAVIGATE_PAGES, 500)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Could not navigate page. Retrying")
        }
      }

      BuyState.FIND_BUY_SLOT -> {
        val buySlots = InventoryUtil.getAllContainerSlotsForItem("Buy", "Fill my inventory!")
        log("BuySlotNumbers: " + buySlots.size)
        for (slotNumber in buySlots) {
          val lore = InventoryUtil.getLore(slotNumber).replace(",", "")
          val matcher = this.instabuyAmountPattern.matcher(lore)

          if (matcher.find() && this.buyAmount == matcher.group(1).toInt()) {
            this.buySlot = slotNumber
            this.changeState(BuyState.CLICK_INSTABUY, 500)
            return
          }

          if (lore.contains("loading...")) return
        }

        this.changeState(BuyState.CLICK_SIGN, 500)

        if (this.timer.hasEnded()) {
          this.stop("Couldn't find buy slot.")
        }
      }

      BuyState.CLICK_SIGN -> {
        if (!this.timer.hasEnded()) return

        val signSlot = InventoryUtil.getContainerSlotForItem("Custom Amount")
        if (signSlot == -1) {
          this.stop("Couldn't find Custom Amount Button.")
          return
        }
        InventoryUtil.clickSlot(signSlot)
        this.changeState(BuyState.VERIFY_AND_INPUT_SIGN, 500)
      }

      BuyState.VERIFY_AND_INPUT_SIGN -> {
        if (!this.timer.hasEnded()) return
        if (mc.currentScreen !is GuiEditSign) {
          this.stop("Couldn't Find Sign Gui. Retrying")
        }

        SignUtil.textToWriteOnString = this.buyAmount.toString()
        this.changeState(BuyState.VERIFY_INSTABUY_PAGE, 2000)
      }

      BuyState.VERIFY_INSTABUY_PAGE -> {
        if (mc.currentScreen is GuiChest && InventoryUtil.getOpenContainerDisplayName() == "Confirm Instant Buy") {
          log("Instant Buy Page Found")
          this.changeState(BuyState.CLICK_INSTABUY, 500)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Cannot verify Instabuy Page. Retrying")
        }
      }

      BuyState.CLICK_INSTABUY -> {
        if (!this.timer.hasEnded()) return

        val instabuySlot =
          if (this.buySlot != -1) this.buySlot else InventoryUtil.getContainerSlotForItem("Custom Amount")
        if (instabuySlot == -1) {
          this.stop("Cannot find instabuy slot.")
          return
        }

        if (this.spendLimit != 0) {
          val lore = InventoryUtil.getLore(instabuySlot)
          val matcher = this.totalCostPattern.matcher(lore)

          if (matcher.find() && matcher.group(1).toFloat() > this.spendLimit) {
            log("Cannot spend ${matcher.group(1)} because it goes over the allowed spend limit of ${this.spendLimit}.")
            this.stop("Spending more coins than allowed.")
            return
          }

          if (lore.contains("loading...")) return
        }

        InventoryUtil.clickSlot(instabuySlot)
        this.changeState(BuyState.CONFIRM_PURCHASE, 2000)
      }

      BuyState.CONFIRM_PURCHASE -> {
        if (this.timer.hasEnded()) {
          this.stop("Couldn't verify purchase.")
        }
      }
    }
  }

  // HandleSell
  private fun changeState(sellState: SellState, time: Int = 0) {
    log("Changing from ${this.sellState} to $sellState, time: $time")
    this.sellState = sellState
    this.timer = Timer(time)
  }

  private fun handleSellToBZ() {
    when (this.sellState) {
      SellState.STARTING -> {
        if (!InventoryUtil.getOpenContainerDisplayName().startsWith("Bazaar ➜ "))
          player.sendChatMessage("/bz")
        this.changeState(SellState.BZ_VERIFY, 2000)
      }

      SellState.BZ_VERIFY -> {
        if (mc.currentScreen is GuiChest && InventoryUtil.getOpenContainerDisplayName().startsWith("Bazaar ➜ ")) {
          log("Opened BZ")
          this.changeState(SellState.CLICK_SELL, 500)
        }

        if (this.timer.hasEnded()) {
          this.stop("Cannot open bz")
        }
      }

      SellState.CLICK_SELL -> {
        if (!this.timer.hasEnded()) return
        val sellType = this.instasellClickQueue.poll()
        val clickButtonSlot = InventoryUtil.getContainerSlotForItem(this.instaSellBtn[sellType])
        if (clickButtonSlot == -1) {
          if (sellType == SELL_SACK) {
            this.changeState(SellState.VERIFY_END, 500)
            return
          }
          this.stop("Could not find instasell page.")
          return
        }

        if (InventoryUtil.getLore(clickButtonSlot).contains("you don't have anything to sell!")) {
          log("Nothing to sell")
          this.changeState(SellState.VERIFY_END, 500)
          return
        }


        InventoryUtil.clickSlot(clickButtonSlot)
        this.changeState(SellState.SELL_PAGE_VERIFY, 2000)
      }

      SellState.SELL_PAGE_VERIFY -> {
        if (mc.currentScreen is GuiChest && InventoryUtil.getOpenContainerDisplayName().contains("Are you sure?")) {
          log("Found Sell Page.")
          this.changeState(SellState.CLICK_CONFIRM, 500)
          return
        }
        if (this.timer.hasEnded()) {
          this.stop("Could not find sell page.")
        }
      }

      SellState.CLICK_CONFIRM -> {
        if (!this.timer.hasEnded()) return
        val sellButton = InventoryUtil.getContainerSlotForItem("Selling whole inventory")
        if (sellButton == -1) {
          this.stop("Could not find sell button")
          return
        }

        InventoryUtil.clickSlot(sellButton)
        this.changeState(SellState.SELL_VERIFY, 2000)
      }

      SellState.SELL_VERIFY -> {
        if (this.timer.hasEnded()) {
          this.stop("Could not verify sell")
        }
      }

      SellState.VERIFY_END -> {
        if (!this.timer.hasEnded()) return

        if (this.instasellClickQueue.isEmpty()) {
          this.stop("Done Selling", true)
          return
        }
        this.changeState(SellState.STARTING)
      }
    }
  }

  enum class MainState {
    BUY_FROM_BZ, SELL_TO_BZ
  }

  enum class BuyState {
    STARTING, BZ_VERIFY,
    NAVIGATE_PAGES,
    NAVIGATE_VERIFY,
    FIND_BUY_SLOT,
    CLICK_SIGN,
    VERIFY_AND_INPUT_SIGN,
    VERIFY_INSTABUY_PAGE,
    CLICK_INSTABUY,
    CONFIRM_PURCHASE
  }

  enum class SellState {
    STARTING, BZ_VERIFY,
    CLICK_SELL, SELL_PAGE_VERIFY,
    CLICK_CONFIRM, SELL_VERIFY,
    VERIFY_END
  }
}