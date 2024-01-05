package dev.liquid.osamaclient.macro.macros

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.feature.implementation.helper.Angle
import dev.liquid.osamaclient.feature.implementation.helper.Target
import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.feature.implementation.helper.pet.Pet
import dev.liquid.osamaclient.feature.implementation.helper.pet.PetRarity
import dev.liquid.osamaclient.macro.AbstractMacro
import dev.liquid.osamaclient.util.*
import net.minecraft.block.BlockAir
import net.minecraft.block.BlockDirt
import net.minecraft.block.BlockLog
import net.minecraft.block.BlockSapling
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.awt.Color
import java.security.Key

class ForagingMacro : AbstractMacro() {
  companion object {
    private var instance: ForagingMacro? = null;
    fun getInstance(): ForagingMacro {
      if (instance == null) {
        instance = ForagingMacro()
      }
      return instance!!
    }
  }

  override var macroName: String = "ForagingMacro"
  private var defaultPitch = 20f
  private var monkey: Pet? = null
  private var placeableDirts = mutableSetOf<BlockPos>()
  private var treecapitatorCooldownTimer = Timer(0)

  private var mainState = MainStates.FORAGE
  private var forageState = ForageState.STARTING
  private var walkDir = WalkDir.LEFT
  private var refillState = RefillState.START_REFILL
  private var hotbarState = HotbarState.START_HOTBAR
  private var petState = PetState.START_PETSCAN
  private var autoSellState = AutoSellState.START_AUTOSELL
  private var clearState = ClearState.START_CLEAR

  private var fixAttempts = 0
  private var fixing = false
  private var checkTimer = 0
  private var blockToClear: BlockPos? = null

  override fun canEnable(): Boolean {
    return this.enabled || this.forceEnable
  }

  override fun toggle() {
    if (this.enabled) {
      this.disable()
    } else {
      this.enable(false)
    }
  }

  override fun enable(forceEnable: Boolean) {
    if (this.enabled) return

    this.enabled = true
    this.forceEnable = forceEnable

    this.resetStates()
    this.monkey = null
    this.placeableDirts.clear()
    this.fixing = false
    this.fixAttempts = 0
    this.checkTimer = 0
    this.blockToClear = null

    this.info("Enabled")
  }

  override fun disable() {
    if (!this.enabled) return

    this.monkey = null
    this.fixing = false
    this.enabled = false
    this.checkTimer = 0
    this.fixAttempts = 0
    this.forceEnable = false
//    this.placeableDirts.clear()
    this.blockToClear = null

    KeybindUtil.updateMovement()
    KeybindUtil.toggleLeftclick(false)
    KeybindUtil.toggleRightclick(false)
    this.resetStates()
    this.stopFeatures()
    this.info("Disabled")
  }

  override fun resetStates() {
    this.mainState = MainStates.CHECKING
    this.forageState = ForageState.STARTING
    this.walkDir = WalkDir.LEFT
    this.refillState = RefillState.START_REFILL
    this.hotbarState = HotbarState.START_HOTBAR
    this.petState = PetState.START_PETSCAN
    this.autoSellState = AutoSellState.START_AUTOSELL
  }

  private fun stopFeatures() {
    autoInventory.disable()
    autoRotation.disable()
    autoBazaar.disable()
    autoChestRefill.disable()
    autoAbiphone.disable()
  }

  override fun pause() {
  }

  override fun resume() {
  }

  private fun walk(direction: WalkDir) {
    if (direction == WalkDir.NONE) {
      KeybindUtil.updateMovement()
    }
    if (direction == WalkDir.LEFT) {
      KeybindUtil.updateMovement(forward = true, left = true)
    }
    if (direction == WalkDir.RIGHT) {
      KeybindUtil.updateMovement(forward = true, right = true)
    }
  }

  override fun necessaryItems(): Pair<List<String>, List<String>> {
    val mustHaves = mutableListOf("Treecapitator")
    val needed = mutableListOf("Sapling", "Bone Meal")
    if (config.foragingMacroRodSwap) mustHaves.add("Rod")
    if (config.foragingMacroAutoRefill) mustHaves.add("Abiphone")
    if (!config.foragingMacroAutoRefill) {
      mustHaves.addAll(needed)
      needed.clear()
    }
    return Pair(mustHaves.toList(), needed.toList())
  }

  private fun getNeededDirection(): EnumFacing? {
    val dirs = BlockUtil.neighbourGenerator(player.getActualPosition(), -4, 4, 0, 0, -4, 4)
      .filter { world.getBlockState(it).block is BlockDirt }
      .map { BlockUtil.getPlayerDirectionToBeAbleToWalkOnBlock(player.getActualPosition(), it) }.groupingBy { it }
      .eachCount()

    return dirs.maxByOrNull { it.value }?.key
  }

  private fun startFix(mainState: MainStates) {
    log("Starting Fix: $mainState")
    this.fixing = true
    this.mainState = mainState
  }

  @SubscribeEvent
  override fun onTick(event: TickEvent.ClientTickEvent) {
    if (player == null || world == null || !this.canEnable()) return

    when (this.mainState) {
      MainStates.FORAGE -> this.handleForaging()
      MainStates.REFILL -> this.handleRefill()
      MainStates.HOTBAR -> this.handleHotbar()
      MainStates.PETS -> this.handlePets()
      MainStates.AUTOSELL -> this.handleAutoSell()
      MainStates.CLEAR -> this.handleClear()
      MainStates.CHECKING -> {
        this.checkTimer++
        if (this.checkTimer >= 200) {
          note("Checking for way too long. Disabling.")
          this.disable()
        }
      }
    }
  }

  // <editor-fold desc="Forage">
  private fun changeState(newState: ForageState, time: Int = 0) {
    log("Change state from: ${this.forageState}, to: $newState, timer: $time")
    this.forageState = newState
    this.timer = Timer(time)
  }

  private fun handleForaging() {
    when (this.forageState) {
      ForageState.STARTING -> {
        this.changeState(ForageState.CHECK_STATE)
      }

      ForageState.CHECK_STATE -> {
        val direction = getNeededDirection()

        if (direction == null) {
          note("Cannot find dirt. Disabling")
          this.disable()
          return
        }
        val neededAngle = Angle(AngleUtil.getYawFromDirection(direction), this.defaultPitch)
        if (!AngleUtil.isWithinAngleThreshold(neededAngle, 1f, 1f)) {
          autoRotation.easeTo(Target(neededAngle), 0)
          this.changeState(ForageState.STATE_VERIFY, 2000)
          return
        }
        this.changeState(ForageState.PLACE)
      }

      ForageState.STATE_VERIFY -> {
        if (autoRotation.hasSucceeded()) {
          log("Rotation Succeeded.")
          this.changeState(ForageState.PLACE)
          return
        }

        if (this.timer.hasEnded()) {
          note("Rotation Failed")
          this.disable()
        }
      }

      ForageState.PLACE -> {
        KeybindUtil.toggleRightclick()
        InventoryUtil.setHotbarSlotForItem("Sapling")

        val dirt1pos = BlockUtil.getRelativeBlock(0, 0, 1, player.getActualPosition())
        val dirt2pos = BlockUtil.getRelativeBlock(0, 0, 2, player.getActualPosition())

        this.placeableDirts.add(dirt1pos)
        this.placeableDirts.add(dirt2pos)

        val dirt1 = world.getBlockState(dirt1pos)
        val dirt2 = world.getBlockState(dirt2pos)

        if (dirt1.block !is BlockDirt || dirt2.block !is BlockDirt) {
          note("Cannot find dirt ahead. Retrying")
          this.changeState(ForageState.STARTING)
//          this.disable()
          return
        }

        if (this.placeableDirts.size > 4) {
          note("Somehow added more than 4 blocks. Disabling")
          this.disable()
          return
        }

        KeybindUtil.toggleRightclick()
        this.changeState(ForageState.CHECK_SAPLINGS)
        this.timer = Timer(2000)
      }

      ForageState.CHECK_SAPLINGS -> {
        val dirt1up = world.getBlockState(BlockUtil.getRelativeBlock(0, 1, 1, player.getActualPosition()))
        val dirt2up = world.getBlockState(BlockUtil.getRelativeBlock(0, 1, 2, player.getActualPosition()))

        if (dirt1up.block is BlockSapling && dirt2up.block is BlockSapling) {
          log("Sapling Placed. Moving")

          if (this.placeableDirts.size == 4) {
            val areAllSaplings = this.placeableDirts.all { world.getBlockState(it.up()).block is BlockSapling }
            if (areAllSaplings) {
              log("All blocks are saplings. Applying Bonemeal.")
              this.changeState(ForageState.APPLY_BONEMEAL, 100)
              KeybindUtil.toggleRightclick(false)
              InventoryUtil.setHotbarSlotForItem("Bone Meal")
              return
            }
          }
          this.changeState(ForageState.MOVE)
          return
        }

        if (this.timer.hasEnded()) {
          note("Could not place sapling in time. Clearing")
          this.forageState = ForageState.STARTING
          this.startFix(MainStates.CLEAR)
//          this.disable()
        }
      }

      ForageState.MOVE -> {

        this.walk(this.walkDir)
        this.changeState(ForageState.WAIT_TO_STOP)
        this.timer = Timer(2000)

        log("Moving")
      }

      ForageState.WAIT_TO_STOP -> {
        val isNotMoving = player.motionX == 0.0 && player.motionZ == 0.0
        if (isNotMoving) {
          KeybindUtil.updateMovement()
          this.changeState(ForageState.PLACE)
          if (this.walkDir == WalkDir.LEFT) {
            this.walkDir = WalkDir.RIGHT
          } else {
            this.walkDir = WalkDir.LEFT
          }
          return
        }

        if (this.timer.hasEnded()) {
          note("Could not move properly. Disabling.")
          this.disable()
        }
      }

      ForageState.APPLY_BONEMEAL -> {
        if (!this.timer.hasEnded()) return

        val lookingAt = mc.objectMouseOver
        if (lookingAt == null || lookingAt.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || world.getBlockState(
            lookingAt.blockPos
          ).block !is BlockSapling
        ) {
          note("Not Looking at a sapling. Disabling.")
          this.disable()
          return
        }

        KeybindUtil.rightClick()
        if (config.foragingMacroRodSwap) {
          this.changeState(ForageState.HOLD_ROD)
        } else {
          this.changeState(ForageState.WAIT_FOR_GROWTH, 2000)
        }
      }

      ForageState.WAIT_FOR_GROWTH -> {
        if (this.placeableDirts.size < 4) {
          note("For some reason there are less than 4 placeable blocks. Disabling")
          this.disable()
          return
        }

        val areAllLogs = this.placeableDirts.all { world.getBlockState(it.up()).block is BlockLog }
        if (areAllLogs) {
          log("Tree grown")
          this.changeState(ForageState.BREAK, 150)
          InventoryUtil.setHotbarSlotForItem("Treecapitator")
          return
        }

        if (this.timer.hasEnded()) {
          note("Tree didnt grow fast enuf. Retrying")
          this.forageState = ForageState.STARTING
          this.startFix(MainStates.CLEAR)
//          this.disable()
        }
      }

      ForageState.BREAK -> {
        if (!this.timer.hasEnded()) return
        if (!this.treecapitatorCooldownTimer.hasEnded()) return
        val lookingAt = mc.objectMouseOver
        if (lookingAt == null || lookingAt.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || world.getBlockState(
            lookingAt.blockPos
          ).block !is BlockLog
        ) {
          note("Not looking at a log. Disabling")
          this.disable()
          return
        }

        KeybindUtil.toggleLeftclick()
        this.changeState(ForageState.VERIFY_BREAK)
        this.timer = Timer(2000)
      }

      ForageState.VERIFY_BREAK -> {
        val areAllAir = this.placeableDirts.all { world.isAirBlock(it.up()) }
        val lookingAt = mc.objectMouseOver

        if (world.isAirBlock(BlockUtil.getRelativeBlock(0, 1, 1, player.getActualPosition()))) {
          val cooldown =
            if (config.foragingMacroRodSwap || (this.monkey != null && this.monkey!!.isEquipped)) (10 * this.monkey!!.level) + 100
            else 2000
          log("Treecap Cooldown")
          this.treecapitatorCooldownTimer = Timer(cooldown) // 50 is an offset
        }

        if (lookingAt != null && lookingAt.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK
          && (world.getBlockState(lookingAt.blockPos).block !is BlockLog && world.getBlockState(lookingAt.blockPos).block !is BlockAir)
        ) {
          KeybindUtil.toggleLeftclick(false)
        }

        if (areAllAir) {
          log("Successfully broke logs.")
          KeybindUtil.toggleLeftclick(false)
          this.changeState(ForageState.STARTING)
          return
        }

        if (this.timer.hasEnded()) {
          note("Could not break logs. Disabling")
          KeybindUtil.toggleLeftclick(false)
          this.forageState = ForageState.STARTING
          this.startFix(MainStates.CLEAR)
        }
      }

      ForageState.HOLD_ROD -> {
        InventoryUtil.setHotbarSlotForItem("Rod")
        this.changeState(ForageState.USE_ROD)
      }

      ForageState.USE_ROD -> {
        KeybindUtil.rightClick()
        this.changeState(ForageState.WAIT_FOR_GROWTH, 2000)
        KeybindUtil.toggleRightclick(false)

        log("Using Rod")
      }
    }
  }
  // </editor-fold>

  // <editor-fold desc="Refill">
  private fun changeState(refillState: RefillState, time: Int = 0) {
    log("From: ${this.refillState}, to: $refillState, time: $time")
    this.refillState = refillState
    this.timer = Timer(time)
  }

  private fun handleRefill() {
    when (this.refillState) {
      RefillState.START_REFILL -> {
        autoChestRefill.enable(ConfigUtil.getForagingMacroSaplingType())
        this.changeState(RefillState.VERIFY_REFILL, 10 * 60 * 1000)
      }

      RefillState.VERIFY_REFILL -> {
        if (autoChestRefill.hasSucceeded()) {
          note("Chest refill successful.")
          this.fixing = false
          this.mainState = MainStates.CHECKING
          this.changeState(RefillState.START_REFILL)
        }

        if (autoChestRefill.hasFailed() || this.timer.hasEnded()) {
          note("Couldn't refill chests. Retrying")
          this.mainState = MainStates.CHECKING
          this.changeState(RefillState.START_REFILL)
          this.fixAttempts++
        }
      }
    }
  }
  // </editor-fold>

  // <editor-fold desc="FixHotbar">
  private fun changeState(hotbarState: HotbarState, time: Int = 0) {
    log("From: ${this.hotbarState}, to $hotbarState, time: $time")
    this.hotbarState = hotbarState
    this.timer = Timer(time)
  }

  private fun handleHotbar() {
    when (this.hotbarState) {
      HotbarState.START_HOTBAR -> {
        log("Starting hotbar fix")
        autoInventory.sendItemsToHotbar(this.necessaryItems().second + this.necessaryItems().first)
        this.changeState(HotbarState.VERIFY_HOTBAR, 30 * 1000)
      }

      HotbarState.VERIFY_HOTBAR -> {
        val succeeded = autoInventory.hasSucceeded()
        val failed = autoInventory.hasFailed() || this.timer.hasEnded()

        if (succeeded || failed) {
          this.changeState(HotbarState.START_HOTBAR)
          this.mainState = MainStates.CHECKING
          this.fixing = false
        }
        if (succeeded) {
          note("HobarFix Succeeded")
          return
        }
        if (failed) {
          note("HotbarFix Failed. Retrying")
          this.fixAttempts++
        }
      }
    }
  }

  // </editor-fold>

  // <editor-fold desc="PetScan">
  private fun changeState(petState: PetState, time: Int = 0) {
    log("From: ${this.petState}, to $petState, time: $time")
    this.petState = petState
    this.timer = Timer(time)
  }

  private fun handlePets() {
    when (this.petState) {
      PetState.START_PETSCAN -> {
        log("Starting petscan")
        autoInventory.scanPets()
        this.changeState(PetState.VERIFY_PETSCAN, 20 * 1000)
      }

      PetState.VERIFY_PETSCAN -> {
        val succeeded = autoInventory.hasSucceeded()
        val failed = autoInventory.hasFailed() || this.timer.hasEnded()

        if (succeeded || failed) {
          this.changeState(PetState.START_PETSCAN)
          this.mainState = MainStates.CHECKING
          this.fixing = false
        }
        if (succeeded) {
          note("PetScan Succeeded")
          this.monkey = autoInventory.getPet("Monkey", PetRarity.LEGENDARY)
          if (this.monkey == null) {
            this.error("No Legendary Monkey pet found")
            this.disable()
          }
          return
        }
        if (failed) {
          note("PetScan Failed. Retrying")
          this.fixAttempts++
        }
      }
    }
  }

  // </editor-fold>

  // <editor-fold desc="AutoSell">
  private fun changeState(autoSellState: AutoSellState, time: Int = 0) {
    log("From: ${this.autoSellState}, to $autoSellState, time: $time")
    this.autoSellState = autoSellState
    this.timer = Timer(time)
  }

  private fun handleAutoSell() {
    when (this.autoSellState) {
      AutoSellState.START_AUTOSELL -> {
        log("Starting AutoSell")
        autoBazaar.sell(*ConfigUtil.getAutoSellType())
        this.changeState(AutoSellState.VERIFY_AUTOSELL, 20 * 1000)
      }

      AutoSellState.VERIFY_AUTOSELL -> {
        val succeeded = autoBazaar.hasSucceeded()
        val failed = autoBazaar.hasFailed() || this.timer.hasEnded()

        if (succeeded || failed) {
          this.changeState(AutoSellState.START_AUTOSELL)
          this.mainState = MainStates.CHECKING
          this.fixing = false
        }
        if (succeeded) {
          note("AutoSell Succeeded")
          return
        }
        if (failed) {
          note("AutoSell Failed. Retrying")
          this.fixAttempts++
        }
      }
    }
  }

  // </editor-fold>

  // <editor-fold desc="Clear">
  private fun changeState(clearState: ClearState, time: Int = 0) {
    log("From: ${this.clearState}, to $clearState, time: $time")
    this.clearState = clearState
    this.timer = Timer(time)
  }

  private fun handleClear() {
    when (this.clearState) {
      ClearState.START_CLEAR -> {
        InventoryUtil.setHotbarSlotForItem("Treecapitator")
        this.changeState(ClearState.FIND_BLOCK)
      }

      ClearState.FIND_BLOCK -> {
        this.blockToClear = this.placeableDirts.firstOrNull { !world.isAirBlock(it.up()) }
        if (this.blockToClear != null) {
          log("Found block to clear")
          this.changeState(ClearState.LOOK_AT_BLOCK)
          return
        }

        note("Cleared all blocks")
        this.mainState = MainStates.CHECKING
        this.fixing = false
        this.clearState = ClearState.START_CLEAR
      }

      ClearState.LOOK_AT_BLOCK -> {
        autoRotation.easeTo(Target(AngleUtil.getAngle(this.blockToClear!!.up())))
        this.changeState(ClearState.LOOK_VERIFY, 2000)
      }

      ClearState.LOOK_VERIFY -> {
        if (autoRotation.hasSucceeded()) {
          log("Looked at block. Breaking")
          this.changeState(ClearState.BREAK)
          return
        }

        if (world.isAirBlock(this.blockToClear!!.up())) {
          log("Block broke while rotating")
          autoRotation.disable()
          this.changeState(ClearState.START_CLEAR)
          return
        }

        if (autoRotation.hasFailed() || this.timer.hasEnded()) {
          log("Couldn't look at block. Retrying")
          this.mainState = MainStates.CHECKING
          this.fixing = false
          this.fixAttempts++
          this.clearState = ClearState.START_CLEAR
        }
      }

      ClearState.BREAK -> {
        KeybindUtil.toggleLeftclick()
        this.changeState(ClearState.BREAK_VERIFY, 2000)
      }

      ClearState.BREAK_VERIFY -> {
        if (world.isAirBlock(this.blockToClear!!.up())) {
          log("Block Broken.")
          this.changeState(ClearState.START_CLEAR)
          KeybindUtil.toggleLeftclick(false)
          return
        }

        if (this.timer.hasEnded()) {
          log("Could not break block. Retrying")
          this.mainState = MainStates.CHECKING
          this.fixing = false
          this.fixAttempts++
          this.clearState = ClearState.START_CLEAR
          KeybindUtil.toggleLeftclick(false)
        }
      }
    }
  }

  // </editor-fold>
  @SubscribeEvent
  fun onTickCheckBroken(event: TickEvent.ClientTickEvent) {
    if (player == null || world == null || !this.canEnable() || this.fixing) return

    val necessaryItems = this.necessaryItems()

    if (!InventoryUtil.areItemsAvailableInInventory(necessaryItems.first.toMutableList())) {
      note("Necessary items are not in inventory. Make sure these are in your inventory: ")
      note((necessaryItems.first + necessaryItems.second).toString())
      this.disable()
      return
    }

    if (!InventoryUtil.areItemsAvailableInInventory(necessaryItems.second.toMutableList())) {
      note("Needed items are not in inventory.")
      if (!config.foragingMacroAutoRefill) { // It should never be true but just in case.
        note("Not allowed to refill. Disabling.")
        this.disable()
      } else {
        note("Starting refill.")
        this.startFix(MainStates.REFILL)
      }
      return
    }

    if (!InventoryUtil.areItemsAvailableInHotbar(necessaryItems.first.toMutableList()) || !InventoryUtil.areItemsAvailableInHotbar(
        necessaryItems.second.toMutableList()
      )
    ) {
      note("Items are not in Hotbar. Swapping items to hotbar.")
      this.startFix(MainStates.HOTBAR)
      return
    }

    if (this.monkey == null && (config.foragingMacroRodSwap || config.foragingMacroDefaultMonkey)) {
      log("Checking Monkey Pet.")
      this.startFix(MainStates.PETS)
      return
    }

    if (InventoryUtil.inventoryFillPercentage() > config.featureAutoSellInvFullPercentage) {
      note("Inventory full. Toggling AutoSell")
      this.startFix(MainStates.AUTOSELL)
      return
    }

    this.mainState = MainStates.FORAGE
  }

  @SubscribeEvent
  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
//    if (!this.enabled) return
//
//    for (block in this.placeableDirts) {
//      RenderUtil.drawAABB(BlockUtil.findBoundsFromBlocks(*this.placeableDirts.toTypedArray()), Color(0, 255, 255, 50))
//    }
//
//    if (this.blockToClear != null) RenderUtil.drawBox(this.blockToClear!!, Color(0, 255, 255, 50))
  }

  override fun onChatMessageReceive(event: ClientChatReceivedEvent) {
    TODO("Not yet implemented")
  }

  override fun onPacketReceive(event: PacketEvent.Received) {
    TODO("Not yet implemented")
  }

  enum class MainStates {
    CHECKING, FORAGE, REFILL, HOTBAR, PETS, AUTOSELL, CLEAR
  }

  enum class ForageState {
    STARTING,
    CHECK_STATE, STATE_VERIFY, // Mainly Rotation - and Saved states in case i make them
    PLACE,
    CHECK_SAPLINGS,
    MOVE,
    WAIT_TO_STOP,
    APPLY_BONEMEAL,
    WAIT_FOR_GROWTH,
    BREAK,
    VERIFY_BREAK,
    HOLD_ROD,
    USE_ROD,
  }

  enum class RefillState {
    START_REFILL, VERIFY_REFILL
  }

  enum class HotbarState {
    START_HOTBAR, VERIFY_HOTBAR
  }

  enum class PetState {
    START_PETSCAN, VERIFY_PETSCAN
  }

  enum class AutoSellState {
    START_AUTOSELL, VERIFY_AUTOSELL
  }

  enum class ClearState {
    START_CLEAR, FIND_BLOCK, LOOK_AT_BLOCK, LOOK_VERIFY, BREAK, BREAK_VERIFY,
  }

  enum class WalkDir {
    NONE, LEFT, RIGHT
  }
}