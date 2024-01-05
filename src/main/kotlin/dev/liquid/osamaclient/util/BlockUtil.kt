package dev.liquid.osamaclient.util

import net.minecraft.block.BlockChest
import net.minecraft.block.BlockDispenser
import net.minecraft.block.BlockDropper
import net.minecraft.block.BlockHopper
import net.minecraft.tileentity.TileEntityChest
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.Vec3
import net.minecraftforge.items.VanillaDoubleChestItemHandler
import kotlin.math.abs
import kotlin.random.Random


object BlockUtil {
  val BLOCK_SIDES = mapOf(
    EnumFacing.DOWN to floatArrayOf(0.5f, 0.01f, 0.5f),
    EnumFacing.UP to floatArrayOf(0.5f, 0.99f, 0.5f),
    EnumFacing.WEST to floatArrayOf(0.01f, 0.5f, 0.5f),
    EnumFacing.EAST to floatArrayOf(0.99f, 0.5f, 0.5f),
    EnumFacing.NORTH to floatArrayOf(0.5f, 0.5f, 0.01f),
    EnumFacing.SOUTH to floatArrayOf(0.5f, 0.5f, 0.99f),
    null to floatArrayOf(0.5f, 0.5f, 0.5f)
  )

  fun getClosestSidePos(block: BlockPos): Vec3 {
    return getSidePos(block, RaytracingUtil.getValidSide(block))
  }

  fun getSidePos(block: BlockPos, side: EnumFacing?): Vec3 {
    val i = BLOCK_SIDES[side]!!
    return Vec3((block.x + i[0]).toDouble(), (block.y + i[1]).toDouble(), (block.z + i[2]).toDouble())
  }

  fun neighbourGenerator(mainBlock: BlockPos, size: Int): List<BlockPos> {
    return neighbourGenerator(mainBlock, size, size, size)
  }

  private fun neighbourGenerator(mainBlock: BlockPos, xD: Int, yD: Int, zD: Int): List<BlockPos> {
    return neighbourGenerator(mainBlock, -xD, xD, -yD, yD, -zD, zD)
  }

  fun neighbourGenerator(
    mainBlock: BlockPos,
    xD1: Int,
    xD2: Int,
    yD1: Int,
    yD2: Int,
    zD1: Int,
    zD2: Int
  ): List<BlockPos> {
    val neighbours: MutableList<BlockPos> = ArrayList()
    for (x in xD1..xD2) {
      for (y in yD1..yD2) {
        for (z in zD1..zD2) {
          neighbours.add(BlockPos(mainBlock.x + x, mainBlock.y + y, mainBlock.z + z))
        }
      }
    }
    return neighbours
  }

  fun getPlayerDirectionToBeAbleToWalkOnBlock(startPos: BlockPos, endPoss: BlockPos): EnumFacing {
    val deltaX: Int = endPoss.x - startPos.x
    val deltaZ: Int = endPoss.z - startPos.z

    return if (abs(deltaX) > abs(deltaZ)) {
      if (deltaX > 0) EnumFacing.EAST else EnumFacing.WEST
    } else {
      if (deltaZ > 0) EnumFacing.SOUTH else EnumFacing.NORTH
    }
  }

  fun getRelativeBlock(x: Int, y: Int, z: Int, block: BlockPos): BlockPos {
    return when (mc.thePlayer.horizontalFacing) {
      EnumFacing.SOUTH -> block.add(-x, y, z)
      EnumFacing.NORTH -> block.add(x, y, -z)
      EnumFacing.EAST -> block.add(z, y, x)
      else -> block.add(-z, y, -x)
    }
  }

  fun bestPointOnBlock(block: BlockPos): Vec3 {
    return pointsOnBlockVisible(block).filter {
      RaytracingUtil.canSeePoint(it)
    }.minByOrNull {
      val angChange = AngleUtil.calculateNeededAngleChange(it)
      abs(angChange.yaw) + abs(angChange.pitch)
    } ?: Vec3(block).addVector(.5, .5, .5)
  }

  fun pointsOnBlockVisible(block: BlockPos): MutableList<Vec3> {
    val points = mutableListOf<Vec3>()
    RaytracingUtil.validSides(block).forEach {
      points.addAll(pointsOnBlockSide(block, it))
    }
    return points
  }

  private fun pointsOnBlockSide(block: BlockPos, side: EnumFacing?): MutableList<Vec3> {
    val points = mutableListOf<Vec3>()
    val it = BLOCK_SIDES[side]!!
    fun randomVal(): Float = (Random.nextInt(3, 7)) / 10f

    if (side != null) {
      for (i in 0 until 20) {
        var x = it[0]
        var y = it[1]
        var z = it[2]
        if (x == .5f) x = randomVal()
        if (y == .5f) y = randomVal()
        if (z == .5f) z = randomVal()
        val point = Vec3(block).addVector(x.toDouble(), y.toDouble(), z.toDouble())
        if (!points.contains(point)) points.add(point)
      }
    } else {
      for (bside in BLOCK_SIDES.values) {
        for (i in 0 until 20) {
          var x = bside[0]
          var y = bside[1]
          var z = bside[2]

          if (x == .5f) x = randomVal()
          if (y == .5f) y = randomVal()
          if (z == .5f) z = randomVal()

          val point = Vec3(block).addVector(x.toDouble(), y.toDouble(), z.toDouble())
          if (!points.contains(point)) points.add(point)
        }
      }
    }
    return points
  }

  // Foraging Macro Specific
  // Ty nirox and may2bee
  fun findChestsToRefill(): Pair<MutableList<BlockPos>, MutableList<BlockPos>> {
    val groups = mutableMapOf<EnumFacing, MutableList<BlockPos>>()
    val droppers = neighbourGenerator(player.getActualPosition().down(), -2, 2, -1, 1, -2, 2).filter {
      val state = world.getBlockState(it).block
      state is BlockDropper || state is BlockDispenser
    }
    val chests = mutableListOf<BlockPos>()
    droppers.forEach {
      val hopper = connectedHopperPosition(it)
      if (hopper != null) {
        findChestsConnectedToHopper(hopper, chests)
      }
    }

    while (chests.isNotEmpty()) {
      val main = chests[0]
      val connected = connectedChestPosition(main)
      if (connected == null) {
        chests.removeAt(0); continue
      }

      val distToMain = player.getDistanceSqToCenter(main)
      val distToConnected = player.getDistanceSqToCenter(connected)
      var itemToAdd = main
      if (distToConnected < distToMain) {
        itemToAdd = connected
      }
      groups.getOrPut(world.getBlockState(itemToAdd).getValue(BlockChest.FACING)){ mutableListOf() }.add(itemToAdd)
      chests.remove(main)
      chests.remove(connected)
    }

    val bonemealChests = mutableListOf<BlockPos>()
    val saplingChests = mutableListOf<BlockPos>()

    val faces = groups.keys.toMutableList()

    for (i in 0 until faces.size - 1) {
      for (j in i + 1 until faces.size) {
        if (faces[i].opposite == faces[j]) {
          saplingChests.addAll(groups[faces[i]]!!)
          saplingChests.addAll(groups[faces[j]]!!)
          bonemealChests.addAll(groups.entries.find { it.key != faces[i] && it.key != faces[j] }?.value ?: emptyList())
        }
      }
    }

    return Pair(saplingChests, bonemealChests)
  }

  private fun findChestsConnectedToHopper(hopper: BlockPos?, mutableList: MutableList<BlockPos>) {
    if (hopper == null) return
    if (world.getBlockState(hopper.up()).block !is BlockChest) return

    mutableList.add(hopper.up())
    findChestsConnectedToHopper(connectedHopperPosition(hopper.up()), mutableList)
  }

  private fun connectedHopperPosition(source: BlockPos): BlockPos? {
    for (x in -1..1) {
      for (z in -1..1) {
        if (x == 0 && z == 0) continue
        if (x != 0 && z != 0) continue
        val block = source.add(x, 0, z)
        val state = world.getBlockState(block)
        if (state.block !is BlockHopper) continue
        if (getPlayerDirectionToBeAbleToWalkOnBlock(block, source) == state.getValue(BlockHopper.FACING)) {
          return block
        }
      }
    }
    return null
  }

  fun connectedChestPosition(source: BlockPos): BlockPos? {
    val t = world.getTileEntity(source) as TileEntityChest
    val connected = VanillaDoubleChestItemHandler.get(t).get()
    return connected?.pos
  }

  fun findBoundsFromBlocks(vararg blocks: BlockPos): AxisAlignedBB {
    var minX = Int.MAX_VALUE
    var minY = Int.MAX_VALUE
    var minZ = Int.MAX_VALUE
    var maxX = Int.MIN_VALUE
    var maxY = Int.MIN_VALUE
    var maxZ = Int.MIN_VALUE

    for (block in blocks) {
      minX = minOf(minX, block.x)
      minY = minOf(minY, block.y)
      minZ = minOf(minZ, block.z)
      maxX = maxOf(maxX, block.x+1)
      maxY = maxOf(maxY, block.y+1)
      maxZ = maxOf(maxZ, block.z+1)
    }
    return AxisAlignedBB(minX.toDouble(), minY.toDouble(), minZ.toDouble(),
      maxX.toDouble(), maxY.toDouble(), maxZ.toDouble()
    )
  }
}
