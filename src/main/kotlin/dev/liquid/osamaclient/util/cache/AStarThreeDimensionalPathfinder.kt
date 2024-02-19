package dev.liquid.osamaclient.util.cache

import dev.liquid.osamaclient.util.LogUtil.info
import dev.liquid.osamaclient.util.cachedWorld
import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import net.minecraft.util.BlockPos
import kotlin.math.max
import kotlin.math.sqrt

class AStarThreeDimensionalPathfinder(startPos: CachedSection, endPos: CachedSection) {
    private val startNode: PathNode
    private val endNode: PathNode
    private val openSet = BinaryHeapOpenSet()
    private val closedSet: Long2ObjectMap<PathNode> = Long2ObjectOpenHashMap()

    init {
        val startSection = cachedWorld.getSectionFromCoordinates(startPos.x, startPos.y, startPos.z)
        val endSection = cachedWorld.getSectionFromCoordinates(endPos.x, endPos.y, endPos.z)
        if (startSection == null || endSection == null) {
            info("Either StartSection: $startSection or EndSection: $endSection is null.")
        }
        val totalDist = this.distanceTo(endSection!!, startSection!!)
        this.startNode = PathNode(startSection, 0.0, totalDist)
        this.endNode = PathNode(endSection, totalDist, 0.0)

        this.openSet.add(this.startNode)
    }

    fun findPath(): List<PathNode> {
        var i = 2000
        while (!this.openSet.isEmpty() && i-- > 0) {
            val currentNode = this.openSet.poll()
//            info("CurrentNode: $currentNode")
            if(currentNode == endNode){
                return this.reconstructPath(currentNode)
            }
            this.closedSet[currentNode!!.getLongHash()] = currentNode
            cachedWorld.getNeighbourSections(currentNode.cachedSection).forEach{
                val node = PathNode(it, this.distanceTo(startNode.cachedSection, it), this.distanceTo(it, endNode.cachedSection))
                node.setParentNode(currentNode)
                if(this.closedSet[node.getLongHash()] == null){
                    this.openSet.add(node)
                }
            }
//            info("NewOpenSetSize: ${openSet.size}")
        }
        return emptyList()
    }

    private fun reconstructPath(endNode: PathNode): List<PathNode>{
        val path = mutableListOf<PathNode>()
        var currentNode = endNode
        while (currentNode.getParentNode() != null){
            path.add(currentNode)
            currentNode = currentNode.getParentNode()!!
        }
        return path.toList()
    }

    // Should Change in case i work on it in the future

    fun distanceTo(startSection: CachedSection, endSection: CachedSection): Double {
        val dx = endSection.x - startSection.x
        val dy = endSection.y - startSection.y
        val dz = endSection.z - startSection.z

        return sqrt((dx * dx + dy * dy + dz * dz).toDouble())
    }

    fun distanceTo(startNode: PathNode, endNode: PathNode): Double {
        return this.distanceTo(startNode.cachedSection, endNode.cachedSection)
    }
}