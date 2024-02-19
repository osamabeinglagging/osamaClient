package dev.liquid.osamaclient.util.cache

import dev.liquid.osamaclient.util.RenderUtil
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraftforge.client.event.RenderWorldLastEvent
import java.awt.Color

class CachedSection(private val parentRegion: CachedRegion, position: Int) {
    val x = (this.parentRegion.regionX shl 8) + ((position and 0xF) shl 4)
    val y = (position shr 8) shl 4
    val z = (this.parentRegion.regionZ shl 8) + (((position and 0xFF) shr 4) shl 4)
    fun getSectionOrigin(): BlockPos = BlockPos(x, y, z)
    fun getSectionCenter(): BlockPos = BlockPos(x + 8, y + 8, z + 8)
    fun getSectionBounds(): AxisAlignedBB = AxisAlignedBB(x.toDouble(), y.toDouble(), z.toDouble(), (x + 16).toDouble(), (y + 16).toDouble(), (z + 16).toDouble())
    fun draw(event: RenderWorldLastEvent) {
        RenderUtil.drawBox(event, this.getSectionBounds(), Color.CYAN, false)
    }

    override fun toString(): String {
        return "Section($x, $y, $z)"
    }

    override fun equals(other: Any?): Boolean {
        if (other !is CachedSection) return false
        return other.x == this.x && other.y == this.y && other.z == this.z
    }
}
