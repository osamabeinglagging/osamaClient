package dev.liquid.osamaclient.command

import cc.polyfrost.oneconfig.utils.commands.annotations.Command
import cc.polyfrost.oneconfig.utils.commands.annotations.Main
import cc.polyfrost.oneconfig.utils.commands.annotations.SubCommand
import dev.liquid.osamaclient.util.*
import dev.liquid.osamaclient.util.LogUtil.info
import dev.liquid.osamaclient.util.cache.AStarThreeDimensionalPathfinder
import dev.liquid.osamaclient.util.cache.CachedRegion
import dev.liquid.osamaclient.util.cache.CachedSection
import net.minecraft.util.AxisAlignedBB
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

@Command(value = "set")
class Set {
    var region: CachedRegion? = null
    val abs = mutableListOf<AxisAlignedBB>()
    var enabled = false
    var startSection: CachedSection? = null
    var endSection: CachedSection? = null

    @Main
    private fun main() {
        val star = AStarThreeDimensionalPathfinder(startSection!!, endSection!!)
        val startTime = System.nanoTime()
        val a = star.findPath()
        info("Time took to FindPath with size ${a.size}: ${(System.nanoTime() - startTime) / 1000000.0} ms")
        info("PathFind Ended. Size: ${a.size}")
        a.forEach {
            abs.add(it.cachedSection.getSectionBounds())
        }
//        val section = cachedWorld.getSectionFromCoordinates(player.posX.toInt(), player.posY.toInt(), player.posZ.toInt())
//        if (section == null) {
//            info("Section is null")
//        } else {
//            info("Section: $section was found. Finding Neighbors")
//            abs.add(section.getSectionBounds())
//            val neighbors = cachedWorld.getNeighbourSections(section)
//            neighbors.forEach {
//                abs.add(it.getSectionBounds())
//            }
//        }
//        for(i in 0 until region!!.array.size){
//            if(region!!.array[i] == null){
//                val x = (region!!.regionX * 256) + ((i and 0xF) shl 4)
//                val y = (i shr 8) shl 4
//                val z = (region!!.regionZ * 256) + (((i and 0xFF) shr 4) shl 4)
//                abs.add(AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), (x+16).toDouble(), (y+16).toDouble(), (z+16).toDouble()))
//                info("$i. x: $x, y: $y, z: $z")
//            }
//        }
    }

    @SubCommand
    private fun ws() {
        worldScanner.enable()
        enabled = true
    }

    @SubCommand
    private fun dws() {
        worldScanner.disable()
    }

    @SubCommand
    private fun clear() {
        region = null
        abs.clear()
        enabled = false
        worldScanner.disable()
        startSection = null
        endSection = null
    }

    @SubCommand
    private fun start() {
        this.startSection = cachedWorld.getSectionFromCoordinates(player.posX.toInt(), player.posY.toInt(), player.posZ.toInt())
        info("Start: $startSection")
    }

    @SubCommand
    private fun end() {
        this.endSection = cachedWorld.getSectionFromCoordinates(player.posX.toInt(), player.posY.toInt(), player.posZ.toInt())
        info("End: $endSection")
    }

    @SubscribeEvent
    fun rend(event: RenderWorldLastEvent) {
        abs.forEach { RenderUtil.drawBox(event, it, Color.RED, false) }
//        if (!enabled) return
//        cachedWorld.draw(event)
//        RenderUtil.drawTracer(player.positionVector.addVector(-10.0, -10.0, -10.0), player.positionVector.addVector(10.0, 10.0, 10.0), Color.CYAN)
//        RenderUtil.drawBox(event, player.entityBoundingBox, Color.CYAN, true)
//        if(region != null){
//            region!!.draw(event)
//        }
        if (startSection != null) startSection!!.draw(event)
        if (endSection != null) endSection!!.draw(event)
    }
}