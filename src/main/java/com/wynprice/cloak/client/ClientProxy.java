package com.wynprice.cloak.client;

import java.util.HashMap;

import com.wynprice.cloak.client.handlers.ModelBakeHandler;
import com.wynprice.cloak.client.handlers.ParticleHandler;
import com.wynprice.cloak.client.handlers.TextureStitchHandler;
import com.wynprice.cloak.client.rendering.TileEntityCloakBlockRenderer;
import com.wynprice.cloak.client.rendering.TileEntityCloakingMachineRenderer;
import com.wynprice.cloak.common.CommonProxy;
import com.wynprice.cloak.common.registries.CloakBlocks;
import com.wynprice.cloak.common.registries.CloakItems;
import com.wynprice.cloak.common.tileentity.TileEntityCloakBlock;
import com.wynprice.cloak.common.tileentity.TileEntityCloakingMachine;

import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.color.ItemColors;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTUtil;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.items.ItemStackHandler;

public class ClientProxy extends CommonProxy
{
	@Override
	public void preInit(FMLPreInitializationEvent event) 
	{
		super.preInit(event);
		CloakItems.regRenders();
		CloakBlocks.regRenders();
		
		registerHandlers();
		registerTileEntityDispatchers();
	}
	
	@Override
	public void init(FMLInitializationEvent event) 
	{
		super.init(event);
		registerItemColors();
	}
	
	
	
	
	
	
	private void registerTileEntityDispatchers()
	{
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCloakBlock.class, new TileEntityCloakBlockRenderer());
		ClientRegistry.bindTileEntitySpecialRenderer(TileEntityCloakingMachine.class, new TileEntityCloakingMachineRenderer(TileEntityCloakingMachine.FACTORY));
	}
	
	private void registerHandlers()
	{
		Object[] handlers = 
			{
					new ModelBakeHandler(),
					new TextureStitchHandler(),
					new ParticleHandler()
			};
		
		for(Object o : handlers)
    		MinecraftForge.EVENT_BUS.register(o);
	}
	
	private void registerItemColors()
	{
		ItemColors ic = Minecraft.getMinecraft().getItemColors();
		ic.registerItemColorHandler(new IItemColor() {
			
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) 
			{
				ItemStackHandler handler = new ItemStackHandler(1);
				handler.deserializeNBT(stack.getOrCreateSubCompound("capture_info").getCompoundTag("item"));
				return tintIndex == 0 ? -1 : handler.getStackInSlot(0).isEmpty() ? -1 : Minecraft.getMinecraft().getItemColors().colorMultiplier(handler.getStackInSlot(0), tintIndex - 1);
			}
		}, CloakItems.BLOCKSTATE_CARD);
		
		ic.registerItemColorHandler(new IItemColor() {
			
			@Override
			public int colorMultiplier(ItemStack stack, int tintIndex) 
			{
				NBTTagCompound nbt = stack.getOrCreateSubCompound("rendering_info");
		    	HashMap<Integer, ItemStack> currentModMap = TileEntityCloakingMachine.readFromNBTTag(nbt.getCompoundTag("mod_list"));
				HashMap<Integer, ItemStack> overrideList = new HashMap<>();
				for(int i : currentModMap.keySet())
					if(currentModMap.get(i) != null && !currentModMap.get(i).isEmpty())
					{
						ItemStackHandler innerHandler = new ItemStackHandler();
						innerHandler.deserializeNBT(currentModMap.get(i).getOrCreateSubCompound("capture_info").getCompoundTag("item"));
						overrideList.put(i, innerHandler.getStackInSlot(0));
					}
								
				
				if(overrideList.containsKey(Math.floorDiv(tintIndex, 1000)))
					return Minecraft.getMinecraft().getItemColors().colorMultiplier(overrideList.get(Math.floorDiv(tintIndex, 1000)), tintIndex % 1000);
				
				ItemStackHandler handler = new ItemStackHandler(3);
				handler.deserializeNBT(nbt.getCompoundTag("ItemHandler"));
				ItemStackHandler innerHandler = new ItemStackHandler();
				innerHandler.deserializeNBT(handler.getStackInSlot(0).getOrCreateSubCompound("capture_info").getCompoundTag("item"));
				return Minecraft.getMinecraft().getItemColors().colorMultiplier(innerHandler.getStackInSlot(0), tintIndex % 1000);
			}
		}, CloakBlocks.CLOAK_BLOCK);
	}
}
