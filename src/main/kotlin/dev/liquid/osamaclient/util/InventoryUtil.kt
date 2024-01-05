package dev.liquid.osamaclient.util

import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest
import net.minecraft.util.StringUtils

/**
 * Utility class providing various inventory-related functions.
 */
object InventoryUtil {
  /**
   * Retrieves the display name of the currently open GUI, if it is a chest container.
   *
   * If the currently open container is not an instance of [ContainerChest], an empty string is returned.
   * Otherwise, the unformatted text of the lower chest inventory's display name is returned.
   *
   * @return The display name of the open chest GUI or an empty string if the current GUI is not a chest container.
   */
  fun getOpenContainerDisplayName(container: Container = player.openContainer): String {
    return if (container !is ContainerChest) ""
    else (container as ContainerChest).lowerChestInventory.displayName.unformattedText
  }

  /**
   * Searches the player's hotbar for an item with a display name containing the specified [itemName].
   *
   * @param itemName The name to search for, case-insensitive.
   * @return The index of the hotbar slot containing the matching item, or -1 if no match is found.
   */
  fun getHotbarSlotForItem(itemName: String): Int {
    for (i in 0..7) {
      if (player.inventory.getStackInSlot(i)?.displayName?.contains(itemName, ignoreCase = true) == true) return i
    }
    return -1
  }

  /**
   * Sets the player's current hotbar slot to the one containing an item with a display name
   * containing the specified [itemName], if found.
   *
   * @param itemName The name to search for, case-insensitive.
   * @return `true` if a matching item is found and the hotbar slot is set; `false` otherwise.
   */
  fun setHotbarSlotForItem(itemName: String): Boolean {
    val slot = getHotbarSlotForItem(itemName)
    if (slot == -1) return false

    player.inventory.currentItem = slot
    return true
  }

  /**
   * Searches the slots of the currently open container for an item with a display name
   * containing the specified [itemName], case-insensitive.
   *
   * @param itemName The name to search for.
   * @return The slot number of the matching item, or -1 if no match is found.
   */
  fun getContainerSlotForItem(itemName: String, equals: Boolean = false): Int {
    for (slot in player.openContainer.inventorySlots) {
      if (slot == null || !slot.hasStack) continue
      if (equals && StringUtils.stripControlCodes(slot.stack.displayName)
          .equals(itemName, ignoreCase = true)
      ) return slot.slotNumber
      if (!equals && StringUtils.stripControlCodes(slot.stack.displayName)
          .contains(itemName, ignoreCase = true)
      ) return slot.slotNumber
    }
    return -1
  }

  /**
   * Searches the slots of the currently open container for an item with a display name
   * containing the specified [itemName], case-insensitive.
   *
   * @param itemName The name to search for.
   * @return The slot number of the matching item, or -1 if no match is found.
   */
  fun getInventorySlotForItem(itemName: String): Int {
    for (i in 9 until player.inventoryContainer.inventorySlots.size) {
      val slot = player.inventoryContainer.getSlot(i)
      if (slot == null || !slot.hasStack) continue
      if (slot.stack.displayName.contains(itemName, ignoreCase = true)) return slot.slotNumber
    }
    return -1
  }

  /**
   * Clicks a slot in the currently open inventory/GUI.
   *
   * @param slot Slot number of the item you want to interact with.
   * @param mouseButton Left, right, or middle click. Use constants from [MouseButton].
   * @param mode Interaction mode. Use constants from [ClickMode].
   * @return Whether the slot was successfully clicked.
   */
  fun clickSlot(slot: Int, mouseButton: Int = MouseButton.LEFT, mode: Int = ClickMode.PICKUP): Boolean {
    if (mc.currentScreen == null || player.openContainer == null
      || slot == -1 || mouseButton == -1
    ) return false

    playerController.windowClick(player.openContainer.windowId, slot, mouseButton, mode, player)
    return true
  }

  /**
   * Shift-clicks the item from the specified initial slot into the container to quickly move it.
   *
   * @param slot The index of the slot containing the item to be shift-clicked.
   * @return `true` if the shift-click operation is successful; `false` otherwise.
   */
  fun shiftClickIntoContainer(slot: Int): Boolean {
    return clickSlot(slot, MouseButton.LEFT, ClickMode.QUICK_MOVE)
  }

  /**
   * Sends an item from the specified source slot to the specified hotbar slot using a swap operation.
   *
   * @param sourceSlot The index of the slot containing the item to be sent to the hotbar.
   * @param hotbarSlot The index of the hotbar slot where the item will be placed.
   * @return `true` if the item is successfully sent to the hotbar; `false` otherwise.
   */
  fun sendItemIntoHotbar(sourceSlot: Int, hotbarSlot: Int): Boolean {
    if (hotbarSlot !in 0..7) return false
    return clickSlot(sourceSlot, hotbarSlot, ClickMode.SWAP)
  }

  /**
   * Throws the item from the specified source slot by simulating a left-click with the throw action.
   *
   * @param sourceSlot The index of the slot containing the item to be thrown.
   * @return `true` if the throw action is successful; `false` otherwise.
   */
  fun throwItem(sourceSlot: Int): Boolean {
    return clickSlot(sourceSlot, MouseButton.LEFT, ClickMode.THROW)
  }

  /**
   * Swaps items between the specified source and target slots in the player's open container.
   *
   * @param sourceSlot The index of the source slot containing the item to be swapped.
   * @param targetSlot The index of the target slot for swapping the item.
   * @return `true` if the swap operation is successful; `false` otherwise.
   */
  fun swapSlots(sourceSlot: Int, targetSlot: Int): Boolean {
    if (player.openContainer == null || sourceSlot == -1 || targetSlot == -1) return false

    val sourceSlotObj = player.openContainer.getSlot(sourceSlot)
    val targetSlotObj = player.openContainer.getSlot(targetSlot)

    if (sourceSlotObj == null || targetSlotObj == null || !sourceSlotObj.hasStack) return false

    val s1 = clickSlot(sourceSlot, mode = ClickMode.PICKUP)
    val s2 = clickSlot(targetSlot, mode = ClickMode.PICKUP)
    val s3 = if (targetSlotObj.hasStack) clickSlot(sourceSlot, ClickMode.PICKUP) else true

    return s1 && s2 && s3
  }

  /**
   * Opens the player's inventory GUI.
   * This function displays the player's inventory screen using Minecraft's `GuiInventory` class.
   */
  fun openPlayerInventory() {
    mc.displayGuiScreen(GuiInventory(player))
  }

  /**
   * Closes the currently open GUI screen, if any.
   * This function checks if the player has an open container and closes it.
   */
  fun closeOpenGUI() {
    if (player.openContainer != null) player.closeScreen()
  }

  /**
   * Retrieves the lore of an ItemStack.
   *
   * @param sourceSlot The open container slot to retrieve the lore from.
   * @return The formatted lore as a single string.
   */
  fun getLore(sourceSlot: Int): String {
    val stack = player.openContainer.getSlot(sourceSlot).stack ?: return ""
    val base = stack.tagCompound.getCompoundTag("display").getTagList("Lore", 8)
    var lore = ""
    for (i in 0..base.tagCount()) {
      lore += StringUtils.stripControlCodes(base.getStringTagAt(i).lowercase().trim()) + " "
    }
    return lore
  }

  /**
   * Checks if the specified items are available in the player's inventory.
   *
   * @param items A list of item names to check for in the inventory.
   * @return `true` if all items are found in the inventory, `false` otherwise.
   */
  fun areItemsAvailableInInventory(items: MutableList<String>): Boolean {
    for (slot in player.inventoryContainer.inventorySlots) {
      if (!slot.hasStack) continue
      val itemName = StringUtils.stripControlCodes(slot.stack.displayName)
      items.removeIf { itemName.contains(it) }
    }
    return items.isEmpty()
  }

  /**
   * Checks if the specified items are available in the player's hotbar.
   *
   * @param items A list of item names to check for in the hotbar.
   * @return `true` if all items are found in the hotbar, `false` otherwise.
   */
  fun areItemsAvailableInHotbar(items: MutableList<String>): Boolean {
    for (i in 0..7) {
      val stack = player.inventory.getStackInSlot(i) ?: continue
      val itemName = StringUtils.stripControlCodes(stack.displayName ?: continue)
      items.removeIf { itemName.contains(it) }
    }
    return items.isEmpty()
  }

  /**
   * Calculates the number of empty slots in the player's currently opened container (if it is a chest).
   * @return The count of empty slots in the container; returns 0 if the container is not a chest.
   */
  fun emptySlotCountInOpenContainer(): Int {
    if (player.openContainer !is ContainerChest) return 0

    var emptySlots = 0
    for (i in 0..player.openContainer.inventorySlots.size - 37) {
      val slot = player.openContainer.getSlot(i)
      if (slot == null || !slot.hasStack) {
        emptySlots++
      }
    }
    return emptySlots
  }

  /**
   * Calculates the number of empty slots in the player's inventory (excluding hotbar).
   * @return The count of empty slots in the inventory.
   */
  fun emptySlotCountInInventory(): Int {
    var emptySlots = 0
    for (i in 9 until player.inventoryContainer.inventorySlots.size) {
      val slot = player.inventoryContainer.getSlot(i)
      if (slot == null || !slot.hasStack) {
        emptySlots++
      }
    }
    return emptySlots
  }

  fun getSlotWithSpecificAmount(itemName: String, amount: Int = 64): Int {
    for (i in 0..player.openContainer.inventorySlots.size - 37) {
      val slot = player.openContainer.getSlot(i)
      if (slot == null || !slot.hasStack) continue

      val stack = slot.stack
      val stackSize = stack.stackSize
      val slotName = StringUtils.stripControlCodes(stack.displayName).lowercase()

      if (!slotName.contains(itemName.lowercase())) continue
      if (stackSize == amount) return slot.slotNumber

    }
    return -1
  }

  fun getAllInventorySlotsForItem(itemName: String): MutableList<Int> {
    val slots = mutableListOf<Int>()
    val inventoryStartIndex = player.openContainer.inventorySlots.size - 36
    for (i in inventoryStartIndex until player.openContainer.inventorySlots.size) {
      val slot = player.openContainer.getSlot(i)
      if (slot == null || !slot.hasStack) continue
      val slotName = StringUtils.stripControlCodes(slot.stack.displayName).lowercase()
      if (slotName.contains(itemName.lowercase())) slots.add(slot.slotNumber)
    }
    return slots
  }

  fun getAllContainerSlotsForItem(vararg itemName: String): MutableList<Int> {
    val slots = mutableListOf<Int>()
    for (i in 0 until player.openContainer.inventorySlots.size - 36) {
      val slot = player.openContainer.getSlot(i)
      if (slot == null || !slot.hasStack) continue
      val slotName = StringUtils.stripControlCodes(slot.stack.displayName).lowercase()
      if (itemName.any { slotName.contains(it.lowercase()) }) slots.add(slot.slotNumber)
    }
    return slots
  }

  fun getTotalItemAmount(itemName: String): Int {
    var size = 0
    getAllInventorySlotsForItem(itemName).forEach {
      size += player.inventoryContainer.getSlot(it).stack?.stackSize ?: 0
    }
    return size
  }

  fun inventoryFillPercentage(): Float {
    return (1 - (this.emptySlotCountInInventory() / 36f)) * 100f
  }
}

/**
 * Constants representing mouse buttons for inventory interactions.
 */
object MouseButton {
  const val LEFT = 0
  const val RIGHT = 1
  const val MIDDLE = 2
}

/**
 * Constants representing different modes of inventory interactions.
 */
object ClickMode {
  const val PICKUP = 0
  const val QUICK_MOVE = 1
  const val SWAP = 2
  const val CLONE = 3
  const val THROW = 4
  const val QUICK_CRAFT = 5
  const val PICKUP_ALL = 6
}