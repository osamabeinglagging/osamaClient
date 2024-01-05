package dev.liquid.osamaclient.feature.implementation.general

import dev.liquid.osamaclient.event.PacketEvent
import dev.liquid.osamaclient.feature.AbstractFeature
import dev.liquid.osamaclient.feature.implementation.helper.Angle
import dev.liquid.osamaclient.feature.implementation.helper.Target
import dev.liquid.osamaclient.util.AngleUtil
import dev.liquid.osamaclient.util.EaseUtil
import dev.liquid.osamaclient.util.player
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import net.minecraftforge.fml.common.gameevent.TickEvent
import kotlin.math.abs

class AutoRotation : AbstractFeature() {
  override val featureName: String = "AutoRotation"
  override val isPassiveFeature: Boolean = false

  private val DEFAULT_DYNAMIC_TIME = 400

  private var easeFunction: ((Float) -> Float)? = null

  private var target: Target? = null
  private var startAngle: Angle? = null

  private var endTime = 0L
  private var startTime = 0L

  private var lockType: LockType = LockType.NONE
  private var smoothLockTime = 0

  companion object {
    private var instance: AutoRotation? = null
    fun getInstance(): AutoRotation {
      if (instance == null) {
        instance = AutoRotation()
      }
      return instance!!
    }
  }

  fun easeTo(
    target: Target,
    time: Int = 0,
    lockType: LockType = LockType.NONE,
    smoothLockTime: Int = 200,
    easeFunction: (Float) -> Float = EaseUtil.easingFunctions.random()
  ) {
    this.enabled = true
    this.forceEnable = true
    this.lockType = lockType
    this.smoothLockTime = smoothLockTime

    this.easeFunction = easeFunction
    this.startAngle = AngleUtil.PLAYER_ANGLE
    this.target = target

    this.failed = false
    this.succeeded = false

    this.startTime = System.currentTimeMillis()
    this.endTime = this.startTime + time
    if (time == 0) {
      this.endTime += getTime()
    }
  }


  private fun getTime(): Int {
    val (yaw, pitch) = AngleUtil.calculateNeededAngleChange(AngleUtil.PLAYER_ANGLE, target!!.getAngle())
    val change = abs(yaw) + abs(pitch)
    if (change < 25) {
      log("Very close rotation, speeding up by 0.65")
      return (DEFAULT_DYNAMIC_TIME * 0.65).toInt()
    }
    if (change < 45) {
      log("Close rotation, speeding up by 0.77")
      return (DEFAULT_DYNAMIC_TIME * 0.77).toInt()
    }
    if (change < 80) {
      log("Not so close, but not that far rotation, speeding up by 0.9")
      return (DEFAULT_DYNAMIC_TIME * 0.9).toInt()
    }
    if (change > 100) {
      log("Far rotation, slowing down by 1.1")
      return (DEFAULT_DYNAMIC_TIME * 1.1).toInt()
    }
    log("Normal rotation")
    return (DEFAULT_DYNAMIC_TIME * 1.0).toInt()
  }

  private fun changeAngle(yawChange: Float, pitchChange: Float) {
    val newYawChange = yawChange / 0.15f
    val newPitchChange = pitchChange / 0.15f
    player.setAngles(newYawChange, newPitchChange)
  }

  private fun interpolate(startAngle: Angle, endAngle: Angle) {
    val timeProgress = (System.currentTimeMillis() - this.startTime).toFloat() / (this.endTime - this.startTime)
    val totalNeededAngleProgress = this.easeFunction!!(timeProgress)
    val totalChange = AngleUtil.calculateNeededAngleChange(this.startAngle!!, endAngle)

    val currentYawProgress: Float = (player.rotationYaw - startAngle.yaw) / totalChange.yaw
    val currentPitchProgress: Float = (player.rotationPitch - startAngle.pitch) / totalChange.pitch
    val yawProgressThisFrame: Float = totalChange.yaw * (totalNeededAngleProgress - currentYawProgress)
    val pitchProgressThisFrame: Float = totalChange.pitch * (totalNeededAngleProgress - currentPitchProgress)

    this.changeAngle(
      AngleUtil.reduceTrailingPointsTo(yawProgressThisFrame, 2),
      -AngleUtil.reduceTrailingPointsTo(pitchProgressThisFrame, 2)
    )
  }

  override fun disable() {
    if (!this.enabled) return
    this.setSuccessStatus(AngleUtil.isWithinAngleThreshold(this.target!!.getAngle(), 1f, 1f))

    enabled = false
    forceEnable = false
    easeFunction = null

    startAngle = null
    target = null

    endTime = 0L
    startTime = 0L

    lockType = LockType.NONE
    smoothLockTime = 0
  }


  @SubscribeEvent
  override fun onRenderWorldLastEvent(event: RenderWorldLastEvent) {
    if (!this.enabled) return

    if (this.endTime >= System.currentTimeMillis()) {
      this.interpolate(this.startAngle!!, this.target!!.getAngle())
      return
    }
    if (lockType == LockType.NONE) {
      this.disable()
      return
    }

    if (lockType == LockType.INSTANT) {
      val angChange = AngleUtil.calculateNeededAngleChange(AngleUtil.PLAYER_ANGLE, this.target!!.getAngle())
      player.rotationYaw += angChange.yaw
      player.rotationPitch += angChange.pitch
    } else {
      this.easeTo(this.target!!, 200, LockType.SMOOTH, this.smoothLockTime)
    }
  }

  override fun onTick(event: TickEvent.ClientTickEvent) {
  }

  override fun onPacketReceive(event: PacketEvent.Received) {
  }

  override fun onChatReceive(event: ClientChatReceivedEvent) {
  }
}

enum class LockType { NONE, INSTANT, SMOOTH }
