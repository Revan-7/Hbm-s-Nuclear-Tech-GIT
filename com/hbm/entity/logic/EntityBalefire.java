package com.hbm.entity.logic;

import org.apache.logging.log4j.Level;

import com.hbm.entity.effect.EntityFalloutRain;
import com.hbm.explosion.ExplosionBalefire;
import com.hbm.explosion.ExplosionFleija;
import com.hbm.explosion.ExplosionNukeAdvanced;
import com.hbm.explosion.ExplosionNukeGeneric;
import com.hbm.explosion.ExplosionSolinium;
import com.hbm.main.MainRegistry;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class EntityBalefire extends Entity {
	
	public int age = 0;
	public int destructionRange = 0;
	public ExplosionBalefire exp;
	public int speed = 1;
	public boolean did = false;

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		age = nbt.getInteger("age");
		destructionRange = nbt.getInteger("destructionRange");
		speed = nbt.getInteger("speed");
		did = nbt.getBoolean("did");
		
    	
		exp = new ExplosionBalefire((int)this.posX, (int)this.posY, (int)this.posZ, this.worldObj, this.destructionRange);
		exp.readFromNbt(nbt, "exp_");
    	
    	this.did = true;
		
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setInteger("age", age);
		nbt.setInteger("destructionRange", destructionRange);
		nbt.setInteger("speed", speed);
		nbt.setBoolean("did", did);
    	
		if(exp != null)
			exp.saveToNbt(nbt, "exp_");
		
	}

	public EntityBalefire(World p_i1582_1_) {
		super(p_i1582_1_);
	}

    @Override
	public void onUpdate() {
        super.onUpdate();
        	
        if(!this.did)
        {
    		if(MainRegistry.enableExtendedLogging && !worldObj.isRemote)
    			MainRegistry.logger.log(Level.INFO, "[NUKE] Initialized BF explosion at " + posX + " / " + posY + " / " + posZ + " with strength " + destructionRange + "!");
    		
        	exp = new ExplosionBalefire((int)this.posX, (int)this.posY, (int)this.posZ, this.worldObj, this.destructionRange);
        	
        	this.did = true;
        }
        
        speed += 1;	//increase speed to keep up with expansion
        
        boolean flag = false;
        boolean flag2 = false;
        boolean flag3 = false;
        
        for(int i = 0; i < this.speed; i++)
        {
        	flag = exp.update();
        	
        	if(flag) {
        		this.setDead();
        	}
        }
        
    	if(rand.nextInt(5) == 0)
        	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "random.explode", 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
        	
        if(!flag)
        {
        	this.worldObj.playSoundEffect(this.posX, this.posY, this.posZ, "ambient.weather.thunder", 10000.0F, 0.8F + this.rand.nextFloat() * 0.2F);
        	ExplosionNukeGeneric.dealDamage(this.worldObj, (int)this.posX, (int)this.posY, (int)this.posZ, this.destructionRange * 2);
        }
        
        age++;
    }

	@Override
	protected void entityInit() { }
}
