package dev.liquid.osamaclient.util

import net.minecraft.inventory.ContainerChest

class InventoryUtil {
  fun getOpenGuiName(): String {
    return if (player.openContainer !is ContainerChest) ""
    else (player.openContainer as ContainerChest).lowerChestInventory.displayName.unformattedText
  }
}