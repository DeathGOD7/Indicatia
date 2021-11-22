package com.stevekung.indicatia.forge.mixin.gui;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.client.gui.ForgeIngameGui;

@Mixin(ForgeIngameGui.class)
public class MixinForgeIngameGui
{
    @Inject(method = "renderChat", at = @At(value = "INVOKE", target = "com/mojang/blaze3d/vertex/PoseStack.translate(DDD)V", shift = At.Shift.AFTER))
    private void indicatia$fixChatBefore(int width, int height, PoseStack poseStack, CallbackInfo info)
    {
        RenderSystem.disableDepthTest();
    }

    @Inject(method = "renderChat", at = @At(value = "INVOKE", target = "com/mojang/blaze3d/vertex/PoseStack.popPose()V", shift = At.Shift.BEFORE))
    private void indicatia$fixChatAfter(int width, int height, PoseStack poseStack, CallbackInfo info)
    {
        RenderSystem.enableDepthTest();
    }
}