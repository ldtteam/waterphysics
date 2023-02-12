package com.ldtteam.waterphysics;

import com.ldtteam.waterphysics.handlers.*;
import net.minecraftforge.fml.common.Mod;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


@Mod("waterphysics")
public class WaterPhysics
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "waterphysics";

    public WaterPhysics()
    {
        Mod.EventBusSubscriber.Bus.FORGE.bus().get().register(this.getClass());
        ITransformationHandler.HANDLERS.add(new WaterFlow());
    }
}
