package com.stevekung.indicatia.fabric.mixin.gui;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;
import com.mojang.blaze3d.vertex.PoseStack;
import com.stevekung.indicatia.core.Indicatia;
import com.stevekung.indicatia.utils.RenderUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;

@Mixin(Gui.class)
public class MixinGui
{
    @Shadow
    @Final
    Minecraft minecraft;

    @Inject(method = "renderEffects", at = @At(value = "INVOKE", target = "java/util/List.add(Ljava/lang/Object;)Z", shift = At.Shift.AFTER, remap = false), locals = LocalCapture.CAPTURE_FAILSOFT)
    private void indicatia$addPotionTime(PoseStack poseStack, CallbackInfo info, Collection<MobEffectInstance> collection, int i, int j, MobEffectTextureManager mobEffectTextureManager, List<Runnable> list, Iterator<MobEffectInstance> iterator, MobEffectInstance mobEffectInstance, MobEffect mobEffect, int x, int y, float alpha, TextureAtlasSprite textureAtlasSprite, int n, int o, float g)
    {
        if (Indicatia.CONFIG.timeOnVanillaPotionHUD)
        {
            list.add(() -> RenderUtils.renderDurationTRPotion(this.minecraft, poseStack, mobEffectInstance, x, y, alpha));
        }
    }
}