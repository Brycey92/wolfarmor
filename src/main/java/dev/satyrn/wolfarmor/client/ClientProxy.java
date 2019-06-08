package dev.satyrn.wolfarmor.client;

import dev.satyrn.wolfarmor.api.common.ItemWolfArmor;
import dev.satyrn.wolfarmor.api.client.RenderLayerFactory;
import dev.satyrn.wolfarmor.api.util.Items;
import dev.satyrn.wolfarmor.client.renderer.entity.layer.LayerWolfArmor;
import dev.satyrn.wolfarmor.client.renderer.entity.layer.LayerWolfBackpack;
import dev.satyrn.wolfarmor.common.CommonProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.color.IItemColor;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.entity.RenderWolf;
import net.minecraft.entity.passive.EntityWolf;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IThreadListener;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Loads client-specific mod data
 */
@SideOnly(Side.CLIENT)
public class ClientProxy extends CommonProxy {
    //region Public / Protected Methods

    @Override
    public void registerEntityRenderingHandlers() {
        RenderManager renderManager = Minecraft.getMinecraft().getRenderManager();
        RenderWolf renderWolf = (RenderWolf) renderManager.entityRenderMap.get(EntityWolf.class);

        LayerWolfArmor armor = (LayerWolfArmor) RenderLayerFactory.createArmorLayer(renderWolf);
        LayerWolfBackpack backpack = (LayerWolfBackpack) RenderLayerFactory.createBackpackLayer(renderWolf);
        if(armor == null || backpack == null) throw new RuntimeException("Armor layer factory failed initialization!");

        renderWolf.addLayer(armor);
        renderWolf.addLayer(backpack);
    }

    /**
     * Registers item renders for this mod.
     *
     * @param initializationEvent the initialization event
     */
    @Override
    public void registerItemRenders(@Nonnull FMLInitializationEvent initializationEvent) {
        registerItemModel(Items.LEATHER_WOLF_ARMOR);
        registerItemModel(Items.CHAINMAIL_WOLF_ARMOR);
        registerItemModel(Items.IRON_WOLF_ARMOR);
        registerItemModel(Items.GOLD_WOLF_ARMOR);
        registerItemModel(Items.DIAMOND_WOLF_ARMOR);
    }

    @Override
    public void registerItemColorHandlers(@Nonnull FMLInitializationEvent initializationEvent) {
        Minecraft.getMinecraft().getItemColors().registerItemColorHandler(new IItemColor() {
            @Override
            public int colorMultiplier(@Nonnull ItemStack stack, int tintIndex) {
                return tintIndex > 0 ? -1 : ((ItemWolfArmor) stack.getItem()).getColor(stack);
            }
        }, Items.LEATHER_WOLF_ARMOR);
    }

    /**
     * Registers item models with the model loader
     *  @param item     The item
     *
     */
    @SideOnly(Side.CLIENT)
    private static void registerItemModel(@Nullable Item item) {
        if (item != null && item.getRegistryName() != null) {
            ModelResourceLocation resource = new ModelResourceLocation(item.getRegistryName().toString(), "inventory");
            ModelLoader.registerItemVariants(item, item.getRegistryName());
            Minecraft.getMinecraft().getRenderItem().getItemModelMesher().register(item, 0, resource);
        }
    }

    @Override
    public IThreadListener getThreadFromContext(MessageContext context) {
        return context.side.isClient() ? Minecraft.getMinecraft() : super.getThreadFromContext(context);
    }

    @Override
    public EntityPlayer getPlayerFromContext(MessageContext context) {
        return context.side.isClient()? Minecraft.getMinecraft().player : super.getPlayerFromContext(context);
    }

    @Override
    public Side getCurrentSide() {
        return Side.CLIENT;
    }

    //endregion Public / Protected Methods
}