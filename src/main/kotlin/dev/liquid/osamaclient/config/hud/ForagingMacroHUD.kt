package dev.liquid.osamaclient.config.hud

import cc.polyfrost.oneconfig.hud.Hud
import cc.polyfrost.oneconfig.libs.universal.UMatrixStack
import cc.polyfrost.oneconfig.renderer.TextRenderer
import dev.liquid.osamaclient.util.mc
import lombok.ToString.Exclude
import net.minecraft.util.StringUtils
import net.minecraftforge.client.event.ClientChatReceivedEvent
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent
import java.awt.Color

class ForagingMacroHUD : Hud() {
  @Exclude
  val exampleText = "Example: AJAJJAJA"
  override fun draw(matrices: UMatrixStack?, x: Float, y: Float, scale: Float, example: Boolean) {
    var textToDraw = "Default Text"
    if (example) {
      textToDraw = exampleText
    }
    TextRenderer.drawScaledString(textToDraw, x, y, Color.CYAN.rgb, TextRenderer.TextType.toType(1), scale)
  }

  override fun getWidth(scale: Float, example: Boolean): Float {
    return mc.fontRendererObj.getStringWidth(exampleText) * scale
  }

  override fun getHeight(scale: Float, example: Boolean): Float {
    return 20f
  }

  override fun shouldShow(): Boolean {
    return true
  }

  @SubscribeEvent
  fun onChat(event: ClientChatReceivedEvent){
    if(event.type.toInt() != 2) return
    val message = StringUtils.stripControlCodes(event.message.unformattedText)
    if(!message.contains("%)")) return
//    LogUtil.note(message)
  }
}