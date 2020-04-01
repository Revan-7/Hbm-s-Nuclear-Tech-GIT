package com.hbm.tileentity.machine;

import com.hbm.handler.MissileStruct;
import com.hbm.items.weapon.ItemCustomMissile;
import com.hbm.items.weapon.ItemMissile;
import com.hbm.items.weapon.ItemMissile.FuelType;
import com.hbm.items.weapon.ItemMissile.PartType;
import com.hbm.lib.HBMSoundHandler;
import com.hbm.packet.PacketDispatcher;
import com.hbm.packet.TEMissileMultipartPacket;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ITickable;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.common.network.NetworkRegistry.TargetPoint;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileEntityMachineMissileAssembly extends TileEntity implements ITickable {

	//TODO Should the CIWS shoot down custom missiles?
	
	public ItemStackHandler inventory;
	
	public MissileStruct load;

	//private static final int[] access = new int[] { 0 };

	private String customName;
	
	public TileEntityMachineMissileAssembly() {
		inventory = new ItemStackHandler(6){
			@Override
			protected void onContentsChanged(int slot) {
				markDirty();
				super.onContentsChanged(slot);
			}
		};
	}
	
	public String getInventoryName() {
		return this.hasCustomInventoryName() ? this.customName : "container.missileAssembly";
	}

	public boolean hasCustomInventoryName() {
		return this.customName != null && this.customName.length() > 0;
	}

	public void setCustomName(String name) {
		this.customName = name;
	}
	
	public boolean isUseableByPlayer(EntityPlayer player) {
		if (world.getTileEntity(pos) != this) {
			return false;
		} else {
			return player.getDistanceSq(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64;
		}
	}
	
	@Override
	public void readFromNBT(NBTTagCompound compound) {
		if(compound.hasKey("inventory"))
			inventory.deserializeNBT(compound.getCompoundTag("inventory"));
		
		super.readFromNBT(compound);
	}
	
	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("inventory", inventory.serializeNBT());
		return super.writeToNBT(compound);
	}
	
	@Override
	public void update() {
		if(!world.isRemote) {
			
			MissileStruct multipart = new MissileStruct(inventory.getStackInSlot(1), inventory.getStackInSlot(2), inventory.getStackInSlot(3), inventory.getStackInSlot(4));
			
			PacketDispatcher.wrapper.sendToAllAround(new TEMissileMultipartPacket(pos, multipart), new TargetPoint(world.provider.getDimension(), pos.getX(), pos.getY(), pos.getZ(), 100));
		}
	}
	
	public int fuselageState() {
		
		if(inventory.getStackInSlot(2).getItem() instanceof ItemMissile) {
			
			ItemMissile part = (ItemMissile)inventory.getStackInSlot(2).getItem();
			
			if(part.type == PartType.FUSELAGE)
				return 1;
		}
		
		return 0;
	}

	public int chipState() {
		
		if(inventory.getStackInSlot(0).getItem() instanceof ItemMissile) {
			
			ItemMissile part = (ItemMissile)inventory.getStackInSlot(0).getItem();
			
			if(part.type == PartType.CHIP)
				return 1;
		}
		
		return 0;
	}

	public int warheadState() {
		
		if(inventory.getStackInSlot(1).getItem() instanceof ItemMissile &&
				inventory.getStackInSlot(2).getItem() instanceof ItemMissile &&
				inventory.getStackInSlot(4).getItem() instanceof ItemMissile) {

			ItemMissile part = (ItemMissile)inventory.getStackInSlot(1).getItem();
			ItemMissile fuselage = (ItemMissile)inventory.getStackInSlot(2).getItem();
			ItemMissile thruster = (ItemMissile)inventory.getStackInSlot(4).getItem();
			if(!(part.attributes.length > 2 && thruster.attributes.length > 2))
				return 0;

			float weight = (Float)part.attributes[2];
			float thrust = (Float)thruster.attributes[2];
			
			if(part.type == PartType.WARHEAD && fuselage.type == PartType.FUSELAGE &&
					part.bottom == fuselage.top && weight <= thrust)
				return 1;
		}
		
		return 0;
	}

	public int stabilityState() {
		
		if(inventory.getStackInSlot(3).isEmpty())
			return -1;
		
		if(inventory.getStackInSlot(3).getItem() instanceof ItemMissile && inventory.getStackInSlot(2).getItem() instanceof ItemMissile) {

			ItemMissile part = (ItemMissile)inventory.getStackInSlot(3).getItem();
			ItemMissile fuselage = (ItemMissile)inventory.getStackInSlot(2).getItem();
			
			if(part.type == PartType.FINS && fuselage.type == PartType.FUSELAGE &&
					part.top == fuselage.bottom)
				return 1;
		}
		
		return 0;
	}

	public int thrusterState() {
		
		if(inventory.getStackInSlot(4).getItem() instanceof ItemMissile && inventory.getStackInSlot(2).getItem() instanceof ItemMissile) {

			ItemMissile part = (ItemMissile)inventory.getStackInSlot(4).getItem();
			ItemMissile fuselage = (ItemMissile)inventory.getStackInSlot(2).getItem();
			
			if(part.type == PartType.THRUSTER && fuselage.type == PartType.FUSELAGE &&
					part.top == fuselage.bottom && (FuelType)part.attributes[0] == (FuelType)fuselage.attributes[0]) {
				return 1;
			}
		}
		
		return 0;
	}
	
	public boolean canBuild() {
		
		if(inventory.getStackInSlot(5).isEmpty() && chipState() == 1 && warheadState() == 1 && fuselageState() == 1 && thrusterState() == 1) {
			return stabilityState() != 0;
		}
		
		return false;
	}

	public void construct() {
		
		if(!canBuild())
			return;
		
		inventory.setStackInSlot(5, ItemCustomMissile.buildMissile(inventory.getStackInSlot(0), inventory.getStackInSlot(1), inventory.getStackInSlot(2), inventory.getStackInSlot(3), inventory.getStackInSlot(4)).copy());
		
		if(stabilityState() == 1)
			inventory.setStackInSlot(3, ItemStack.EMPTY);

		inventory.setStackInSlot(0, ItemStack.EMPTY);
		inventory.setStackInSlot(1, ItemStack.EMPTY);
		inventory.setStackInSlot(2, ItemStack.EMPTY);
		inventory.setStackInSlot(4, ItemStack.EMPTY);

		this.world.playSound(null, pos.getX(), pos.getY(), pos.getZ(), HBMSoundHandler.missileAssembly2, SoundCategory.BLOCKS, 1F, 1F);
	}
	
	@Override
	public AxisAlignedBB getRenderBoundingBox() {
		return TileEntity.INFINITE_EXTENT_AABB;
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared()
	{
		return 65536.0D;
	}
	
	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY ? CapabilityItemHandler.ITEM_HANDLER_CAPABILITY.cast(inventory) : super.getCapability(capability, facing);
	}
	
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY || super.hasCapability(capability, facing);
	}
}
