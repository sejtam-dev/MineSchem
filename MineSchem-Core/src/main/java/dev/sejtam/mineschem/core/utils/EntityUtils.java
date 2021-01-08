package dev.sejtam.mineschem.core.utils;

import net.minestom.server.attribute.Attribute;
import net.minestom.server.attribute.Attributes;
import net.minestom.server.entity.*;
import net.minestom.server.entity.type.ambient.*;
import net.minestom.server.entity.type.animal.*;
import net.minestom.server.entity.type.decoration.EntityArmorStand;
import net.minestom.server.entity.type.decoration.EntityItemFrame;
import net.minestom.server.entity.type.monster.*;
import net.minestom.server.entity.type.other.EntityAreaEffectCloud;
import net.minestom.server.entity.type.other.EntityEndCrystal;
import net.minestom.server.entity.type.other.EntityIronGolem;
import net.minestom.server.entity.type.other.EntitySnowman;
import net.minestom.server.entity.type.projectile.EntityEyeOfEnder;
import net.minestom.server.entity.type.vehicle.*;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.Position;
import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTList;

public class EntityUtils {

    public static Entity getEntity(EntityType type, Position position) {
        switch (type) {
            case BAT:
                return new EntityBat(position);
            case BEE:
                return new EntityBee(position);
            case CAT:
                return new EntityCat(position);
            case COD:
                return null;
            case COW:
                return new EntityCow(position);
            case EGG:
                return null;
            case FOX:
                return new EntityFox(position);
            case PIG:
                return new EntityPig(position);
            case TNT:
                return null;
            case VEX:
                return null;
            case BOAT:
                return new EntityBoat(position);
            case HUSK:
                return new EntityZombie(position);
            case ITEM:
                return new ItemEntity(new ItemStack(Material.DIRT, (byte)0), position);
            case MULE:
                return null;
            case WOLF:
                return null;
            case ARROW:
                return null;
            case BLAZE:
                return new EntityBlaze(position);
            case GHAST:
                return new EntityGhast(position);
            case GIANT:
                return new EntityGiant(position);
            case HORSE:
                return null;
            case LLAMA:
                return new EntityLlama(position);
            case PANDA:
                return new EntityPanda(position);
            case SHEEP:
                return null;
            case SLIME:
                return new EntitySlime(position);
            case SQUID:
                return null;
            case STRAY:
                return null;
            case WITCH:
                return new EntityWitch(position);
            case DONKEY:
                return null;
            case EVOKER:
                return null;
            case HOGLIN:
                return null;
            case OCELOT:
                return new EntityOcelot(position);
            case PARROT:
                return null;
            case PIGLIN:
                return null;
            case PLAYER:
                return null;
            case POTION:
                return null;
            case RABBIT:
                return new EntityRabbit(position);
            case SALMON:
                return null;
            case SPIDER:
                return new EntitySpider(position);
            case TURTLE:
                return null;
            case WITHER:
                return null;
            case ZOGLIN:
                return null;
            case ZOMBIE:
                return new EntityZombie(position);
            case CHICKEN:
                return new EntityChicken(position);
            case CREEPER:
                return new EntityCreeper(position);
            case DOLPHIN:
                return new EntityDolphin(position);
            case DROWNED:
                return null;
            case PHANTOM:
                return new EntityPhantom(position);
            case RAVAGER:
                return null;
            case SHULKER:
                return null;
            case STRIDER:
                return null;
            case TRIDENT:
                return null;
            case ENDERMAN:
                return null;
            case FIREBALL:
                return null;
            case GUARDIAN:
                return new EntityGuardian(position);
            case MINECART:
                return null;
            case PAINTING:
                return null;
            case PILLAGER:
                return null;
            case SKELETON:
                return null;
            case SNOWBALL:
                return null;
            case VILLAGER:
                return null;
            case ENDERMITE:
                return new EntityEndermite(position);
            case MOOSHROOM:
                return new EntityMooshroom(position);
            case ILLUSIONER:
                return null;
            case IRON_GOLEM:
                return new EntityIronGolem(position);
            case ITEM_FRAME:
                return new EntityItemFrame(position, EntityItemFrame.ItemFrameOrientation.EAST);
            case LEASH_KNOT:
                return null;
            case LLAMA_SPIT:
                return null;
            case MAGMA_CUBE:
                return null;
            case POLAR_BEAR:
                return new EntityPolarBear(position);
            case PUFFERFISH:
                return null;
            case SILVERFISH:
                return new EntitySilverfish(position);
            case SNOW_GOLEM:
                return new EntitySnowman(position);
            case VINDICATOR:
                return null;
            case ARMOR_STAND:
                return new EntityArmorStand(position);
            case CAVE_SPIDER:
                return new EntityCaveSpider(position);
            case END_CRYSTAL:
                return new EntityEndCrystal(position);
            case ENDER_PEARL:
                return null;
            case ENDER_DRAGON:
                return null;
            case EVOKER_FANGS:
                return null;
            case EYE_OF_ENDER:
                return new EntityEyeOfEnder(position);
            case PIGLIN_BRUTE:
                return null;
            case TNT_MINECART:
                return null;
            case TRADER_LLAMA:
                return null;
            case WITHER_SKULL:
                return null;
            case ZOMBIE_HORSE:
                return null;
            case FALLING_BLOCK:
                return null;
            case TROPICAL_FISH:
                return null;
            case CHEST_MINECART:
                return null;
            case ELDER_GUARDIAN:
                return null;
            case EXPERIENCE_ORB:
                return new ExperienceOrb((short)0, position);
            case FISHING_BOBBER:
                return null;
            case LIGHTNING_BOLT:
                return null;
            case SHULKER_BULLET:
                return null;
            case SKELETON_HORSE:
                return null;
            case SMALL_FIREBALL:
                return null;
            case SPECTRAL_ARROW:
                return null;
            case DRAGON_FIREBALL:
                return null;
            case FIREWORK_ROCKET:
                return null;
            case HOPPER_MINECART:
                return null;
            case WITHER_SKELETON:
                return null;
            case ZOMBIE_VILLAGER:
                return null;
            case FURNACE_MINECART:
                return null;
            case SPAWNER_MINECART:
                return null;
            case WANDERING_TRADER:
                return null;
            case ZOMBIFIED_PIGLIN:
                return new EntityZombifiedPiglin(position);
            case AREA_EFFECT_CLOUD:
                return new EntityAreaEffectCloud(position);
            case EXPERIENCE_BOTTLE:
                return null;
            case COMMAND_BLOCK_MINECART:
                return null;
            default:
                return new Entity(type, position){
                    @Override
                    public void update(long time) {}
                    @Override
                    public void spawn() {}
                };
        }
    }
}
