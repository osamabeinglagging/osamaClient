package dev.liquid.osamaclient.util.cache

class PathNode(val cachedSection: CachedSection, private val gCost: Double = 0.0, private val hCost: Double = 0.0) {
    private val fCost = this.gCost + this.hCost
    private var parentNode: PathNode? = null

    fun setParentNode(parentNode: PathNode) {
        this.parentNode = parentNode
    }

    fun getParentNode() = this.parentNode
    fun getFCost() = this.fCost
    fun getGCost() = this.gCost
    fun getHCost() = this.hCost

    // s kiddied from baritoe betterblockpos
    fun getLongHash(x: Int = this.cachedSection.x, y: Int = this.cachedSection.y, z: Int = this.cachedSection.z): Long {
        var hash: Long = 3241
        hash = 3457689L * hash + x
        hash = 8734625L * hash + y
        hash = 2873465L * hash + z
        return hash
    }

    override fun equals(other: Any?): Boolean {
        if (other !is PathNode) return false
        return other.cachedSection == this.cachedSection
    }

    override fun toString(): String {
        return "Cached: $cachedSection, fCost: $fCost, gCost: $gCost, hCost: $hCost"
    }
}