package dev.liquid.osamaclient.util.cache

import net.minecraft.world.chunk.Chunk
import net.minecraftforge.client.event.RenderWorldLastEvent

class CachedRegion(val regionX: Int, val regionZ: Int) {
    private val MAX_SECTIONS = 4096 // 16 * 16 * 16
    private val array: Array<CachedSection?> = arrayOfNulls(MAX_SECTIONS)

    fun insert(chunk: Chunk) {
        for (i in 0..15) {
            if (chunk.blockStorageArray[i] != null) continue
            val index = getSectionIndex(chunk.xPosition, i, chunk.zPosition)
            array[index] = CachedSection(this, index)
        }
    }

    fun getSection(sectionX: Int, sectionY: Int, sectionZ: Int): CachedSection? {
        return this.array[this.getSectionIndex(sectionX, sectionY, sectionZ)]
    }

    private fun getSectionIndex(x: Int, y: Int, z: Int): Int {
        return (x - (regionX shl 4)) or (y shl 8) or ((z - (regionZ shl 4)) shl 4)
    }

    fun draw(event: RenderWorldLastEvent) {
        for (i in this.array.indices) {
            if (this.array[i] == null) {
                CachedSection(this, i).draw(event)
            }
        }
    }

    override fun toString(): String {
        return "Region($regionX, $regionZ)"
    }
}
