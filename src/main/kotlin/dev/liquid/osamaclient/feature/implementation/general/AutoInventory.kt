package dev.liquid.osamaclient.feature.implementation.general

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.feature.AbstractFeature
import dev.liquid.osamaclient.feature.implementation.helper.Timer
import dev.liquid.osamaclient.feature.implementation.helper.pet.Pet
import dev.liquid.osamaclient.feature.implementation.helper.pet.PetRarity
import dev.liquid.osamaclient.util.*
import net.minecraft.client.gui.inventory.GuiChest
import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.Slot
import net.minecraft.item.ItemStack
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import java.util.LinkedList
import java.util.Queue
import java.util.regex.Pattern

class AutoInventory : AbstractFeature() {
  companion object {
    private var instance: AutoInventory? = null
    fun getInstance(): AutoInventory {
      if (instance == null) {
        instance = AutoInventory()
      }
      return instance!!
    }
  }

  override val featureName: String = "AutoInventory"
  override val isPassiveFeature: Boolean = false

  // States
  private var mainState = MainStates.NONE

  // Send Items To Hotbar
  private var itemsToHotbarState = ItemsToHotbarState.STARTING
  private var availableSlots: Queue<Int> = LinkedList()
  private var itemsToSend: Queue<String> = LinkedList()

  // Scan Pets
  private var petNamePattern = Pattern.compile("\\[Lvl\\s(\\d+)\\]\\s(.*)");
  private var petRarityPattern = Pattern.compile("(§\\w)\\w+")
  private val PET_RARITY = hashMapOf(
    "§d" to PetRarity.MYTHIC, // Not Sure
    "§6" to PetRarity.LEGENDARY,
    "§5" to PetRarity.EPIC,
    "§9" to PetRarity.RARE,
    "§a" to PetRarity.UNCOMMON,
    "§f" to PetRarity.COMMON
  )

  private var petState = PetState.STARTING
  private var petsMenu = hashMapOf<String, MutableList<Pet>>()
  private var lastMenu = ""

  override fun disable() {
    log("Disabling")
    if (!this.enabled) return

    this.enabled = false
    this.forceEnable = false
    this.mainState = MainStates.NONE
    this.itemsToHotbarState = ItemsToHotbarState.STARTING
    this.timer = Timer(0)
    this.availableSlots.clear()
    this.itemsToSend.clear()

    // Pets
    this.petState = PetState.STARTING
    this.lastMenu = ""

    InventoryUtil.closeOpenGUI()

    note("Disabled")
  }

  fun sendItemsToHotbar(items: List<String>, forceEnable: Boolean = false) {
    if (this.enabled) return

    this.enabled = true
    this.forceEnable = forceEnable
    this.succeeded = false
    this.failed = false
    this.mainState = MainStates.ITEMS_TO_HOTBAR
    this.itemsToHotbarState = ItemsToHotbarState.STARTING

    this.availableSlots = availableHotbarSlotIndex(items.toMutableList())
    this.itemsToSend.clear()
    items.forEach { if (InventoryUtil.getHotbarSlotForItem(it) == -1) this.itemsToSend.add(it) }

    note("Sending items to hotbar.")
  }

  fun scanPets(forceEnable: Boolean = false) {
    if (this.enabled) return

    this.enabled = true
    this.forceEnable = true
    this.failed = false
    this.succeeded = false
    this.mainState = MainStates.SCAN_PETS
    this.petState = PetState.STARTING
    this.petsMenu = hashMapOf()
    this.lastMenu = ""

    note("Enabling")
  }

  private fun stop(message: String, passed: Boolean = false) {
    error(message)
    this.setSuccessStatus(passed)
    this.disable()
  }

  @SubscribeEvent
  override fun onTick(event: TickEvent.ClientTickEvent) {
    if (player == null || world == null || !this.enabled) return

    when (this.mainState) {
      MainStates.ITEMS_TO_HOTBAR -> {
        this.handleSendItemsToHotbar()
      }

      MainStates.SCAN_PETS -> {
        this.handlePetScan()
      }

      else -> this.disable()
    }
  }

  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
  }

  override fun onPacketReceive(event: PacketEvent.Received) {
  }

  override fun onChatReceive(event: ClientChatReceivedEvent) {
  }

  // <editor-fold desc="Items to hotbar">
  private fun availableHotbarSlotIndex(items: MutableList<String>): LinkedList<Int> {
    val slots = LinkedList<Int>()
    for (i in 0..7) {
      val item = player.inventory.getStackInSlot(i)
      if (item == null || item.displayName == null) {
        slots.add(i); continue
      }
      val displayName = StringUtils.stripControlCodes(item.displayName)
      if (items.none { displayName.contains(it) }) slots.add(i)
    }
    return slots
  }

  private fun changeState(state: ItemsToHotbarState, time: Int = 0) {
    log("Old: ${this.itemsToHotbarState}, new: $state, time: $time")
    this.itemsToHotbarState = state
    this.timer = Timer(time)
  }

  private fun handleSendItemsToHotbar() {
    when (this.itemsToHotbarState) {
      ItemsToHotbarState.STARTING -> {
        log("In Starting")
        if (mc.currentScreen !is GuiInventory) InventoryUtil.openPlayerInventory()
        this.changeState(ItemsToHotbarState.SWAP_SLOTS, 300)

        if (this.itemsToSend.isEmpty() || this.availableSlots.isEmpty()) {
          this.changeState(ItemsToHotbarState.CLOSE_INVENTORY, 300)
        }
      }

      ItemsToHotbarState.SWAP_SLOTS -> {
        log("In Swap Slots")
        if (!this.timer.hasEnded()) return
        log("Swap slot timer ended. Clicking.")

        InventoryUtil.sendItemIntoHotbar(
          InventoryUtil.getContainerSlotForItem(this.itemsToSend.poll()), this.availableSlots.poll()
        )

        log("Swapping slots")
        this.changeState(ItemsToHotbarState.STARTING)
      }

      ItemsToHotbarState.CLOSE_INVENTORY -> {
        if (!this.timer.hasEnded()) return
        log("in close inventory")
        InventoryUtil.closeOpenGUI()

        this.setSuccessStatus(this.itemsToSend.isEmpty())
        this.disable()
      }
    }
  }
  // </editor-fold>

  // <editor-fold desc="ScanPets">
  private fun changeState(petState: PetState, time: Int = 0) {
    log("Old: ${this.petState}, new: $petState, time: $time")
    this.petState = petState
    this.timer = Timer(time)
  }

  private fun handlePetScan() {
    when (this.petState) {
      PetState.STARTING -> {
        if (!InventoryUtil.getOpenContainerDisplayName().contains("Pets")) player.sendChatMessage("/pets")
        this.changeState(PetState.VERIFY_PETS_MENU, 2000)
      }

      PetState.VERIFY_PETS_MENU -> {
        val invName = InventoryUtil.getOpenContainerDisplayName()
        if (mc.currentScreen is GuiChest && invName.contains("Pets") && invName != this.lastMenu) {
          log("Found Pets Menu")
          this.lastMenu = invName
          this.changeState(PetState.SCAN_PETS, 500)
          return
        }

        if (this.timer.hasEnded()) {
          this.stop("Could not find pets menu")
        }
      }

      PetState.SCAN_PETS -> {
        if (!this.timer.hasEnded()) return

        for (i in 0..player.openContainer.inventorySlots.size - 37) {
          val slot = player.openContainer.getSlot(i)
          if (slot == null || !slot.hasStack) continue
          val pet = this.getPetObject(slot)
          this.petsMenu.getOrPut(pet.name) { mutableListOf() }.add(pet)
        }

        this.changeState(PetState.NAVIGATE_MENU, 500)
      }

      PetState.NAVIGATE_MENU -> {
        if (!this.timer.hasEnded()) return
        val nextPageSlot = InventoryUtil.getContainerSlotForItem("Next Page")
        if (nextPageSlot == -1) {
          log("Cannot find next page. Disabling")
          this.stop("No Next Page", true)
          return
        }
        InventoryUtil.clickSlot(nextPageSlot)
        this.changeState(PetState.STARTING)
      }
    }
  }

  fun getPet(petName: String, petRarity: PetRarity? = null): Pet? {
    if (this.petsMenu.isEmpty()) return null
    val petList = this.petsMenu[petName] ?: return null
    petList.forEach {
      if (it.name == petName && (petRarity != null && it.rarity == petRarity)) return it
    }
    return null
  }

  private fun getPetObject(petSlot: Slot): Pet {
    val formattedPetName = petSlot.stack.displayName
    val rarityMatcher = this.petRarityPattern.matcher(formattedPetName)
    val nameMatcher = this.petNamePattern.matcher(StringUtils.stripControlCodes(formattedPetName))
    var rarity = PetRarity.COMMON
    var level = 1
    var name = "Osama"
    var isEquipped = false
    if (nameMatcher.find()) {
      level = nameMatcher.group(1).toInt()
      name = nameMatcher.group(2)
      if (rarityMatcher.find()) {
        rarity = this.PET_RARITY[rarityMatcher.group(1)]!!
      }
      isEquipped = InventoryUtil.getLore(petSlot.slotNumber).contains("click to despawn")
    }
    return Pet(name, level, rarity, isEquipped)
  }
  // </editor-fold>

  enum class MainStates {
    NONE, ITEMS_TO_HOTBAR, SCAN_PETS
  }

  enum class ItemsToHotbarState {
    STARTING, SWAP_SLOTS, CLOSE_INVENTORY
  }

  enum class PetState {
    STARTING, VERIFY_PETS_MENU, SCAN_PETS, NAVIGATE_MENU,
  }
}