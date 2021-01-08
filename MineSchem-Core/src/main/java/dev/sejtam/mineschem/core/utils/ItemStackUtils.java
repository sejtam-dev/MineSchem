package dev.sejtam.mineschem.core.utils;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.NBTUtils;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;

public class ItemStackUtils {

    public static ItemStack getItemStackFromNBT(NBTCompound nbtItem) {
        if(nbtItem == null)
            return new ItemStack(Material.AIR, (byte)0);

        String id = nbtItem.getString("id");
        if(id == null)
            return new ItemStack(Material.AIR, (byte)0);

        String materialName = id.split(":")[1];
        ItemStack item = new ItemStack(Material.valueOf(materialName.toUpperCase()), (byte)1);


        NBTCompound itemTags = (NBTCompound) nbtItem.get("tag");
        if(itemTags == null)
            return new ItemStack(Material.AIR, (byte)0);

        NBTUtils.loadDataIntoItem(item, itemTags);

        return item;
    }

}
