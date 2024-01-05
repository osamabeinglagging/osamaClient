package dev.liquid.osamaclient.feature.implementation.foragingmacro

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.feature.AbstractFeature
import dev.liquid.osamaclient.feature.implementation.helper.Angle
import dev.liquid.osamaclient.feature.implementation.helper.Target
import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.util.*
import net.minecraft.util.BlockPos
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import kotlin.math.max
import kotlin.math.min

class AutoChestRefill : AbstractFeature() {
  companion object {
    private var instance: AutoChestRefill? = null
    fun getInstance(): AutoChestRefill {
      if (instance == null) {
        instance = AutoChestRefill()
      }
      return instance!!
    }
  }

  override val featureName: String = "AutoChestRefill"
  override val isPassiveFeature: Boolean = false
  private var chests = mutableListOf<BlockPos>() to mutableListOf<BlockPos>()
  private var saplingName = ""
  private var mainState = MainState.NONE
  private var checkState = CheckState.STARTING
  private var buyState = BuyState.STARTING
  private var loadState = LoadState.STARTING
  private var loadItem = LoadItem.SAPLING

  private var loadAttempt = 0
  private var saplingPurchaseSlot = -1

  private var targetChestPos: BlockPos? = null
  private var chestEmptySlotCount = -1

  private var fixingInventory = false

  private val builderSlots = hashMapOf(
    "Builder" to "Green Thumb",
    "Green Thumb" to ConfigUtil.getForagingMacroSaplingType(),
  )

  fun enable(saplingName: String, forceEnable: Boolean = false) {
    if (this.enabled) return

    this.resetStates()
    this.enabled = true
    this.forceEnable = forceEnable
    this.chests = BlockUtil.findChestsToRefill()
    this.saplingName = saplingName

    if (this.saplingName.isEmpty()) {
      this.stop("No Sapling Name Provided.")
      return
    }

    if (this.chests.first.isEmpty() || this.chests.second.isEmpty()) {
      this.stop("Failed to find some chests. Disabling.")
      return
    }

    this.targetChestPos = null
    this.mainState = MainState.CHECK_CHEST
    this.loadItem = LoadItem.SAPLING
    this.chestEmptySlotCount = -1
    this.saplingPurchaseSlot = -1
    this.loadAttempt = 0
    this.fixingInventory = false

    info("Starting.")
  }

  override fun disable() {
    if (!this.enabled) return

    this.enabled = false
    this.forceEnable = false
    this.loadItem = LoadItem.SAPLING
    this.chests = mutableListOf<BlockPos>() to mutableListOf<BlockPos>()
    this.targetChestPos = null
    this.chestEmptySlotCount = -1
    this.saplingPurchaseSlot = -1
    this.loadAttempt = 0
    this.fixingInventory = false
    this.resetStates()

    info("Stopping")
  }

  private fun resetStates() {
    this.mainState = MainState.NONE
    this.checkState = CheckState.STARTING
    this.buyState = BuyState.STARTING
    this.loadState = LoadState.STARTING
  }

  private fun stop(message: String, succeeded: Boolean = false) {
    log(message)
    this.setSuccessStatus(succeeded)
    this.disable()
  }

  private fun removeTargetFromTotalChests() {
    if (this.loadItem == LoadItem.SAPLING) {
      this.chests.first.remove(this.targetChestPos)
    } else {
      this.chests.second.remove(this.targetChestPos)
    }
    this.targetChestPos = null
  }

  private fun itemToBuy(item: LoadItem = this.loadItem): String {
    return if (item == LoadItem.SAPLING) {
      this.saplingName
    } else {
      "Enchanted Bone Meal"
    }
  }

  @SubscribeEvent
  override fun onTick(event: TickEvent.ClientTickEvent) {
    if (player == null || world == null || !this.enabled) return

    when (this.mainState) {
      MainState.NONE -> {}
      MainState.CHECK_CHEST -> this.handleCheck()
      MainState.BUY_ITEM -> this.handleBuy()
      MainState.LOAD_ITEM -> this.handleLoad()
    }
  }

  // HandleCheck
  private fun changeState(checkState: CheckState, time: Int = 0) {
    log("Old: ${this.checkState}, new: $checkState, time: $time")
    this.checkState = checkState
    this.timer = Timer(time)
  }

  private fun handleCheck() {
    when (this.checkState) {
      CheckState.STARTING -> {

        if (this.chests.first.isEmpty() && this.loadItem == LoadItem.SAPLING) {
          log("No more sapling chests to fill. Changing to bonemeal. ${this.chests.second.isEmpty()}")
          this.loadItem = LoadItem.BONE_MEAL
        }

        if (this.chests.second.isEmpty() && this.loadItem == LoadItem.BONE_MEAL) {
          this.resetStates()
          this.mainState = MainState.BUY_ITEM
          this.chestEmptySlotCount = 1
          this.fixingInventory = true

          if (InventoryUtil.getTotalItemAmount(this.itemToBuy(LoadItem.SAPLING)) < 10) {
            this.loadItem = LoadItem.SAPLING
          } else if (InventoryUtil.getTotalItemAmount(this.itemToBuy(LoadItem.BONE_MEAL)) < 10) {
            this.loadItem = LoadItem.BONE_MEAL
          } else {
            this.stop("No more bonemeal chests. Disabling", true)
          }
          return
        }

        if (this.targetChestPos == null) {
          if (this.loadItem == LoadItem.SAPLING) {
            this.targetChestPos = this.chests.first.first()
          } else {
            this.targetChestPos = this.chests.second.first()
          }
        }
        InventoryUtil.setHotbarSlotForItem("Treecapitator")
        this.changeState(CheckState.LOOKING)
      }

      CheckState.LOOKING -> {
        autoRotation.easeTo(Target(this.targetChestPos!!))
        this.changeState(CheckState.LOOKING_VERIFY, 2000)
      }

      CheckState.LOOKING_VERIFY -> {
        if (autoRotation.hasSucceeded()) {
          log("Rotation Succeeded. Opening Chest")
          this.changeState(CheckState.OPEN_CHEST, 250)
          return
        }

        if (autoRotation.hasFailed() || this.timer.hasEnded()) {
          this.stop("Could not finish rotation in time. Disabling")
        }
      }

      CheckState.OPEN_CHEST -> {
        if (!this.timer.hasEnded()) return
        val lookingAt = mc.objectMouseOver

        if (lookingAt == null || lookingAt.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || lookingAt.blockPos != this.targetChestPos) {
          this.stop("Not Looking at Chest.")
          return
        }

        KeybindUtil.rightClick()
        this.changeState(CheckState.OPENING_VERIFY, 2000)
      }

      CheckState.OPENING_VERIFY -> {
        if (mc.currentScreen != null && InventoryUtil.getOpenContainerDisplayName().contains("Chest")) {
          this.changeState(CheckState.FIND_SLOTS, 250)
          log("Opened Chest Successfully.")
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Could not open chest.")
        }
      }

      CheckState.FIND_SLOTS -> {
        if (!this.timer.hasEnded()) return

        this.chestEmptySlotCount = InventoryUtil.emptySlotCountInOpenContainer()
        log("Found Slots: ${this.chestEmptySlotCount}")
        this.changeState(CheckState.CLOSE, 250)
      }

      CheckState.CLOSE -> {
        if (!this.timer.hasEnded()) return
        InventoryUtil.closeOpenGUI()

        if (this.chestEmptySlotCount == 0) {
          log("Chest is already full. Removing and restarting")
          this.resetStates()
          this.mainState = MainState.CHECK_CHEST
          this.removeTargetFromTotalChests()
          return
        }

        log("Ending ChestCheckState")
        this.changeState(CheckState.STARTING)
        this.mainState = MainState.BUY_ITEM
      }
    }
  }

  // HandleBuy
  private fun changeState(buyState: BuyState, time: Int = 0) {
    log("Old: ${this.buyState}, new: $buyState, time: $time")
    this.buyState = buyState
    this.timer = Timer(time)
  }

  private fun handleBuy() {
    when (this.buyState) {
      BuyState.STARTING -> {
        if (this.loadItem == LoadItem.SAPLING) {
          this.changeState(BuyState.LOOKING)
        } else {
          this.changeState(BuyState.AUTO_BAZAAR)
        }
      }

      BuyState.LOOKING -> {
        autoRotation.easeTo(Target(Angle(AngleUtil.PLAYER_ANGLE.yaw, -25f)))
        this.changeState(BuyState.LOOKING_VERIFY, 2000)
      }

      BuyState.LOOKING_VERIFY -> {
        if (autoRotation.hasSucceeded()) {
          log("Rotation Ended")
          this.changeState(BuyState.CALL_BUILDER)
          return
        }

        if (autoRotation.hasFailed() || this.timer.hasEnded()) {
          this.stop("Could not look down.")
        }
      }

      BuyState.CALL_BUILDER -> {
        autoAbiphone.call("Builder")
        this.changeState(BuyState.CALL_VERIFY, 15000)
      }

      BuyState.CALL_VERIFY -> {
        if (autoAbiphone.hasSucceeded()) {
          log("Called Builder. Waiting for Builder Chest Page")
          this.changeState(BuyState.VERIFY_BUILDER_PAGE, 15000)
          return
        }

        if (autoAbiphone.hasFailed() || this.timer.hasEnded()) {
          this.stop("Could not call builder")
        }
      }

      BuyState.VERIFY_BUILDER_PAGE -> {
        if (mc.currentScreen != null && InventoryUtil.getOpenContainerDisplayName().contains("Builder")) {
          log("Opened Builder Page.")
          this.changeState(BuyState.NAVIGATE_TO_SAPLING_PAGE, 400)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Could not open Builder Page.")
        }
      }

      BuyState.NAVIGATE_TO_SAPLING_PAGE -> {
        if (!this.timer.hasEnded()) return

        val displayName = InventoryUtil.getOpenContainerDisplayName()

        if (displayName.contains("Shop Trading Options")) {
          log("Opened Sapling Page")
          this.changeState(BuyState.PURCHASE_SAPLING, 400)
          return
        }

        val slotName = this.builderSlots[displayName]
        if (slotName == null) {
          this.stop("Could not find slot to click. Current Inv Name: ${InventoryUtil.getOpenContainerDisplayName()}")
          return
        }

        InventoryUtil.clickSlot(InventoryUtil.getContainerSlotForItem(slotName), MouseButton.RIGHT)
        this.changeState(BuyState.NAVIGATE_TO_SAPLING_PAGE, 400)
      }

      BuyState.PURCHASE_SAPLING -> {
        if (!this.timer.hasEnded()) return

        if (InventoryUtil.emptySlotCountInInventory() == 0 || this.chestEmptySlotCount == 0) {
          log("No more slots in inventory/chest. Starting Load")
          this.changeState(BuyState.CLOSE_CHEST, 400)
          return
        }

        if (this.saplingPurchaseSlot == -1) {
          this.saplingPurchaseSlot =
            InventoryUtil.getSlotWithSpecificAmount(this.itemToBuy(), 64)
          if (this.saplingPurchaseSlot == -1) {
            this.stop("Could not find sapling slot to click.")
            return
          }
        }

        if (!player.openContainer.getSlot(this.saplingPurchaseSlot).hasStack) return

        InventoryUtil.clickSlot(this.saplingPurchaseSlot)
        this.chestEmptySlotCount--
        this.changeState(BuyState.PURCHASE_SAPLING)
      }

      BuyState.AUTO_BAZAAR -> {
        val emptyInvSlots = InventoryUtil.emptySlotCountInInventory()
        val amountToBuy = min(this.chestEmptySlotCount, emptyInvSlots) * 64
        this.chestEmptySlotCount = max(0, this.chestEmptySlotCount - emptyInvSlots)

        autoBazaar.buy("Enchanted Bone Meal", amountToBuy)
        this.changeState(BuyState.AUTO_BAZAAR_VERIFY, 20000)
      }

      BuyState.AUTO_BAZAAR_VERIFY -> {
        if (autoBazaar.hasSucceeded()) {
          log("Bought Stuff")
          this.changeState(BuyState.CLOSE_CHEST, 500)
          return
        }

        if (autoBazaar.hasFailed() || this.timer.hasEnded()) {
          this.stop("Couldn't buy bonemeal")
        }
      }

      BuyState.CLOSE_CHEST -> {
        if (!this.timer.hasEnded()) return

        log("Switching to load")
        this.changeState(BuyState.STARTING)
        this.mainState = MainState.LOAD_ITEM
        InventoryUtil.closeOpenGUI()
        this.saplingPurchaseSlot = -1
      }
    }
  }

  // HandleLoad
  private fun changeState(loadState: LoadState, time: Int = 0) {
    log("Old: ${this.loadState}, new: $loadState, time: $time")
    this.loadState = loadState
    this.timer = Timer(time)
  }

  private fun handleLoad() {

    if (this.loadAttempt > 3) {
      this.stop("Tried $loadAttempt times to load chest but failed")
      return
    }

    when (this.loadState) {
      LoadState.STARTING -> {
        log("Starting Loadstate.")

        if (this.fixingInventory) {
          this.chestEmptySlotCount = 0
          this.resetStates()
          this.mainState = MainState.CHECK_CHEST
          return
        }

        InventoryUtil.setHotbarSlotForItem("Treecapitator")
        this.changeState(LoadState.LOOKING, 250)
      }

      LoadState.LOOKING -> {
        if (!this.timer.hasEnded()) return

        autoRotation.easeTo(Target(this.targetChestPos!!))
        this.changeState(LoadState.LOOKING_VERIFY, 2000)
      }

      LoadState.LOOKING_VERIFY -> {
        if (autoRotation.hasSucceeded()) {
          log("Rotation Successful")
          this.changeState(LoadState.OPEN_CHEST, 300)
          return
        }

        if (autoRotation.hasFailed() || this.timer.hasEnded()) {
          this.stop("Could Not Finish Rotation")
        }
      }

      LoadState.OPEN_CHEST -> {
        if (!this.timer.hasEnded()) return

        val lookingAt = mc.objectMouseOver
        if (lookingAt == null || lookingAt.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || lookingAt.blockPos != this.targetChestPos) {
          this.stop("Not looking at chest/chest out of reach")
          return
        }

        KeybindUtil.rightClick()
        this.changeState(LoadState.OPENING_VERIFY, 2000)
      }

      LoadState.OPENING_VERIFY -> {
        if (mc.currentScreen != null && InventoryUtil.getOpenContainerDisplayName().contains("Chest")) {
          log("Opened Chest")
          this.changeState(LoadState.LOAD_ITEMS_IN_CHEST, 250)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Could not verify open chest")
        }
      }

      LoadState.LOAD_ITEMS_IN_CHEST -> {
        if (!this.timer.hasEnded()) return

        val slots = InventoryUtil.getAllInventorySlotsForItem(this.itemToBuy())
        if (slots.isEmpty()) {
          this.stop("Could not find any items in inventory. cannot load anything.")
          return
        }
        for (i in 0 until slots.size) {
          InventoryUtil.clickSlot(slots[i], mode = ClickMode.QUICK_MOVE)
        }
        this.changeState(LoadState.WAIT_FOR_ITEMS_TO_LOAD, 500)
      }

      LoadState.WAIT_FOR_ITEMS_TO_LOAD -> {
        if (!this.timer.hasEnded()) return
        val slots = InventoryUtil.getAllInventorySlotsForItem(this.itemToBuy())
        val chestSlots = InventoryUtil.emptySlotCountInOpenContainer()
        if (slots.isEmpty() || chestSlots == 0) {
          log("Successfully moved items into chest because inv is empty or chest is full.")
          this.changeState(LoadState.DISABLE)
          return
        }

        log("Could not move everything in inventory some stuff might have lagged back")
        this.changeState(LoadState.LOAD_ITEMS_IN_CHEST)
        this.loadAttempt++
      }

      LoadState.DISABLE -> {
        InventoryUtil.closeOpenGUI()
        this.resetStates()
        if (this.chestEmptySlotCount == 0) {
          log("Chest is full. Moving onto another chest")
          this.mainState = MainState.CHECK_CHEST
          this.removeTargetFromTotalChests()
        } else {
          log("Chest is not full. Buying again")
          this.mainState = MainState.BUY_ITEM
        }
      }
    }
  }

  enum class LoadItem {
    SAPLING, BONE_MEAL
  }

  enum class MainState {
    NONE,
    CHECK_CHEST,
    BUY_ITEM,
    LOAD_ITEM,
  }

  enum class CheckState {
    STARTING,
    LOOKING, LOOKING_VERIFY,
    OPEN_CHEST, OPENING_VERIFY,
    FIND_SLOTS, CLOSE,
  }

  enum class BuyState {
    STARTING,
    LOOKING, LOOKING_VERIFY,
    CALL_BUILDER, CALL_VERIFY,
    VERIFY_BUILDER_PAGE,
    NAVIGATE_TO_SAPLING_PAGE,
    PURCHASE_SAPLING,
    AUTO_BAZAAR, AUTO_BAZAAR_VERIFY,
    CLOSE_CHEST,
  }

  enum class LoadState {
    STARTING,
    LOOKING, LOOKING_VERIFY,
    OPEN_CHEST, OPENING_VERIFY,
    LOAD_ITEMS_IN_CHEST,
    WAIT_FOR_ITEMS_TO_LOAD,
    DISABLE
  }

  @SubscribeEvent
  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
    if (!this.enabled || this.targetChestPos == null) return
    val cChest = BlockUtil.connectedChestPosition(this.targetChestPos!!) ?: return
    RenderUtil.drawAABB(BlockUtil.findBoundsFromBlocks(this.targetChestPos!!, cChest), Color(0, 255, 255, 100))
  }

  override fun onPacketReceive(event: PacketEvent.Received) {}
  override fun onChatReceive(event: ClientChatReceivedEvent) {}
}
