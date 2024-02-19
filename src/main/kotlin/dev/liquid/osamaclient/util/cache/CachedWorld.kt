package dev.liquid.osamaclient.util.cache

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.client.event.RenderWorldLastEvent

// Includes a few shameless skids from Baritone
class CachedWorld {
    private val cachedRegions: Long2ObjectMap<CachedRegion> = Long2ObjectOpenHashMap();

    fun pack(chunk: Chunk) {
        this.getRegionFromChunk(chunk.xPosition, chunk.zPosition).insert(chunk)
    }

    private fun getRegionFromBlock(blockX: Int, blockZ: Int): CachedRegion {
        return this.getRegion(blockX shr 8, blockZ shr 8)
    }

    private fun getRegionFromChunk(chunkX: Int, chunkZ: Int): CachedRegion {
        return this.getRegion(chunkX shr 4, chunkZ shr 4)
    }

    private fun getRegion(regionX: Int, regionZ: Int): CachedRegion {
        return this.cachedRegions.getOrPut(this.getRegionID(regionX, regionZ)) { CachedRegion(regionX, regionZ) }
    }

    private fun getRegionID(regionX: Int, regionZ: Int): Long {
        return regionX.toLong() and 0xFFFFFFFFL or ((regionZ.toLong() and 0xFFFFFFFFL) shl 32);
    }

    fun draw(event: RenderWorldLastEvent) {
        this.cachedRegions.forEach { it?.value?.draw(event) }
    }

    // Section x,y,z are basically 3d coordiantes so i can reuse this
    // This can be both block AND section coords
    fun getSectionFromCoordinates(x: Int, y: Int, z: Int): CachedSection? {
        return this.getRegionFromBlock(x, z).getSection(x shr 4, y shr 4, z shr 4)
    }

    fun getSectionFromRegion(sectionX: Int, sectionY: Int, sectionZ: Int, region: CachedRegion): CachedSection? {
        return region.getSection(sectionX, sectionY, sectionZ)
    }

    fun getNeighbourSections(cachedSection: CachedSection): List<CachedSection> {
        val cachedSections = mutableListOf<CachedSection>()
        for (i in -1..1) {
            for (j in -1..1) {
                for (k in -1..1) {
                    if (i == 0 && j == 0 && k == 0) continue
                    val section = this.getSectionFromCoordinates(cachedSection.x + (i shl 4), cachedSection.y + (j shl 4), cachedSection.z + (k shl 4)) ?: continue
                    cachedSections.add(section)
                }
            }
        }
        return cachedSections.toList()
    }
}