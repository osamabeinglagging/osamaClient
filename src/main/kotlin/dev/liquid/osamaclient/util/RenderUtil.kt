package dev.liquid.osamaclient.util

import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.client.renderer.RenderGlobal
import net.minecraft.client.renderer.Tessellator
import net.minecraft.client.renderer.vertex.DefaultVertexFormats
import net.minecraft.util.AxisAlignedBB
import net.minecraft.util.BlockPos
import net.minecraft.util.StringUtils
import net.minecraft.util.Vec3
import net.minecraftforge.client.event.RenderGameOverlayEvent
import org.lwjgl.opengl.GL11
import java.awt.Color
import kotlin.math.max
import kotlin.math.sqrt

// Copied from FarmHelper
// https://github.com/JellyLabScripts/FarmHelper/
object RenderUtil {

  fun drawAABB(aabb: AxisAlignedBB, color: Color){
    val d0 = mc.renderManager.viewerPosX
    val d1 = mc.renderManager.viewerPosY
    val d2 = mc.renderManager.viewerPosZ
    drawBox(aabb.offset(-d0, -d1, -d2), color)
  }

  fun drawPoint(point: Vec3, color: Color, pointSize: Int) {
    val radius = pointSize / 200.0
    val x = point.xCoord - radius
    val y = point.yCoord - radius
    val z = point.zCoord - radius
    val x2 = point.xCoord + radius
    val y2 = point.yCoord + radius
    val z2 = point.zCoord + radius
    val d0 = mc.renderManager.viewerPosX
    val d1 = mc.renderManager.viewerPosY
    val d2 = mc.renderManager.viewerPosZ
    drawBox(AxisAlignedBB(x, y, z, x2, y2, z2).offset(-d0, -d1, -d2), color)
  }

  fun drawBox(blockPos: BlockPos, color: Color) {
    val x = blockPos.x.toDouble()
    val y = blockPos.y.toDouble()
    val z = blockPos.z.toDouble()
    val x2 = x + 1
    val y2 = y + 1
    val z2 = z + 1
    val d0 = mc.renderManager.viewerPosX
    val d1 = mc.renderManager.viewerPosY
    val d2 = mc.renderManager.viewerPosZ
    drawBox(AxisAlignedBB(x, y, z, x2, y2, z2).offset(-d0, -d1, -d2), color)
  }

  fun drawBox(aabb: AxisAlignedBB, color: Color) {
    GlStateManager.pushMatrix()
    GlStateManager.enableBlend()
    GlStateManager.disableDepth()
    GlStateManager.disableLighting()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    GlStateManager.disableTexture2D()
    val tessellator = Tessellator.getInstance()
    val worldrenderer = tessellator.worldRenderer
    val a = color.alpha / 255.0f
    val r = color.red / 255.0f
    val g = color.green / 255.0f
    val b = color.blue / 255.0f
    GlStateManager.color(r, g, b, a)
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
    tessellator.draw()
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
    tessellator.draw()
    GlStateManager.color(r, g, b, a)
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
    tessellator.draw()
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
    tessellator.draw()
    GlStateManager.color(r, g, b, a)
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.minZ).endVertex()
    worldrenderer.pos(aabb.minX, aabb.minY, aabb.minZ).endVertex()
    tessellator.draw()
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos(aabb.minX, aabb.minY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.minY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.maxX, aabb.maxY, aabb.maxZ).endVertex()
    worldrenderer.pos(aabb.minX, aabb.maxY, aabb.maxZ).endVertex()
    tessellator.draw()
    GlStateManager.color(r, g, b, a)
    GL11.glLineWidth(2f)
    RenderGlobal.drawSelectionBoundingBox(aabb)
    GL11.glLineWidth(1.0f)
    GlStateManager.enableTexture2D()
    GlStateManager.enableDepth()
    GlStateManager.disableBlend()
    GlStateManager.resetColor()
    GlStateManager.popMatrix()
  }

  @JvmOverloads
  fun drawCenterTopText(text: String?, event: RenderGameOverlayEvent, color: Color, scale: Float = 3f) {
    val scaledResolution = event.resolution
    val scaledWidth = scaledResolution.scaledWidth
    GlStateManager.pushMatrix()
    GlStateManager.translate((scaledWidth / 2).toFloat(), 50f, 0.0f)
    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    GlStateManager.scale(scale, scale, scale)
    mc.fontRendererObj.drawString(
      text,
      -mc.fontRendererObj.getStringWidth(text) / 2f, 0f, color.rgb, true
    )
    GlStateManager.popMatrix()
  }

  fun drawText(str: String, x: Double, y: Double, z: Double, scale: Float) {
    var lScale = scale
    val fontRenderer = mc.fontRendererObj
    val renderPosX = x - mc.renderManager.viewerPosX
    val renderPosY = y - mc.renderManager.viewerPosY
    val renderPosZ = z - mc.renderManager.viewerPosZ
    val distance = sqrt(renderPosX * renderPosX + renderPosY * renderPosY + renderPosZ * renderPosZ)
    val multiplier = max(distance / 150f, 0.1)
    lScale *= (0.45f * multiplier).toFloat()
    val xMultiplier = (if (mc.gameSettings.thirdPersonView == 2) -1 else 1).toFloat()
    GlStateManager.pushMatrix()
    GlStateManager.translate(renderPosX, renderPosY, renderPosZ)
    val renderManager = mc.renderManager
    GlStateManager.rotate(-renderManager.playerViewY, 0f, 1f, 0f)
    GlStateManager.rotate(renderManager.playerViewX * xMultiplier, 1f, 0f, 0f)
    GlStateManager.scale(-lScale, -lScale, lScale)
    GlStateManager.disableLighting()
    GlStateManager.depthMask(false)
    GlStateManager.disableDepth()
    GlStateManager.enableBlend()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    val textWidth = fontRenderer.getStringWidth(StringUtils.stripControlCodes(str))
    val j = textWidth / 2f
    GlStateManager.disableTexture2D()
    val tessellator = Tessellator.getInstance()
    val worldrenderer = tessellator.worldRenderer
    GlStateManager.color(0f, 0f, 0f, 0.5f)
    worldrenderer.begin(7, DefaultVertexFormats.POSITION)
    worldrenderer.pos((-j - 1).toDouble(), -1.0, 0.0).endVertex()
    worldrenderer.pos((-j - 1).toDouble(), 8.0, 0.0).endVertex()
    worldrenderer.pos((j + 1).toDouble(), 8.0, 0.0).endVertex()
    worldrenderer.pos((j + 1).toDouble(), -1.0, 0.0).endVertex()
    tessellator.draw()
    GlStateManager.enableTexture2D()
    fontRenderer.drawString(str, -textWidth / 2, 0, 553648127)
    GlStateManager.depthMask(true)
    fontRenderer.drawString(str, -textWidth / 2, 0, -1)
    GlStateManager.enableDepth()
    GlStateManager.enableBlend()
    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    GlStateManager.popMatrix()
  }

  fun drawTracer(from: Vec3, to: Vec3, color: Color) {
    GlStateManager.pushMatrix()
    GlStateManager.enableBlend()
    GlStateManager.disableDepth()
    GlStateManager.disableLighting()
    GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0)
    GlStateManager.disableTexture2D()
    val renderManager = mc.renderManager
    val renderPosX = to.xCoord - renderManager.viewerPosX
    val renderPosY = to.yCoord - renderManager.viewerPosY
    val renderPosZ = to.zCoord - renderManager.viewerPosZ
    GL11.glLineWidth(1.5f)
    GL11.glColor4f(color.red / 255f, color.green / 255f, color.blue / 255f, color.alpha / 255f)
    GL11.glBegin(GL11.GL_LINES)
    GL11.glVertex3d(from.xCoord, from.yCoord, from.zCoord)
    GL11.glVertex3d(renderPosX, renderPosY, renderPosZ)
    GL11.glEnd()
    GL11.glLineWidth(1.0f)
    GlStateManager.enableTexture2D()
    GlStateManager.enableDepth()
    GlStateManager.disableBlend()
    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f)
    GlStateManager.resetColor()
    GlStateManager.popMatrix()
  }

  fun drawTracer(to: Vec3, color: Color) {
    drawTracer(Vec3(0.0, mc.thePlayer.getEyeHeight().toDouble(), 0.0), to, color)
  }
}