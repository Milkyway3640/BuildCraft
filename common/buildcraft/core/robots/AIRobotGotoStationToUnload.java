/**
 * Copyright (c) 2011-2014, SpaceToad and the BuildCraft Team
 * http://www.mod-buildcraft.com
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public
 * License 1.0, or MMPL. Please check the contents of the license located in
 * http://www.mod-buildcraft.com/MMPL-1.0.txt
 */
package buildcraft.core.robots;

import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;

import net.minecraftforge.common.util.ForgeDirection;

import buildcraft.api.core.IBox;
import buildcraft.api.robots.AIRobot;
import buildcraft.api.robots.EntityRobotBase;
import buildcraft.core.inventory.ITransactor;
import buildcraft.core.inventory.Transactor;
import buildcraft.core.inventory.filters.ArrayStackFilter;
import buildcraft.silicon.statements.StateStationRequestItems;
import buildcraft.transport.Pipe;

public class AIRobotGotoStationToUnload extends AIRobot {

	private IBox box;

	public AIRobotGotoStationToUnload(EntityRobotBase iRobot, IBox iBox) {
		super(iRobot, 0);

		box = iBox;

	}

	@Override
	public void start() {
		startDelegateAI(new AIRobotLookForStation(robot, new StationInventory(), box));
	}

	@Override
	public void delegateAIEnded(AIRobot ai) {
		terminate();
	}

	private class StationInventory implements IStationFilter {
		@Override
		public boolean matches(DockingStation station) {
			Pipe pipe = station.pipe.pipe;

			for (int i = 0; i < robot.getSizeInventory(); ++i) {
				boolean found = false;
				ItemStack stackToAdd = robot.getStackInSlot(i);

				if (stackToAdd == null) {
					continue;
				}

				for (Object s : pipe.getActionStates()) {
					if (s instanceof StateStationRequestItems) {
						if (((StateStationRequestItems) s).matches(new ArrayStackFilter(stackToAdd))) {
							found = true;
							break;
						}
					}
				}

				if (!found) {
					// This stack is not accepted by any of the action states
					// currently active on this pipe - look for another one
					continue;
				}

				for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
					TileEntity nearbyTile = robot.worldObj.getTileEntity(station.x() + dir.offsetX, station.y()
							+ dir.offsetY, station.z()
							+ dir.offsetZ);

					if (nearbyTile != null && nearbyTile instanceof IInventory) {
						ITransactor trans = Transactor.getTransactorFor(nearbyTile);

						if (stackToAdd != null && trans.add(stackToAdd, dir, false) != null) {
							// We can add the item to this inventory, go to this
							// station.

							return true;
						}

					}
				}

			}

			return false;
		}
	}

}
