package dev.liquid.osamaclient.command

import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import cc.polyfrost.oneconfig.utils.dsl.runAsync
import dev.liquid.osamaclient.util.InventoryUtil
import dev.liquid.osamaclient.util.LogUtil.info
import dev.liquid.osamaclient.util.player

@Command(value = "set")
class Set {
  @Main
  private fun main(){
    runAsync {
      Thread.sleep(1500)
      for(i in 0..player.openContainer.inventorySlots.size){
        val slot = player.openContainer.inventorySlots[i]
        if(!slot.hasStack){
          info("i: $i, slotNumber: ${slot.slotNumber}")
        }else{
          info("i: $i, slotNumber: ${slot.slotNumber}, name: ${slot.stack.displayName}")
        }
      }
    }
  }
}