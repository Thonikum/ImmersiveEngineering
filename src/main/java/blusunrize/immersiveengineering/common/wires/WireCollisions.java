/*
 * BluSunrize
 * Copyright (c) 2017
 *
 * This code is licensed under "Blu's License of Common Sense"
 * Details can be found in the license file in the root folder of this project
 */

package blusunrize.immersiveengineering.common.wires;

import blusunrize.immersiveengineering.api.ApiUtils;
import blusunrize.immersiveengineering.api.energy.wires.Connection;
import blusunrize.immersiveengineering.api.energy.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.energy.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.energy.wires.WireCollisionData;
import blusunrize.immersiveengineering.api.energy.wires.WireCollisionData.CollisionInfo;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.ICollisionHandler;
import blusunrize.immersiveengineering.api.energy.wires.localhandlers.LocalNetworkHandler;
import blusunrize.immersiveengineering.common.Config.IEConfig;
import blusunrize.immersiveengineering.common.util.IEDamageSources;
import blusunrize.immersiveengineering.common.util.Utils;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorldEventListener;
import net.minecraft.world.World;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collection;

public class WireCollisions implements IWorldEventListener
{
	public static void handleEntityCollision(BlockPos p, Entity e)
	{
		if(!e.world.isRemote&&IEConfig.enableWireDamage&&e instanceof LivingEntity&&
				!e.isEntityInvulnerable(IEDamageSources.wireShock)&&
				!(e instanceof PlayerEntity&&((PlayerEntity)e).capabilities.disableDamage))
		{
			GlobalWireNetwork global = GlobalWireNetwork.getNetwork(e.world);
			WireCollisionData wireData = global.getCollisionData();
			Collection<WireCollisionData.CollisionInfo> atBlock = wireData.getCollisionInfo(p);
			if(atBlock!=null)
				for(WireCollisionData.CollisionInfo info : atBlock)
				{
					LocalWireNetwork local = info.getLocalNet();
					for(LocalNetworkHandler h : local.getAllHandlers())
						if(h instanceof ICollisionHandler)
							((ICollisionHandler)h).onCollided((LivingEntity)e, p, info);
				}
		}
	}

	@Override
	public void notifyBlockUpdate(@Nonnull World worldIn, @Nonnull BlockPos pos, @Nonnull BlockState oldState, @Nonnull BlockState newState, int flags)
	{
		if(!worldIn.isRemote&&(flags&1)!=0&&newState.getBlock().canCollideCheck(newState, false))
		{
			GlobalWireNetwork globalNet = GlobalWireNetwork.getNetwork(worldIn);
			Collection<CollisionInfo> data = globalNet.getCollisionData().getCollisionInfo(pos);
			if(data!=null)
			{
				Collection<Pair<Connection, BlockPos>> toBreak = new ArrayList<>();
				for(CollisionInfo info : data)
					if(info.isInBlock)
					{
						Vec3d vecA = info.conn.getPoint(0, info.conn.getEndA());
						if(Utils.isVecInBlock(vecA, pos, info.conn.getEndA().getPosition(), 1e-3))
							continue;
						Vec3d vecB = info.conn.getPoint(0, info.conn.getEndB());
						if(Utils.isVecInBlock(vecB, pos, info.conn.getEndB().getPosition(), 1e-3))
							continue;
						BlockPos dropPos = pos;
						if(ApiUtils.preventsConnection(worldIn, pos, newState, info.intersectA, info.intersectB))
						{
							for(Direction f : Direction.VALUES)
								if(worldIn.isAirBlock(pos.offset(f)))
								{
									dropPos = dropPos.offset(f);
									break;
								}
							toBreak.add(new ImmutablePair<>(info.conn, dropPos));
						}
					}
				for(Pair<Connection, BlockPos> b : toBreak)
					globalNet.removeAndDropConnection(b.getLeft(), b.getRight());
			}
		}
	}

	@Override
	public void notifyLightSet(@Nonnull BlockPos pos)
	{
	}

	@Override
	public void markBlockRangeForRenderUpdate(int x1, int y1, int z1, int x2, int y2, int z2)
	{
	}

	@Override
	public void playSoundToAllNearExcept(PlayerEntity player, @Nonnull SoundEvent soundIn, @Nonnull SoundCategory category, double x, double y, double z, float volume, float pitch)
	{
	}

	@Override
	public void playRecord(@Nonnull SoundEvent soundIn, @Nonnull BlockPos pos)
	{
	}

	@Override
	public void addParticle(int particleID, boolean ignoreRange, double xCoord, double yCoord, double zCoord, double xSpeed, double ySpeed, double zSpeed, @Nonnull int... parameters)
	{
	}

	@Override
	public void addParticle(int id, boolean ignoreRange, boolean p_190570_3_, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, @Nonnull int... parameters)
	{
	}

	@Override
	public void onEntityAdded(@Nonnull Entity entityIn)
	{
	}

	@Override
	public void onEntityRemoved(@Nonnull Entity entityIn)
	{
	}

	@Override
	public void broadcastSound(int soundID, @Nonnull BlockPos pos, int data)
	{
	}

	@Override
	public void playEvent(PlayerEntity player, int type, @Nonnull BlockPos blockPosIn, int data)
	{
	}

	@Override
	public void sendBlockBreakProgress(int breakerId, @Nonnull BlockPos pos, int progress)
	{
	}
}
