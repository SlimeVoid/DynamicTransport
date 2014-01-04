/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version. This program is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General
 * Public License for more details. You should have received a copy of the GNU
 * Lesser General Public License along with this program. If not, see
 * <http://www.gnu.org/licenses/>
 */
package slimevoid.dynamictransport.client.tickhandler;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import slimevoid.dynamictransport.entities.EntityElevator;
import cpw.mods.fml.common.ITickHandler;
import cpw.mods.fml.common.TickType;

public class PlayerMotionTickHandler implements ITickHandler {

	@Override
	public void tickStart(EnumSet<TickType> type, Object... tickData) {
		if (type.equals(EnumSet.of(TickType.PLAYER))) {
			EntityPlayer entityplayer = (EntityPlayer) tickData[0];
			if (entityplayer.boundingBox != null) {
				World world = entityplayer.worldObj;
				Set<Entity> potentialEntities = new HashSet<Entity>();

				potentialEntities.addAll(world.getEntitiesWithinAABBExcludingEntity(entityplayer,
																					entityplayer.boundingBox.copy().offset(	0,
																															-.5,
																															0)));
				for (Entity entity : potentialEntities) {
					if (entity instanceof EntityElevator) {
						EntityElevator elevator = (EntityElevator) entity;
						if (elevator.getBoundingBox().maxY + .025
							- entityplayer.boundingBox.minY >= 0) {
							entityplayer.motionY = Math.max(elevator.getBoundingBox().maxY
																	+ .025
																	- entityplayer.boundingBox.minY,
															entityplayer.motionY);
							entityplayer.onGround = true;
							entityplayer.fallDistance = 0;
							return;
						}
					}
				}
			}
			// this will tick on both server and client
		}
	}

	@Override
	public void tickEnd(EnumSet<TickType> type, Object... tickData) {

	}

	@Override
	public EnumSet<TickType> ticks() {
		return EnumSet.of(TickType.PLAYER);
	}

	@Override
	public String getLabel() {
		return "MinersHelmetHandler";
	}

}
