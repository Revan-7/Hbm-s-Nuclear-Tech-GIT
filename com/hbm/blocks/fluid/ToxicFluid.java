package com.hbm.blocks.fluid;

import java.awt.Color;

import com.hbm.lib.RefStrings;

import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidRegistry;

public class ToxicFluid extends Fluid {

	public ToxicFluid(){
		super("toxic_fluid", new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/toxic_still"), new ResourceLocation(RefStrings.MODID, "blocks/forgefluid/toxic_flowing"), Color.white);
		FluidRegistry.addBucketForFluid(this);
	}
	
}
