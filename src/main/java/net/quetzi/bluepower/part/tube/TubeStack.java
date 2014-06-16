package net.quetzi.bluepower.part.tube;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemDye;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.ForgeDirection;
import net.quetzi.bluepower.api.tube.IPneumaticTube.TubeColor;
import net.quetzi.bluepower.api.vec.Vector3Cube;
import net.quetzi.bluepower.client.renderers.RenderHelper;
import net.quetzi.bluepower.references.Refs;

import org.lwjgl.opengl.GL11;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class TubeStack {
    
    public ItemStack          stack;
    private final TubeColor   color;
    public double             progress;        //0 at the start, 0.5 on an intersection, 1 at the end.
    public double             oldProgress;
    public ForgeDirection     heading;
    
    @SideOnly(Side.CLIENT)
    private static RenderItem customRenderItem;
    private static EntityItem renderedItem;
    
    public TubeStack(ItemStack stack, ForgeDirection from) {
    
        this(stack, from, TubeColor.NONE);
    }
    
    public TubeStack(ItemStack stack, ForgeDirection from, TubeColor color) {
    
        heading = from;
        this.stack = stack;
        this.color = color;
    }
    
    /**
     * Updates the movement by the given m/tick.
     * @return true if the stack has gone past the center, meaning logic needs to be triggered.
     */
    public boolean update(double move) {
    
        oldProgress = progress;
        boolean isEntering = progress < 0.5;
        progress += move;
        return progress >= 0.5 && isEntering;
    }
    
    public void writeToNBT(NBTTagCompound tag) {
    
        stack.writeToNBT(tag);
        tag.setByte("color", (byte) color.ordinal());
        tag.setByte("heading", (byte) heading.ordinal());
        tag.setDouble("progress", progress);
    }
    
    public static TubeStack loadFromNBT(NBTTagCompound tag) {
    
        TubeStack stack = new TubeStack(ItemStack.loadItemStackFromNBT(tag), ForgeDirection.getOrientation(tag.getByte("heading")), TubeColor.values()[tag.getByte("color")]);
        stack.progress = tag.getDouble("progress");
        return stack;
    }
    
    @SideOnly(Side.CLIENT)
    public void render(float partialTick) {
    
        if (customRenderItem == null) {
            customRenderItem = new RenderItem() {
                
                @Override
                public boolean shouldBob() {
                
                    return false;
                };
            };
            customRenderItem.setRenderManager(RenderManager.instance);
            
            renderedItem = new EntityItem(FMLClientHandler.instance().getWorldClient());
            renderedItem.hoverStart = 0.0F;
        }
        
        renderedItem.setEntityItemStack(stack);
        
        double renderProgress = (oldProgress + (progress - oldProgress) * partialTick) * 2 - 1;
        
        GL11.glPushMatrix();
        GL11.glTranslated(heading.offsetX * renderProgress * 0.5, heading.offsetY * renderProgress * 0.5, heading.offsetZ * renderProgress * 0.5);
        customRenderItem.doRender(renderedItem, 0, 0, 0, 0, 0);
        
        if (color != TubeColor.NONE) {
            
            float size = 0.2F;
            
            int colorInt = ItemDye.field_150922_c[color.ordinal()];
            float red = (colorInt >> 16) / 256F;
            float green = (colorInt >> 8 & 255) / 256F;
            float blue = (colorInt & 255) / 256F;
            
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glColor3f(red, green, blue);
            Minecraft.getMinecraft().renderEngine.bindTexture(new ResourceLocation(Refs.MODID, "textures/blocks/tubes/inside_color_border.png"));
            RenderHelper.drawTesselatedTexturedCube(new Vector3Cube(-size, -size, -size, size, size, size));
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glEnable(GL11.GL_LIGHTING);
        }
        
        GL11.glPopMatrix();
    }
}
