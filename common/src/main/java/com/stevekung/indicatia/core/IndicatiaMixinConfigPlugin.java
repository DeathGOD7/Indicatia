package com.stevekung.indicatia.core;

import java.util.List;
import java.util.Set;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import com.stevekung.stevekungslib.utils.LoggerBase;

public class IndicatiaMixinConfigPlugin implements IMixinConfigPlugin
{
    static final LoggerBase LOGGER = new LoggerBase("Indicatia MixinConfig");
    static boolean foundOptifine;

    static
    {
        foundOptifine = findAndDetectModClass("net/optifine/Config.class", "OptiFine");
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig()
    {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName)
    {
        if (mixinClassName.equals("com.stevekung.indicatia.mixin.optifine.renderer.entity.layers.MixinBipedArmorLayerOptifine") || mixinClassName.equals("com.stevekung.indicatia.mixin.optifine.renderer.tileentity.MixinItemStackTileEntityRendererOptifine"))
        {
            return foundOptifine;
        }
        else if (mixinClassName.equals("com.stevekung.indicatia.mixin.renderer.entity.layers.MixinBipedArmorLayer") || mixinClassName.equals("com.stevekung.indicatia.mixin.renderer.tileentity.MixinItemStackTileEntityRenderer"))
        {
            return !foundOptifine;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins()
    {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    private static boolean findAndDetectModClass(String classPath, String modName)
    {
        boolean found = Thread.currentThread().getContextClassLoader().getResourceAsStream(classPath) != null;
        LOGGER.info(found ? modName + " detected!" : modName + " not detected!");
        return found;
    }
}