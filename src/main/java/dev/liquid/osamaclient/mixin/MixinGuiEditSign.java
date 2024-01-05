package dev.liquid.osamaclient.mixin;

import dev.liquid.osamaclient.util.LogUtil;
import dev.liquid.osamaclient.util.helper.SignUtil;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.inventory.GuiEditSign;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C12PacketUpdateSign;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.ChatComponentText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// Credits farmhelper but they stole it from GTC so i'm not a skidder
@Mixin(GuiEditSign.class)
public abstract class MixinGuiEditSign extends GuiScreen {

    @Shadow
    private TileEntitySign tileSign;

    public MixinGuiEditSign(TileEntitySign tileEntitySign) {
        tileSign = tileEntitySign;
    }

    @Inject(method = "drawScreen", at = @At("RETURN"))
    private void initGui(CallbackInfo ci) {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (SignUtil.INSTANCE.getTextToWriteOnString().isEmpty()) return;

        LogUtil.INSTANCE.log("Sign text: " + SignUtil.INSTANCE.getTextToWriteOnString());
        tileSign.signText[0] = new ChatComponentText(SignUtil.INSTANCE.getTextToWriteOnString());

        NetHandlerPlayClient netHandlerPlayClient = mc.getNetHandler();
        if (netHandlerPlayClient != null) {
            netHandlerPlayClient.addToSendQueue(new C12PacketUpdateSign(tileSign.getPos(), tileSign.signText));
            SignUtil.INSTANCE.setTextToWriteOnString("");
            LogUtil.INSTANCE.log("Sign text set to empty.");
        }
    }
}