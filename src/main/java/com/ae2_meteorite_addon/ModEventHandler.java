package com.ae2_meteorite_addon;

import appeng.api.AEApi;
import com.ae2_meteorite_addon.entity.EntityBuddingRepair;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod.EventBusSubscriber(modid = AE2MeteoriteAddon.MODID)
public class ModEventHandler {

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        if (event.getWorld().isRemote) {
            return;
        }
        if (event.getEntity() instanceof EntityBuddingRepair) {
            return;
        }

        if (event.getEntity() instanceof EntityItem) {
            EntityItem entityItem = (EntityItem) event.getEntity();
            ItemStack stack = entityItem.getItem();
            if (stack.isEmpty()) {
                return;
            }

            if (!AEApi.instance().definitions().materials().certusQuartzCrystalCharged().isSameAs(stack)) {
                return;
            }

            event.setCanceled(true);

            EntityBuddingRepair newEntity = new EntityBuddingRepair(
                    event.getWorld(),
                    entityItem.posX, entityItem.posY, entityItem.posZ,
                    stack.copy()
            );
            newEntity.motionX = entityItem.motionX;
            newEntity.motionY = entityItem.motionY;
            newEntity.motionZ = entityItem.motionZ;
            newEntity.setPickupDelay(40);

            event.getWorld().spawnEntity(newEntity);
        }
    }
}
