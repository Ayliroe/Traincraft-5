package train.common.api;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import ebf.tim.utility.CommonUtil;
import fexcraft.tmt.slim.Vec3f;
import mods.railcraft.api.tracks.ITrackSwitch;
import mods.railcraft.api.tracks.ITrackTile;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockRailBase;
import net.minecraft.entity.boss.EntityDragonPart;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import train.common.blocks.BlockTCRail;
import train.common.blocks.BlockTCRailGag;
import train.common.items.TCRailTypes;
import train.common.tile.TileTCRail;
import train.common.tile.TileTCRailGag;

public class EntityBogie extends EntityDragonPart {

	public EntityRollingStock host;

	public boolean isOnRail = false;
	public int meta,oldBlockX,oldBlockZ;
	public TileTCRail lastTrack=null;
	public Block l;

	private int railMetadata, xFloor=0,yFloor=0,zFloor=0;
	private double railPathX=0, railPathZ=0,motionSqrt,railPathX2, railPathZ2;
	public static final double[][][] martix = new double[][][] {
			//straight
			{{0, -0.5}, {0, 0.5}, {0, -1}},
			{{ -0.5, 0}, {0.5, 0}, {-1, 0}},
			//slope
			{{ -0.5, 0}, {0.5, 0}, {-1, 0}},
			{{ -0.5, 0}, {0.5, 0}, {-1, 0}},
			{{0, -0.5}, {0, 0.5}, {0, -1}},
			{{0, -0.5}, {0, 0.5}, {0, -1}},
			//turns
			{{0, 0.5}, {0.5, 0}, {-0.5, 0.5}},
			{{0, 0.5}, { -0.5, 0}, {0.5, 0.5}},
			{{0, -0.5}, { -0.5, 0}, {0.5, -0.5}},
			{{0, -0.5}, {0.5, 0}, {-0.5, -0.5}}
	};

	public double[] velocity = new double[]{0,0};

	public EntityBogie(EntityRollingStock host, double x, double y, double z) {
		super(host, "bogie", 0.5f, 1.25f);
		this.host = host;
		yOffset=0.425f; // TODO: this shouldn't be duplicated in ItemRollingStock
		setPosition(x, y, z);
	}

	public void update() {
		if(!getWorld().isRemote && (Math.abs(velocity[0]) + Math.abs(velocity[1])) != 0) {
			xFloor = CommonUtil.floorDouble(posX);
			yFloor = CommonUtil.floorDouble(posY);
			zFloor = CommonUtil.floorDouble(posZ);

			//update old position, add the gravity, and get the block below this,
			prevPosX = posX;
			prevPosY = posY;
			prevPosZ = posZ;

			Block oldL=l;

			l = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
			//detect slopes
			if(!(l instanceof BlockRailBase || l instanceof BlockTCRail || l instanceof BlockTCRailGag)){
				prevPosY = posY;
				if(getWorld().isAirBlock(xFloor, yFloor, zFloor)){
					posY--;
					yFloor--;
				} else {
					posY++;
					yFloor++;
				}
				l = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);

				//if it wasn't a slope, fall back to the last block, only do this for a single full block distance.
				if(!(l instanceof BlockAir || l instanceof BlockRailBase ||
						l instanceof BlockTCRail || l instanceof BlockTCRailGag) &&
						(Math.abs(xFloor)-Math.abs(oldBlockZ))+(Math.abs(zFloor)+Math.abs(oldBlockZ))<=2){
					l=oldL;
					posY++;
					yFloor++;
				}
			} else {
				oldBlockX=xFloor;
				oldBlockZ=zFloor;
			}

			//move on rails
			isOnRail = true;
			double speedMagnitude = Math.sqrt(Math.pow(velocity[0],2)+Math.pow(velocity[1],2));
			limitSpeed(speedMagnitude);
			if (l instanceof BlockRailBase) {
				loopVanilla(speedMagnitude, (BlockRailBase) l);
			} else if (l instanceof BlockTCRail || l instanceof BlockTCRailGag){
				moveOnTCRail(xFloor, yFloor, zFloor, l);
			} else {
				posY++;
				yFloor++;
				posX += velocity[0] * 0.5;
				posZ += velocity[1] * 0.5;
				isOnRail = false;
			}
		}
	}

	/*
	 * =========================================== ENTITY ===========================================
	 **/

	@Override
	@SideOnly(Side.CLIENT)
	public float getShadowSize() { return height / 2.0F; }

	// Returning false in both of these disables writing the entity to disk
	@Override
	public boolean writeToNBTOptional(NBTTagCompound tagCompound) { return false; }
	@Override
	public boolean writeMountToNBT(NBTTagCompound tagCompound) { return false; }

	@Override
	public boolean canBeCollidedWith() { return false; }

	/*
	 * =========================================== MOVEMENT ===========================================
	 **/

	private void moveOnTCRail(int i, int j, int k, Block l) {

		if(l instanceof BlockTCRail) {
			if(!TCRailTypes.isCrossingTrack((TileTCRail) getWorld().getTileEntity(i, j, k)) && !TCRailTypes.isDiagonalCrossingTrack((TileTCRail) getWorld().getTileEntity(i,j,k))) {
				lastTrack = (TileTCRail) getWorld().getTileEntity(i, j, k);
			}
		} else if(l instanceof BlockTCRailGag && (lastTrack==null || !CommonUtil.getTiles(getWorld(),i,j,k).contains(lastTrack))){
			TileTCRailGag tileGag = (TileTCRailGag) getWorld().getTileEntity(i, j, k);
			if(tileGag.originX.size()>0 && getWorld().getTileEntity(tileGag.originX.get(0), tileGag.originY.get(0), tileGag.originZ.get(0)) != null) {
				lastTrack = (TileTCRail) getWorld().getTileEntity(tileGag.originX.get(0), tileGag.originY.get(0), tileGag.originZ.get(0));
			}
		}

		// --- Switch track logic fix ---
		if (TCRailTypes.isSwitchTrack(lastTrack)) {
			// Determine direction of travel
			double vx = velocity[0];
			double vz = velocity[1];
			boolean goStraight = false;

			// For each meta, determine if the bogie is going "against" the curve (should ignore switch)
			switch (lastTrack.getBlockMetadata()) {
				case 0: // North-South (Z axis)
					goStraight = (vz > 0 && !lastTrack.getSwitchState()) || (vz < 0 && lastTrack.getSwitchState());
					break;
				case 2: // South-North (Z axis)
					goStraight = (vz < 0 && !lastTrack.getSwitchState()) || (vz > 0 && lastTrack.getSwitchState());
					break;
				case 1: // West-East (X axis)
					goStraight = (vx > 0 && !lastTrack.getSwitchState()) || (vx < 0 && lastTrack.getSwitchState());
					break;
				case 3: // East-West (X axis)
					goStraight = (vx < 0 && !lastTrack.getSwitchState()) || (vx > 0 && lastTrack.getSwitchState());
					break;
			}

			if (goStraight) {
				moveOnTCStraight(j, Math.abs(vx)>Math.abs(vz)?1:2);
			} else {
				moveOnTC90TurnRail(j, lastTrack.r, lastTrack.cx, lastTrack.cz);
			}
		}
		else if (TCRailTypes.isStraightTrack(lastTrack)) {
			moveOnTCStraight(j, lastTrack.getBlockMetadata());
		} else if(TCRailTypes.isTurnTrack(lastTrack)){
			moveOnTC90TurnRail(j, lastTrack.r, lastTrack.cx, lastTrack.cz);
		} else if (TCRailTypes.isCrossingTrack(lastTrack)) {
			moveOnTCTwoWaysCrossing();
		} else if (TCRailTypes.isSlopeTrack(lastTrack)) {
			moveOnTCSlope(j, lastTrack.xCoord, lastTrack.zCoord, lastTrack.slopeAngle, lastTrack.getBlockMetadata());
		} else if (TCRailTypes.isCurvedSlopeTrack(lastTrack)) {
			moveOnTCCurvedSlope(j, lastTrack.r, lastTrack.cx, lastTrack.cz, lastTrack.xCoord, lastTrack.zCoord, lastTrack.getBlockMetadata(), lastTrack.slopeAngle);
		} else if (TCRailTypes.isDiagonalTrack(lastTrack) || TCRailTypes.isDiagonalCrossingTrack(lastTrack)){
			moveOnTCDiagonal(j);
		}
	}

	private void moveOnTCDiagonal(int j) {

		railPathX=Math.copySign(0.5,velocity[0]);
		railPathZ=Math.copySign(0.5,velocity[1]);
		motionSqrt = Math.abs(velocity[0])+Math.abs(velocity[1]);
		velocity[0] = motionSqrt * railPathX;
		velocity[1] = motionSqrt * railPathZ;

		centerDiagonal(posX-xFloor,posZ-zFloor);

		motionSqrt = Math.abs(velocity[0])+Math.abs(velocity[1]);
		posY = j + 0.2+ yOffset;
		setPositionRelative(railPathX*motionSqrt,0,railPathZ*motionSqrt);
	}

	public void centerDiagonal(double x, double z) {
		// Calculate the vector from the center to the given point
		railPathX2 = x - 0.5;
		railPathZ2 = z - 0.5;
		// get the nearest 45 degree angle from the block center to the current position
		double nearestAngleRadians = (Math.round(CommonUtil.atan2degreesf(railPathZ2, railPathX2) / 45.0) * 45.0)*
				CommonUtil.radianF;
		// Calculate the distance from the center to the given point
		double distance = Math.sqrt(railPathX2 * railPathX2 + railPathZ2 * railPathZ2);
	}

	private void moveOnTCStraight(int j, int meta) {
		if(meta==2 || meta==0){
			railPathX=0;
			railPathZ=Math.copySign(1,velocity[1]);
			posX=xFloor+0.5;
		} else {
			railPathX=Math.copySign(1,velocity[0]);
			railPathZ=0;
			posZ=zFloor+0.5;
		}
		motionSqrt = Math.abs(velocity[0])+Math.abs(velocity[1]);
		velocity[0] = motionSqrt * railPathX;
		velocity[1] = motionSqrt * railPathZ;

		motionSqrt = Math.abs(velocity[0])+Math.abs(velocity[1]);
		posY = j + 0.2+ yOffset-ySize;
		setPositionRelative(railPathX*motionSqrt,0,railPathZ*motionSqrt);

	}

	private void moveOnTCCurvedSlope(int j,double radius, double startX, double startZ, int tilex, int tilez, int meta, double slopeAngle) {

		moveOnTC90TurnRail(j,radius,startX,startZ);
		railPathX2 = posX - startX;
		railPathZ2 = posZ - startZ;
		motionSqrt = Math.sqrt(railPathX2 * railPathX2 + railPathZ2 * railPathZ2);

		railPathX = startX + ((railPathX2 / motionSqrt) * radius);
		railPathZ = startZ + ((railPathZ2 / motionSqrt) * radius);

		setPosition(railPathX, j + 0.2 + yOffset, railPathZ);

		railPathX = tilex - posX;
		railPathZ = tilez - posZ;
		if (meta == 2 ) {
			railPathZ += 1;
			railPathX += 0.5;
		} else if (meta == 0) {
			railPathX += 0.5;
		} else if (meta == 1 ) {
			railPathX += 1;
			railPathZ += 0.5;
		} else if (meta == 3) {
			railPathZ += 0.5;
		}
		posY = Math.abs(j+ Math.min(1, (slopeAngle * Math.abs(Math.sqrt(railPathX * railPathX + railPathZ * railPathZ)))) +0.2+ yOffset -ySize);
	}

	private void moveOnTCTwoWaysCrossing() {
		double norm = Math.abs(velocity[0])+Math.abs(velocity[1]);

		if (lastTrack.blockMetadata==0||lastTrack.blockMetadata==2) {
			setPositionRelative(0.0D, 0.0D, Math.copySign(norm, Math.abs(velocity[1])));
		}
		else {
			setPositionRelative(Math.copySign(norm, Math.abs(velocity[0])), 0.0D, 0.0D);
		}

	}

	private void moveOnTCSlope(int j, double tilex, double tilez, double slopeAngle, int meta) {
		//slopes use the opposite axis of straights for some reason, so we gotta invert it.
		moveOnTCStraight(j, meta);
		railPathX = tilex - posX;
		railPathZ = tilez - posZ;
		if (meta == 2 ) {
			railPathZ += 1;
			railPathX += 0.5;
		} else if (meta == 0) {
			railPathX += 0.5;
		} else if (meta == 1 ) {
			railPathX += 1;
			railPathZ += 0.5;
		} else if (meta == 3) {
			railPathZ += 0.5;
		}
		double newYPos = Math.abs(j+ Math.min(1, (slopeAngle * Math.abs(Math.sqrt(railPathX * railPathX + railPathZ * railPathZ)))) + yOffset + 0.34f);
		setPositionRelative(0, newYPos-(j + 0.2 + yOffset), 0);
	}

	private void moveOnTC90TurnRail(int j,double radius, double startX, double startZ){
        double controlX = posX - startX;
        double controlZ = posZ - startZ;

        double controlNorm = Math.sqrt(controlX * controlX + controlZ * controlZ);
        double vnorm = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);

		motionSqrt = Math.sqrt(velocity[0] * velocity[0] + velocity[1] * velocity[1]);

        double norm_cpx = controlX / controlNorm; //u
        double norm_cpz = controlZ / controlNorm; //v

        railPathX = (posX + velocity[0]) - startX;
        railPathZ = (posZ + velocity[1]) - startZ;

        double p2_c_norm = Math.sqrt((railPathX * railPathX) + (railPathZ * railPathZ));

		railPathX2 = (startX + ((railPathX / p2_c_norm) * radius)) - posX;
		railPathZ2 = (startZ + ((railPathZ / p2_c_norm) * radius)) - posZ;

        setPosition(startX + ((controlX / controlNorm) * radius), posY, startZ + ((controlZ / controlNorm) * radius));
        setPositionRelative(Math.copySign(-norm_cpz * vnorm, railPathX2), 0.0D, Math.copySign(norm_cpx * vnorm, railPathZ2));

		velocity[0] = Math.copySign(-norm_cpz * motionSqrt, railPathX2);
		velocity[1] = Math.copySign(norm_cpx * motionSqrt, railPathZ2);
	}

	private void limitSpeed(double speedMagnitude) {

		// Default speed for most carts
		double maxSpeed = 1.8f;
		// Current max speed for locos
		if (host instanceof Locomotive) {
			maxSpeed = Math.min(maxSpeed,TrainUtils.convertSpeed((double)((Locomotive)host).getCurrentMaxSpeed()));
		}

		if (speedMagnitude > maxSpeed) {
			double overspeedFactor = speedMagnitude/maxSpeed;
			velocity[0] /= overspeedFactor;
			velocity[1] /= overspeedFactor;
		}
	}

	/*
	 * Velocity needs to be added relative to each bogie's current rotation to prevent drifting between them, as the host's rotation doesn't match when entering curves.
	 * Always adding in the direction of existing movement prevents reverse, and is unpredictable with null starting velocity, so we compare the bogie's rotation to the host's.
	 */
	public void addVelocity(double speed) {
		Vec3f bogieRotation = CommonUtil.rotatePoint(new Vec3f(1,0,0),0,180+(float)Math.toDegrees(Math.atan2(velocity[1], velocity[0])),0);
		Vec3f hostRotation = CommonUtil.rotatePoint(new Vec3f(1,0,0),0,180+host.rotationYaw,0);
		int direction = bogieRotation.dotProduct(hostRotation) >= 0 ? 1 : -1;

		velocity[0] += speed * bogieRotation.xCoord * direction;
		velocity[1] += speed * bogieRotation.zCoord * direction;
	}

	public void multiplyVelocity(double mult) {
		velocity[0] *= mult;
		velocity[1] *= mult;
	}

	public void setVelocity(double speed){
		Vec3f bogieRotation = CommonUtil.rotatePoint(new Vec3f(1,0,0),0,180+(float)Math.toDegrees(Math.atan2(velocity[1], velocity[0])),0);
		Vec3f hostRotation = CommonUtil.rotatePoint(new Vec3f(1,0,0),0,180+host.rotationYaw,0);
		int direction = bogieRotation.dotProduct(hostRotation) >= 0 ? 1 : -1;

		velocity[0] = speed * bogieRotation.xCoord * direction;
		velocity[1] = speed * bogieRotation.zCoord * direction;
	}

	public World getWorld(){return worldObj;}

	private void loopVanilla(double moveLength, BlockRailBase block){

		//try to adhere to limiter track
		float railmax = block.getRailMaxSpeed(getWorld(),host, xFloor, yFloor, zFloor); // TODO: x and y flipped?
		Block blockUp;
		if(railmax!=0.4f){
			moveLength=Math.min(moveLength,railmax);
		}
		railMetadata = CommonUtil.getRailMeta(getWorld(), host, xFloor, yFloor, zFloor);
		//actually move
		while (moveLength>0) {
			moveBogieVanilla(Math.min(0.3, moveLength));
			moveLength -= 0.3;

			//update the last used block to the one we just used, if it's actually different.
			if(xFloor!=CommonUtil.floorDouble(posX) || zFloor != CommonUtil.floorDouble(posZ)) {
				xFloor = CommonUtil.floorDouble(posX);
				yFloor = CommonUtil.floorDouble(posY);
				zFloor = CommonUtil.floorDouble(posZ);
				//check for collisions and skip update
				for (int i = 1; i < host.getHitboxSize()[1] - 1; i++) {
					blockUp = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor + i, zFloor);
					if (!(blockUp instanceof BlockAir)) {
						host.bogies.multiplyVelocity(0);
						return;
					}
				}
				//handle slope movement before other interactions
				if(!CommonUtil.isRailBlockAt(getWorld(), xFloor, yFloor, zFloor)){
					prevPosY =posY;
					if(CommonUtil.isRailBlockAt(getWorld(), xFloor, yFloor+1, zFloor)){
						posY++;
					} else if (CommonUtil.isRailBlockAt(getWorld(), xFloor, yFloor-1, zFloor)) {
						posY--;
					}
					yFloor = CommonUtil.floorDouble(posY);
				}

				l = CommonUtil.getBlockAt(getWorld(), xFloor, yFloor, zFloor);
				//now loop this again for the next increment of movement, if there is one
				if (l instanceof BlockRailBase) {
					block = (BlockRailBase) l;
					//do the rail functions.
					if(host.shouldDoRailFunctions()) {
						block.onMinecartPass(getWorld(), host, xFloor, yFloor, zFloor);
					}
					//get the direction of the rail from it's metadata
					railMetadata = CommonUtil.getRailMeta(getWorld(), host, xFloor, yFloor, zFloor);
				}
				//get the direction of the rail from it's metadata
				else if (getWorld().getTileEntity(xFloor, yFloor, zFloor) instanceof ITrackTile && (((ITrackTile)getWorld().getTileEntity(xFloor, yFloor, zFloor)).getTrackInstance() instanceof ITrackSwitch)){
					railMetadata = CommonUtil.getRailMeta(getWorld(),host,xFloor, yFloor, zFloor);//railcraft support
				}
			}
		}
	}

	private void moveBogieVanilla(double currentMotion){
		if(Math.abs(currentMotion)<0.000001){return;}
		//figure out the current rail's direction
		railPathX = (martix[railMetadata][2][0]);
		railPathZ = (martix[railMetadata][2][1]);

		railPathX = Math.copySign(Math.sqrt(Math.abs(railPathX)),railPathX);
		railPathZ = Math.copySign(Math.sqrt(Math.abs(railPathZ)),railPathZ);

		//cover moving reverse of track direction using the rotation from the closed loop rather than the full motion
		if(velocity[0] * railPathX + velocity[1] * railPathZ <= 0.0D) {
			railPathX = -railPathX;
			railPathZ = -railPathZ;
		}

		setPositionRelative((currentMotion * railPathX), 0, (currentMotion * railPathZ));

		motionSqrt = Math.sqrt(Math.pow(velocity[0],2)+Math.pow(velocity[1],2));
		velocity[0] = (float)(motionSqrt * railPathX);
		velocity[1] = (float)(motionSqrt * railPathZ);

		motionSqrt = Math.sqrt(Math.pow(velocity[0],2)+Math.pow(velocity[1],2));

		//define the rail path again, to center the transport.
		railPathX2 = xFloor + 0.5D + martix[railMetadata][0][0];
		railPathZ2 = zFloor + 0.5D + martix[railMetadata][0][1];
		railPathX = (xFloor + 0.5D + martix[railMetadata][1][0]) - railPathX2;
		railPathZ = (zFloor + 0.5D + martix[railMetadata][1][1]) - railPathZ2;

		//based on the path direction, try to center the bogie on the track
		if (railPathX == 0.0D) {
			motionSqrt = posZ - zFloor;
		} else if (railPathZ == 0.0D) {
			motionSqrt = posX - xFloor;
		} else {
			motionSqrt = ((posX - railPathX2) * railPathX + (posZ - railPathZ2) * railPathZ) * 2.0D;
		}
		//do the centering movement
		setPosition((railPathX2 + railPathX * motionSqrt), posY, (railPathZ2 + railPathZ * motionSqrt));
	}

	public void setPositionRelative(double x, double y, double z) {
		posX+=((int)(x*10000))*0.0001;
		if(y!=0) {//usually we won't be changing this, so this is more efficient
			posY += ((int) (y * 10000)) * 0.0001;
		}
		posZ+=((int)(z*10000))*0.0001;
		float f = width / 2.0F;
		boundingBox.setBounds(x - (double)f, y - (double)yOffset + (double)ySize, z - (double)f, x + (double)f, y - (double)yOffset + (double)ySize + (double)height, z + (double)f);

	}
}