package dev.liquid.osamaclient.feature.implementation.general

import dev.liquid.osamaclient.feature.implementation.helper.Angle
import dev.liquid.osamaclient.feature.implementation.helper.Target
import dev.liquid.osamaclient.util.AngleUtil
import dev.liquid.osamaclient.util.EaseUtil
import dev.liquid.osamaclient.util.LogUtil
import dev.liquid.osamaclient.util.player
import net.minecraftforge.client.event.RenderWorldLastEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent

class Rotation {
  private var enabled = false
  private var start: Target? = null
  private var target: Target? = null
  private var startTime = 0L
  private var endTime = 0L
  private var hasChanged = false
  private var cAngle: Angle? = null
  fun easeTo(target: Target, time: Int) {
    this.enabled = true
    this.target = target
    this.start = Target(AngleUtil.PLAYER_ANGLE)
    this.startTime = System.currentTimeMillis()
//    this.cAngle = Angle(this.target!!.getAngle().yaw - this.start!!.getAngle().yaw, this.target!!.getAngle().yaw - this.target!!.getAngle().pitch)
    this.cAngle = Angle(45f, 0f)
    this.endTime = startTime + time
  }

  fun disable() {
    this.enabled = false
    this.target = null
    this.start = null
    this.startTime = 0L
    this.endTime = 0L
    cAngle = null
    hasChanged = false
  }


  @SubscribeEvent
  fun onRender(event: RenderWorldLastEvent) {
    if (!enabled) return
    var progres = (System.currentTimeMillis() - startTime) / (endTime - startTime).toFloat()
    if(progres > .2 && !hasChanged){
      this.hasChanged = true
      this.cAngle = this.target!!.getAngle()
      this.target = Target(Angle(90f, 30f))
      this.endTime += 300
    }
    if (progres > 1) progres = 1f
    getYP(EaseUtil.easeOutCubic(progres))
    if (progres >= 1) disable()
  }

  fun getYP(progress: Float) {
    val sAngle = start!!.getAngle()
    val tAngle = target!!.getAngle()
//    val yaw = (1 - progress) * sAngle.yaw + progress * tAngle.yaw
//    val pitch = (1 - progress) * sAngle.pitch + progress * tAngle.pitch
    LogUtil.log("ELLO")
    val pLeft = 1 - progress
    val yaw = pLeft * pLeft * sAngle.yaw + 2 * pLeft * progress * cAngle!!.yaw + progress * progress * tAngle.yaw
    val pitch = pLeft * pLeft * sAngle.pitch + 2 * pLeft * progress * cAngle!!.pitch + progress * progress * tAngle.pitch
    player.rotationYaw = yaw
    player.rotationPitch = pitch
  }
}