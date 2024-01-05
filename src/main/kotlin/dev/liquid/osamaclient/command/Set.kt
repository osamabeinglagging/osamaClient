package dev.liquid.osamaclient.command

import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand
import dev.liquid.osamaclient.feature.implementation.general.AutoBazaar
import dev.liquid.osamaclient.feature.implementation.helper.Angle
import dev.liquid.osamaclient.feature.implementation.helper.Target
import dev.liquid.osamaclient.feature.implementation.helper.pet.PetRarity
import dev.liquid.osamaclient.macro.MacroHandler
import dev.liquid.osamaclient.util.*
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color
import kotlin.math.log

@Command(value = "set")
class Set {
  var aabbs = mutableListOf<AxisAlignedBB>()
  var blk: BlockPos? = null
  var blks = mutableListOf<BlockPos>()

  @Main
  private fun main() {
    newRot.easeTo(Target(Angle(45f, 0f)), 500)
  }

  @SubCommand
  private fun yp(yaw: Float, pitch: Float){
    player.rotationYaw = yaw
    player.rotationPitch = pitch
  }

  @SubCommand
  private fun sf() {
//    FeatureManager.getInstance().disableFeatures()
//    AutoBazaar.getInstance().disable()
//    autoChestRefill.disable()
//    MacroHandler.getInstance().disable()
//    autoBazaar.disable()
//    autoInventory.scanPets()
    val pet = autoInventory.getPet("Monkey", PetRarity.LEGENDARY)
    if(pet != null){
      LogUtil.note("Pet: $pet")
    }else{
      LogUtil.note("No Pet found")
    }
  }

  @SubCommand
  private fun buy() {
    if (!AutoBazaar.getInstance().enabled) {
      AutoBazaar.getInstance().buy("Carrot", 1408)
    } else {
      AutoBazaar.getInstance().disable()
    }
  }

  @SubCommand
  private fun b() {
    blk = player.getActualPosition().down()
  }

  @SubCommand
  private fun clear() {
    blks.clear()
    aabbs.clear()
    blk = null
  }

  @SubscribeEvent
  fun onRender(event: RenderWorldLastEvent) {
    for (block in blks) {
      RenderUtil.drawBox(block, Color(0, 255, 255, 100))
    }
    if (blk != null) {
      RenderUtil.drawBox(blk!!, Color(0, 255, 255, 100))
    }
    for (aabb in aabbs) {
      RenderUtil.drawAABB(aabb, Color(0, 255, 255, 100))
    }
  }
}