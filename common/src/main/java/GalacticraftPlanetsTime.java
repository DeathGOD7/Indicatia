//package com.stevekung.indicatia.integration;
//
//import com.stevekung.indicatia.config.ExtendedConfig;
//import com.stevekung.indicatia.hud.InfoOverlay;
//import com.stevekung.stevekungslib.utils.LangUtils;
//
//import micdoodle8.mods.galacticraft.api.prefab.world.gen.WorldProviderSpace;
//import net.minecraft.client.Minecraft;
//
//public class GalacticraftPlanetsTime
//{
//    public static InfoOverlay getSpaceTime(Minecraft mc)
//    {
//        if (!(mc.world.dimension instanceof WorldProviderSpace))
//        {
//            return null;
//        }
//
//        WorldProviderSpace space = (WorldProviderSpace) mc.world.dimension;
//        long dayLength = space.getDayLength();
//        StringBuilder builder = new StringBuilder();
//        String celestialName = space.getCelestialBody().getLocalizedName();
//        long spaceWorldTime = space.getWorldTime() % dayLength;
//        long worldTimeDivide = dayLength / 24;
//        int hours = (int)((spaceWorldTime / worldTimeDivide + 6) % 24);
//        int minutes = (int)(60 * (spaceWorldTime % worldTimeDivide) / worldTimeDivide);
//
//        if (dayLength >= 1L && dayLength <= 24L)
//        {
//            return new InfoOverlay(celestialName, LangUtils.translateComponent("hud.time.galacticraft.fastest_dn_cycle").getString(), ExtendedConfig.INSTANCE.gameTimeColor, ExtendedConfig.INSTANCE.gameTimeValueColor, InfoOverlay.Position.RIGHT);
//        }
//        else if (dayLength == 0L)
//        {
//            return new InfoOverlay(celestialName, LangUtils.translateComponent("hud.time.galacticraft.no_dn_cycle").getString(), ExtendedConfig.INSTANCE.gameTimeColor, ExtendedConfig.INSTANCE.gameTimeValueColor, InfoOverlay.Position.RIGHT);
//        }
//
//        if (hours <= 9)
//        {
//            builder.append(0);
//        }
//
//        builder.append(hours);
//        builder.append(":");
//
//        if (minutes <= 9)
//        {
//            builder.append(0);
//        }
//        builder.append(minutes);
//        builder.append(" " + (hours >= 12 ? "PM" : "AM"));
//        return new InfoOverlay(celestialName, builder.toString(), ExtendedConfig.INSTANCE.gameTimeColor, ExtendedConfig.INSTANCE.gameTimeValueColor, InfoOverlay.Position.RIGHT);
//    }
//}