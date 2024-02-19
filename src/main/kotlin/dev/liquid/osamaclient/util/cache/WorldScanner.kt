package dev.liquid.osamaclient.util.cache

import cc.polyfrost.oneconfig.utils.dsl.runAsync
import dev.liquid.osamaclient.mixin.MixinChunkProvider
import dev.liquid.osamaclient.util.LogUtil.info
import dev.liquid.osamaclient.util.cachedWorld
import dev.liquid.osamaclient.util.world
import net.minecraft.world.chunk.Chunk
import net.minecraftforge.event.world.ChunkEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.util.*
import kotlin.jvm.Throws


class WorldScanner {
    private val chunkQueue = LinkedList<Chunk>()
    private var enabled = false

    init {
        runAsync {
            try {
                while (true) {
                    if (this.chunkQueue.size == 0) {
                        Thread.sleep(2000)
                        continue
                    }
                    val chunk = this.chunkQueue.poll()
                    cachedWorld.pack(chunk)
                }
            }catch (e: Exception){
                info("Balled")
                println(e.stackTraceToString())
            }
        }
    }

    fun enable() {
        this.chunkQueue.addAll((world.chunkProvider as MixinChunkProvider).chunkListing)
        this.enabled = true

        info("WorldScanner Enabled")
    }

    fun disable() {
        this.enabled = false

        info("WorldScanner Disabled")
    }

    @SubscribeEvent
    fun onChunkLoad(event: ChunkEvent.Load) {
        if (!this.enabled) return
        info("ChunkLoaded. Size: ${chunkQueue.size}")
        this.chunkQueue.add(event.chunk)
    }
}