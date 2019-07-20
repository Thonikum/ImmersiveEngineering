/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.blocks;

import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.EventHandler;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ILightValue;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces.ISpawnInterdiction;
import blusunrize.immersiveengineering.common.blocks.metal.FloodlightTileEntity;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorldReader;

public class FakeLightBlock extends IETileProviderBlock
{
	public FakeLightBlock()
	{
		super("fake_light", Properties.create(Material.AIR), ItemBlockIEBase.class);
		setNotNormalBlock();
	}

	@Override
	public boolean isAir(BlockState state, IBlockReader world, BlockPos pos)
	{
		return true;
	}

	@Override
	public VoxelShape getShape(BlockState state, IBlockReader world, BlockPos pos)
	{
		return VoxelShapes.empty();
	}

	@Override
	public PushReaction getPushReaction(BlockState state)
	{
		return PushReaction.DESTROY;
	}

	@Override
	public boolean canBeReplacedByLeaves(BlockState state, IWorldReader world, BlockPos pos)
	{
		return true;
	}

	@Override
	public TileEntity createBasicTE(BlockState state)
	{
		return new FakeLightTileEntity();
	}

	public static class FakeLightTileEntity extends IEBaseTileEntity implements ITickableTileEntity, ISpawnInterdiction, ILightValue
	{
		public static TileEntityType<FakeLightTileEntity> TYPE;

		public int[] floodlightCoords = {-1, -1, -1};

		public FakeLightTileEntity()
		{
			super(TYPE);
			if(IEConfig.Machines.floodlight_spawnPrevent)
				synchronized(EventHandler.interdictionTiles)
				{
					if(!EventHandler.interdictionTiles.contains(this))
						EventHandler.interdictionTiles.add(this);
				}
		}

		@Override
		public void tick()
		{
			if(world.getGameTime()%256==((getPos().getX()^getPos().getZ())&255))
			{
				if(floodlightCoords==null||floodlightCoords.length < 3)
				{
					world.removeBlock(getPos(), false);
					return;
				}
				BlockPos floodlightPos = new BlockPos(floodlightCoords[0], floodlightCoords[1], floodlightCoords[2]);
				TileEntity tile = Utils.getExistingTileEntity(world, floodlightPos);
				if(!(tile instanceof FloodlightTileEntity)||!((FloodlightTileEntity)tile).active)
				{
					world.removeBlock(getPos(), false);
					return;
				}
			}

		}

		@Override
		public int getLightValue()
		{
			return 15;
		}

		@Override
		public double getInterdictionRangeSquared()
		{
			return 1024;
		}

		@Override
		public void remove()
		{
			synchronized(EventHandler.interdictionTiles)
			{
				EventHandler.interdictionTiles.remove(this);
			}
			super.remove();
		}

		@Override
		public void onChunkUnloaded()
		{
			synchronized(EventHandler.interdictionTiles)
			{
				EventHandler.interdictionTiles.remove(this);
			}
			super.onChunkUnloaded();
		}

		@Override
		public void readCustomNBT(CompoundNBT nbt, boolean descPacket)
		{
			floodlightCoords = nbt.getIntArray("floodlightCoords");
		}

		@Override
		public void writeCustomNBT(CompoundNBT nbt, boolean descPacket)
		{
			nbt.putIntArray("floodlightCoords", floodlightCoords);

		}
	}
}