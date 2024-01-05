package dev.liquid.osamaclient.util

import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MovingObjectPosition
import net.minecraft.util.Vec3
import kotlin.math.abs

object RaytracingUtil {
  fun getValidSide(block: BlockPos): EnumFacing? {
    if (!world.isBlockFullCube(block)) return null
    var dist = Float.MAX_VALUE
    var face: EnumFacing? = null
    for (side in BlockUtil.BLOCK_SIDES.keys) {
      val distanceToSide = this.getPlayerHeadPosition().distanceTo(BlockUtil.getSidePos(block, side)).toFloat()
      if (canSeeSide(block, side) && distanceToSide < dist) {
        if (side == null && face != null) continue
        dist = distanceToSide
        face = side
      }
    }
    return face
  }

  fun validSides(block: BlockPos): MutableList<EnumFacing?> {
    val validSide = mutableListOf<EnumFacing?>()
    for (face in BlockUtil.BLOCK_SIDES.keys) {
      if (canSeeSide(block, face)) validSide.add(face)
    }
    return validSide
  }

  private fun canSeeSide(block: BlockPos, side: EnumFacing?): Boolean {
    val i = BlockUtil.BLOCK_SIDES[side]!!
    val endVec = Vec3((block.x + i[0]).toDouble(), (block.y + i[1]).toDouble(), (block.z + i[2]).toDouble())
    return canSeePoint(endVec)
  }

  fun canSeePoint(position: Vec3): Boolean {
    val startVec = this.getPlayerHeadPosition()
    val result = raytrace(startVec, position) ?: return false
    if (result.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK) return false
    val r = result.hitVec
    return abs(r.xCoord - position.xCoord) < .1f && abs(r.yCoord - position.yCoord) < .1f && abs(r.zCoord - position.zCoord) < .1f
  }

  fun getBlockLookingAt(distance: Float): BlockPos? {
    val playerEye = player.getPositionEyes(1f)
    val lookVec = player.lookVec
    val endPos = Vec3(
      playerEye.xCoord + lookVec.xCoord * distance,
      playerEye.yCoord + lookVec.yCoord * distance,
      playerEye.zCoord + lookVec.zCoord * distance
    )
    val result = world.rayTraceBlocks(playerEye, endPos)
    return result?.blockPos
  }

  fun raytrace(v1: Vec3, v2: Vec3): MovingObjectPosition? {
    val v3 = v2.subtract(v1)
    val entities = world.getEntitiesInAABBexcluding(
      player,
      (player.entityBoundingBox.addCoord(v3.xCoord, v3.yCoord, v3.zCoord).expand(1.0, 1.0, 1.0))
    )
    { it!!.isEntityAlive && it.canBeCollidedWith() }.sortedBy { player.getDistanceToEntity(it) }
    for (entity in entities) {
      val intercept = entity.entityBoundingBox.expand(0.5, 0.5, 0.5).calculateIntercept(v1, v2)
      if (intercept != null) {
        return MovingObjectPosition(entity, intercept.hitVec)
      }
    }
    return world.rayTraceBlocks(v1, v2, false, true, false)
  }

  private fun getPlayerHeadPosition(): Vec3{
    return player.positionVector.addVector(0.0, player.eyeHeight.toDouble(), 0.0)
  }
}
