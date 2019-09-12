package com.hbm.forgefluid;

import java.util.ArrayList;
import java.util.List;

import com.hbm.interfaces.IFluidPipe;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.IFluidHandler;

public class FFPipeNetwork {
	protected Fluid type;
	protected List<IFluidHandler> fillables = new ArrayList<IFluidHandler>();
	protected List<IFluidPipe> pipes = new ArrayList<IFluidPipe>();
	

	public FFPipeNetwork() {
		this(null);
	}

	public FFPipeNetwork(Fluid fluid) {
		this.type = fluid;
	}
	
	public int getSize() {
		return pipes.size() + fillables.size();
	}

	public static FFPipeNetwork mergeNetworks(FFPipeNetwork net, FFPipeNetwork merge) {
		if (net != null && merge != null && net != merge) {
			for (IFluidPipe pipe : merge.pipes) {
				net.pipes.add(pipe);
				pipe.setNetwork(net);
			}
			for (IFluidHandler fill : merge.fillables) {
				net.fillables.add(fill);

			}
			merge.Destroy();
			return net;
		} else {
			return null;
		}
	}

	public static FFPipeNetwork buildNewNetwork(TileEntity pipe) {
		FFPipeNetwork net = new ;
		if (pipe instanceof IFluidPipe) {
			IFluidPipe fPipe = (IFluidPipe) pipe;
			List[] netVars = iteratePipes(fPipe.getNetwork().pipes, fPipe.getNetwork().fillables, pipe);
			net = new FFPipeNetwork();
			net.pipes = netVars[0];
			net.fillables = netVars[1];
			net.setType(fPipe.getType());
		}
		return net;
	}

	public static List[] iteratePipes(List<IFluidPipe> pipes, List<IFluidHandler> consumers, TileEntity te) {
		if(pipes == null)
			pipes = new ArrayList<IFluidPipe>();
		if(consumers == null)
			consumers = new ArrayList<IFluidHandler>();
		if (te == null)
			return new List[]{pipes, consumers};
		TileEntity next = null;
		if (te.getWorldObj().getTileEntity(te.xCoord, te.yCoord, te.zCoord) != null) {
			for (int i = 0; i < 6; i++) {
				next = getTileEntityAround(te, i);
				if (next instanceof IFluidHandler && next instanceof IFluidPipe) {
					pipes.add((IFluidPipe) next);
					List[] nextPipe = iteratePipes(pipes, consumers, te);
					pipes.addAll(nextPipe[0]);
					consumers.addAll(nextPipe[1]);
				} else if (next instanceof IFluidHandler) {
					consumers.add((IFluidHandler) next);
				}
			}

		}
		return new List[]{pipes, consumers};
	}

	public static TileEntity getTileEntityAround(TileEntity te, int dir) {
		if (te == null)
			return null;
		switch (dir) {
		case 0:
			return te.getWorldObj().getTileEntity(te.xCoord + 1, te.yCoord, te.zCoord);
		case 1:
			return te.getWorldObj().getTileEntity(te.xCoord - 1, te.yCoord, te.zCoord);
		case 2:
			return te.getWorldObj().getTileEntity(te.xCoord, te.yCoord, te.zCoord + 1);
		case 3:
			return te.getWorldObj().getTileEntity(te.xCoord, te.yCoord, te.zCoord - 1);
		case 4:
			return te.getWorldObj().getTileEntity(te.xCoord, te.yCoord + 1, te.zCoord);
		case 5:
			return te.getWorldObj().getTileEntity(te.xCoord, te.yCoord - 1, te.zCoord);
		default:
			return null;
		}
	}

	public void Destroy() {
		this.fillables.clear();
		this.pipes.clear();
		
	}

	public void setType(Fluid fluid) {
		this.type = fluid;
	}

	public Fluid getType() {
		return this.type;
	}

	public boolean addPipe(IFluidPipe pipe) {
		if (pipe.getType() != null && pipe.getType() == this.getType()) {
			pipes.add(pipe);
			return true;
		}
		return false;
	}

	public boolean removePipe(IFluidPipe pipe) {
		if (pipes.contains(pipe)) {
			pipes.remove(pipe);
			return true;
		}
		return false;
	}
}