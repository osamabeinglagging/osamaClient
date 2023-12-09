package dev.liquid.osamaclient.util

import net.minecraft.client.gui.inventory.GuiInventory
import net.minecraft.inventory.Container
import net.minecraft.inventory.ContainerChest

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
  fun getContainerSlotForItem(itemName: String): Int {
    for (slot in player.openContainer.inventorySlots) {
      if (slot == null || !slot.hasStack) continue
      if (slot.stack.displayName.contains(itemName, ignoreCase = true)) return slot.slotNumber
    }
    return -1
  }

  // Documentation from MacroHQ/Macro Framework
  /**
   * Click an item in the currently open inventory / gui.
   *
   * @param slot   Slot number of the item you want to click.
   * @param button <br>
   *               - 0: Left click. <br>
   *               - 1: Right click. <br>
   *               - 2: Middle click, not sure.
   * @param type   <br>
   *               - 0: PICKUP - Regular click <br>
   *               - 1: QUICK_MOVE - Shift click to move from inventory to container for example. <br>
   *               - 2: SWAP - Not sure, would not recommend using. <br>
   *               - 3: CLONE - Not sure, would not recommend using. <br>
   *               - 4: Throw - Throw away an item from an inventory. <br>
   *               - 5: QUICK_CRAFT - Quick craft, again I don't see this being useful so don't use. <br>
   *               - 6: PICKUP_ALL - Don't know if it's different to PICKUP.
   * @return Whether the slot was successfully clicked. To prevent ping-less clicks,
   * it won't click unless there's an item in the slot.
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
   * @param initialSlot The index of the slot containing the item to be shift-clicked.
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
}

object MouseButton {
  const val LEFT = 0
  const val RIGHT = 1
  const val MIDDLE = 2
}

object ClickMode {
  const val PICKUP = 0
  const val QUICK_MOVE = 1
  const val SWAP = 2
  const val CLONE = 3
  const val THROW = 4
  const val QUICK_CRAFT = 5
  const val PICKUP_ALL = 6
}