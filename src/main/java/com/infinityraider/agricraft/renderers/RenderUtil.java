package com.infinityraider.agricraft.renderers;

import com.agricraft.agricore.config.AgriConfigCategory;
import com.agricraft.agricore.config.AgriConfigurable;
import com.agricraft.agricore.core.AgriCore;
import com.infinityraider.agricraft.renderers.tessellation.ITessellator;
import com.infinityraider.agricraft.renderers.tessellation.TessellatorAbstractBase;
import com.infinityraider.agricraft.utility.AgriForgeDirection;
import com.infinityraider.agricraft.utility.BaseIcons;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

@SideOnly(Side.CLIENT)
public final class RenderUtil {

	public static final int COLOR_MULTIPLIER_STANDARD = 16777215;

	@AgriConfigurable(key = "Draw Rendering Axes", category = AgriConfigCategory.DEBUG, comment = "If the AgriCraft renderer should render reference axes.")
	private static boolean debugAxis = false;

	static {
		AgriCore.getConfig().addConfigurable(RenderUtil.class);
	}

	private RenderUtil() {
	}

	public static int getMixedBrightness(IBlockAccess world, BlockPos pos, Block block) {
		return getMixedBrightness(world, pos, world.getBlockState(pos), block);
	}

	public static int getMixedBrightness(IBlockAccess world, BlockPos pos, IBlockState state) {
		return getMixedBrightness(world, pos, state, state.getBlock());
	}

	public static int getMixedBrightness(IBlockAccess world, BlockPos pos, IBlockState state, Block block) {
		//TODO: get brightness
		//return world.getCombinedLight();
		return 1;
	}

	public static int getColorMultiplier(IBlockAccess world, BlockPos pos, IBlockState state) {
		return getColorMultiplier(world, pos, state, state.getBlock());
	}

	public static int getColorMultiplier(IBlockAccess world, BlockPos pos, Block block) {
		return getColorMultiplier(world, pos, world.getBlockState(pos), block);
	}

	public static int getColorMultiplier(IBlockAccess world, BlockPos pos, IBlockState state, Block block) {
		return Minecraft.getMinecraft().getBlockColors().colorMultiplier(state, world, pos, block.getMetaFromState(state));
	}

	/**
	 * Applies a color multiplier to the tessellator for a given side, the side
	 * is transformed according to the rotation of the tessellator
	 */
	public static void applyColorMultiplier(ITessellator tessellator, int colorMultiplier, AgriForgeDirection side) {
		float preMultiplier;
		if (tessellator instanceof TessellatorAbstractBase) {
			preMultiplier = getMultiplier(transformSide((TessellatorAbstractBase) tessellator, side));
		} else {
			preMultiplier = 1;
		}
		float r = preMultiplier * ((float) (colorMultiplier >> 16 & 255) / 255.0F);
		float g = preMultiplier * ((float) (colorMultiplier >> 8 & 255) / 255.0F);
		float b = preMultiplier * ((float) (colorMultiplier & 255) / 255.0F);
		tessellator.setColorRGB(r, g, b);
	}

	/**
	 * Transforms a direction according to the rotation of the tessellator
	 */
	public static AgriForgeDirection transformSide(TessellatorAbstractBase tessellator, AgriForgeDirection dir) {
		if (dir == AgriForgeDirection.UNKNOWN) {
			return dir;
		}
		double[] coords = tessellator.getTransformationMatrix().transform(dir.offsetX, dir.offsetY, dir.offsetZ);
		double[] translation = tessellator.getTransformationMatrix().getTranslation();
		coords[0] = coords[0] - translation[0];
		coords[1] = coords[1] - translation[1];
		coords[2] = coords[2] - translation[2];
		double x = Math.abs(coords[0]);
		double y = Math.abs(coords[1]);
		double z = Math.abs(coords[2]);
		if (x > z) {
			if (x > y) {
				return coords[0] > 0 ? AgriForgeDirection.EAST : AgriForgeDirection.WEST;
			}
		} else if (z > y) {
			return coords[2] > 0 ? AgriForgeDirection.SOUTH : AgriForgeDirection.NORTH;
		}
		return coords[1] > 0 ? AgriForgeDirection.UP : AgriForgeDirection.DOWN;
	}

	/**
	 * Gets a color multiplier factor for the given side (same values used by
	 * vanilla)
	 */
	public static float getMultiplier(AgriForgeDirection side) {
		switch (side) {
			case DOWN:
				return 0.5F;
			case NORTH:
			case SOUTH:
				return 0.8F;
			case EAST:
			case WEST:
				return 0.6F;
			default:
				return 1;
		}
	}

	public static void rotateBlock(ITessellator tess, AgriForgeDirection dir) {
		tess.translate(0.5, 0, 0.5);
		//tess.rotate(180, 0F, 0F, 1F);
		switch (dir) {
			case WEST:
				tess.rotate(90, 0, 1, 0);
				break;
			case SOUTH:
				tess.rotate(180, 0, 1, 0);
				break;
			case EAST:
				tess.rotate(270, 0, 1, 0);
				break;
		}
		tess.translate(-0.5, 0, -0.5);
	}

	public static void rotateBlockInventory(ITessellator tess) {
		tess.translate(0.5, 0.5, 0.5);
		tess.rotate(30, 1, 0, 0);
		tess.rotate(-45, 0, 1, 0);
		tess.translate(-.5, -.475, -.5);
		tess.scale(0.63, 0.63, 0.63);
	}

	public static void drawAxis(ITessellator tess) {
		if (debugAxis) {
			int prevc = tess.getColor();
			tess.setColorRGBA(255, 0, 0, 125);
			tess.drawScaledPrism(0, 0, 0, 16f, 1f, 1f, BaseIcons.IRON_BLOCK.getIcon());
			tess.setColorRGBA(0, 255, 0, 125);
			tess.drawScaledPrism(0, 0, 0, 1f, 16f, 1f, BaseIcons.IRON_BLOCK.getIcon());
			tess.setColorRGBA(0, 0, 255, 125);
			tess.drawScaledPrism(0, 0, 0, 1f, 1f, 16f, BaseIcons.IRON_BLOCK.getIcon());
			tess.setColor(prevc);
		}
	}

	public static void renderItemStack(ItemStack stack, double x, double y, double z, double scale, boolean rotate) {

		// Save Settings
		GlStateManager.pushAttrib();
		GlStateManager.pushMatrix();

		// Fix Lighting
		RenderHelper.enableStandardItemLighting();
		GlStateManager.enableLighting();

		// Translate to correct spot
		GlStateManager.translate(x, y, z);

		// Scale to correct Size
		GlStateManager.scale(scale, scale, scale);

		// Rotate Item as function of system time.
		if (rotate) {
			double angle = (720.0 * (System.currentTimeMillis() & 0x3FFFL) / 0x3FFFL); //credits to Pahimar
			GlStateManager.rotate((float) angle, 0, 1, 0);
		}

		// Draw the item.
		Minecraft.getMinecraft().getRenderItem().renderItem(stack, ItemCameraTransforms.TransformType.GROUND);

		// Restore Settings.
		GlStateManager.popMatrix();
		GlStateManager.popAttrib();

	}

}
