package dev.liquid.osamaclient.util

import dev.liquid.osamaclient.feature.implementation.helper.Angle
import net.minecraft.entity.Entity
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import net.minecraft.util.MathHelper
import net.minecraft.util.Vec3
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.pow
import kotlin.math.sqrt

object AngleUtil {
  val PLAYER_ANGLE get() = Angle(normalizeAngle(player.rotationYaw), player.rotationPitch)

  fun yawTo360(yaw: Float): Float {
    return (((yaw % 360) + 360) % 360)
  }

  private fun normalizeAngle(yaw: Float): Float {
    var convertedYaw = yaw % 360.0f
    if (convertedYaw > 180.0f) {
      convertedYaw -= 360.0f
    } else if (convertedYaw < -180.0f) {
      convertedYaw += 360.0f
    }
    return convertedYaw
  }

  fun getClosest90(): Float {
    var normalizedYaw = yawTo360(player.rotationYaw)
    val remainder = normalizedYaw % 90
    if (remainder < 45) {
      normalizedYaw -= remainder
    } else {
      normalizedYaw += 90 - remainder
    }
    return normalizedYaw
  }

  fun getClosestActual90(): Float {
    return normalizeAngle(getClosest90())
  }

  fun getYawFromDirection(facing: EnumFacing): Float{
    return when(facing){
      EnumFacing.NORTH -> 180f
      EnumFacing.SOUTH -> 0f
      EnumFacing.EAST -> -90f
      else -> 90f
    }
  }

  fun getAngle(endVec: Vec3, startVec: Vec3 = Vec3(player.posX, player.posY + player.eyeHeight, player.posZ)): Angle {
    val dX = endVec.xCoord - startVec.xCoord
    val dY = endVec.yCoord - startVec.yCoord
    val dZ = endVec.zCoord - startVec.zCoord

    val yaw = -Math.toDegrees(atan2(dX, dZ)).toFloat()
    val pitch = -Math.toDegrees(atan2(dY, sqrt(dX * dX + dZ * dZ))).toFloat()
    return Angle(this.reduceTrailingPointsTo(yaw, 2), this.reduceTrailingPointsTo(pitch, 2))
  }

  fun getAngle(blockPos: BlockPos): Angle {
    return getAngle(BlockUtil.getClosestSidePos(blockPos))
  }

  fun getAngle(entity: Entity, height: Float = 1.5f): Angle {
    return getAngle(entity.positionVector.addVector(0.0, height.toDouble(), 0.0))
  }

  fun calculateNeededAngleChange(entity: Entity, height: Float = 1.5f): Angle {
    val end = entity.positionVector.addVector(0.0, height.toDouble(), 0.0)
    return calculateNeededAngleChange(end)
  }

  fun calculateNeededAngleChange(blockPos: BlockPos): Angle {
    return calculateNeededAngleChange(BlockUtil.getClosestSidePos(blockPos))
  }

  fun calculateNeededAngleChange(vecPos: Vec3): Angle {
    val end = getAngle(vecPos)
    return calculateNeededAngleChange(PLAYER_ANGLE, end)
  }

  fun calculateNeededAngleChange(startRot: Angle, endRot: Angle): Angle {
    var yawChange = MathHelper.wrapAngleTo180_float(endRot.yaw) - MathHelper.wrapAngleTo180_float(startRot.yaw)
    if (yawChange <= -180.0f) yawChange += 360.0f else if (yawChange > 180.0f) yawChange += -360.0f
    return Angle(yawChange, endRot.pitch - startRot.pitch)
  }

  fun reduceTrailingPointsTo(value: Float, number: Int = 2): Float {
    val multiplier = 10f.pow(number)
    return (value * multiplier).toInt() / multiplier
  }

  fun isWithinAngleThreshold(
    blockPos: BlockPos,
    maxAllowedYawDifference: Float,
    maxAllowedPitchDifference: Float
  ): Boolean {
    return isWithinAngleThreshold(
      BlockUtil.bestPointOnBlock(blockPos),
      maxAllowedYawDifference,
      maxAllowedPitchDifference
    )
  }

  fun isWithinAngleThreshold(
    entity: Entity,
    maxAllowedYawDifference: Float,
    maxAllowedPitchDifference: Float,
    height: Float = 1.5f
  ): Boolean {
    return isWithinAngleThreshold(
      entity.positionVector.addVector(0.0, height.toDouble(), 0.0),
      maxAllowedYawDifference,
      maxAllowedPitchDifference
    )
  }

  fun isWithinAngleThreshold(
    targetPosition: Vec3,
    maxAllowedYawDifference: Float,
    maxAllowedPitchDifference: Float
  ): Boolean {
    val neededAngleChange = calculateNeededAngleChange(targetPosition)
    return abs(neededAngleChange.yaw) < maxAllowedYawDifference && abs(neededAngleChange.pitch) < maxAllowedPitchDifference
  }

  fun isWithinAngleThreshold(
    targetAngle: Angle,
    maxAllowedYawDifference: Float,
    maxAllowedPitchDifference: Float
  ): Boolean {
    val neededAngleChange = calculateNeededAngleChange(PLAYER_ANGLE, targetAngle)
    return abs(neededAngleChange.yaw) < maxAllowedYawDifference && abs(neededAngleChange.pitch) < maxAllowedPitchDifference
  }
}
