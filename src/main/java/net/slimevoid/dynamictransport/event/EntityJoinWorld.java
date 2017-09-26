package net.slimevoid.dynamictransport.event;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.slimevoid.dynamictransport.entities.EntityElevatorPart;

public class EntityJoinWorld {
	@SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent event) {
    	if (event.entity instanceof EntityElevatorPart) {
    		((EntityElevatorPart) event.entity).checkFlag();
    	}
    }
}
