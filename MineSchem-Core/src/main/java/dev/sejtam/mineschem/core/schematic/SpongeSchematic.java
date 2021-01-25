package dev.sejtam.mineschem.core.schematic;

import dev.sejtam.mineschem.core.utils.EntityUtils;
import dev.sejtam.mineschem.core.utils.ItemStackUtils;
import kotlin.Pair;

import dev.sejtam.mineschem.core.utils.Region;

import net.minestom.server.chat.ChatParser;
import net.minestom.server.entity.*;
import net.minestom.server.entity.type.decoration.EntityArmorStand;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;
import net.minestom.server.utils.Vector;

import org.jetbrains.annotations.NotNull;

import org.jglrxavpok.hephaistos.nbt.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class SpongeSchematic implements ISchematic {

    private File schematicFile;
    private Instance instance;
    private BlockBatch blockBatch;

    private Integer version = 2;

    // Sizes
    private Short width;
    private Short height;
    private Short length;
    private int[] offset;

    // Blocks
    private Integer maxPalette;
    private Map<String, Integer> palette = new HashMap<>();
    private byte[] blocksData;
    private List<Region.RegionBlock> regionBlocks = new ArrayList<>();

    //Entities
    private List<Entity> entities = new ArrayList<>();

    private boolean isLoaded = false;

    public SpongeSchematic(@NotNull String schematicName, @NotNull Instance instance) {
        this(new File(Schematic.MAIN_FOLDER + schematicName + ".schem"), instance);
    }
    public SpongeSchematic(@NotNull File schematicFile, @NotNull Instance instance) {
        this.schematicFile = schematicFile;
        this.instance = instance;
    }

    public ErrorMessage read() {
        if(this.isLoaded)
            return ErrorMessage.None;

        // Check is file exists
        if(!this.schematicFile.exists())
            return ErrorMessage.NoSuchFile;

        // Check is file
        if(!this.schematicFile.isFile())
            return ErrorMessage.NoSuchFile;

        try (NBTReader reader = new NBTReader(this.schematicFile, true)) {
            // Get Main NBT Name
            Pair<String, NBT> pair = reader.readNamed();
            // Get Main NBT
            NBTCompound nbtTag = (NBTCompound) pair.getSecond();
            // Is SpongeSchematic
            if(!pair.getFirst().equals("Schematic"))
                return ErrorMessage.NBTName;

            this.version = nbtTag.getInt("Version");
            if(this.version == null)
                this.version = 2;

            // TODO: Read and Check Data Version

            ErrorMessage errorMessage = readSizes(nbtTag);
            if(errorMessage != ErrorMessage.None)
                return errorMessage;

            errorMessage = readBlockPalette(nbtTag);
            if(errorMessage != ErrorMessage.None)
                return errorMessage;

            // TODO: Read block data
            errorMessage = readBlocks();
            if(errorMessage != ErrorMessage.None)
                return errorMessage;

            if(this.version != 2)
                return ErrorMessage.None;

            errorMessage = readEntities(nbtTag);
            if(errorMessage != ErrorMessage.None)
                return ErrorMessage.NBTEntities;

            // TODO: Read Biome Palette

            // TODO: Read BiomeData

        } catch (IOException | NBTException ex) {
            return ErrorMessage.BadRead;
        }

        this.isLoaded = true;
        return ErrorMessage.None;
    }

    private ErrorMessage readSizes(NBTCompound nbtTag) {
        // Get Width
        this.width = nbtTag.getShort("Width");
        if(this.width == null)
            return ErrorMessage.NBTWidth;

        // Get Height
        this.height = nbtTag.getShort("Height");
        if(this.height == null)
            return ErrorMessage.NBTHeight;

        // Get Length
        this.length = nbtTag.getShort("Length");
        if(this.length == null)
            return ErrorMessage.NBTLength;

        // Get offset
        this.offset = nbtTag.getIntArray("Offset");
        if(this.offset == null || this.offset.length != 3)
            this.offset = new int[] {0, 0, 0};

        return ErrorMessage.None;
    }

    private ErrorMessage readBlockPalette(NBTCompound nbtTag) {
        // Get Max Palette
        this.maxPalette = nbtTag.getInt("PaletteMax");
        if(this.maxPalette == null)
            return ErrorMessage.NBTMaxPalette;

        // Get Palette
        NBTCompound nbtPalette = (NBTCompound) nbtTag.get("Palette");
        if(nbtPalette == null)
            return ErrorMessage.NBTPalette;

        // Is Palette same size
        List<String> keys = nbtPalette.getKeys();
        if(keys.size() != maxPalette)
            return ErrorMessage.PaletteNotEqualsMaxPalette;

        // Create map from nbtPalette
        for(String key : keys) {
            Integer value = nbtPalette.getInt(key);
            if(value == null)
                return ErrorMessage.PaletteGetInt;

            this.palette.put(key, value);
        }

        // Sort Palette map by values
        this.palette = this.palette.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .collect(LinkedHashMap::new,(map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        // Get block data
        this.blocksData = nbtTag.getByteArray("BlockData");
        if(this.blocksData == null || this.blocksData.length == 0)
            return ErrorMessage.NBTBlockData;

        return ErrorMessage.None;
    }

    // https://github.com/EngineHub/WorldEdit/blob/303f5a76b2df70d63480f2126c9ef4b228eb3c59/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/SpongeSchematicReader.java#L261-L297
    private ErrorMessage readBlocks() {
        int index = 0;
        int i = 0;
        int value;
        int varintLength;
        List<String> paletteKeys = new ArrayList<>(palette.keySet());

        while (i < this.blocksData.length) {
            value = 0;
            varintLength = 0;

            while (true) {
                value |= (this.blocksData[i] & 127) << (varintLength++ * 7);
                if (varintLength > 5) {
                    return ErrorMessage.VarIntSize;
                }
                if ((this.blocksData[i] & 128) != 128) {
                    i++;
                    break;
                }
                i++;
            }

            // Offset is not needed
            int x = (index % (width * length)) % width
                    //- offset[0]
                    ;
            int y = index / (width * length)
                    //- offset[1]
                    ;
            int z = (index % (width * length)) / width
                    //- offset[2]
                    ;

            String block = paletteKeys.get(value);
            short stateId = getStateId(block);

            this.regionBlocks.add(new Region.RegionBlock(
                    new BlockPosition(x, y, z),
                    stateId
            ));

            index++;
        }

        return ErrorMessage.None;
    }

    private ErrorMessage readEntities(NBTCompound nbtTag) {
        NBTList<NBT> nbtEntityList = nbtTag.getList("Entities");
        if(nbtEntityList == null || nbtEntityList.getLength() == 0)
            return ErrorMessage.None;

        for (NBT nbtEntity : nbtEntityList) {
            if(!(nbtEntity instanceof NBTCompound))
                continue;

            NBTCompound entityTag = (NBTCompound) nbtEntity;
            String id = entityTag.getString("Id");

            String fullName = id.split("\\[")[0];
            String name = fullName.split(":")[1];

            EntityType entityType = EntityType.valueOf(name.toUpperCase());
            Position position = new Position(0, 0, 0, 0, 0);
            NBTList<NBTDouble> positionNbt = entityTag.getList("Pos");
            NBTList<NBTFloat> rotationNbt = entityTag.getList("Rotation");

            if(positionNbt != null) {
                position.setX((float)positionNbt.get(0).getValue() - offset[0]);
                position.setY((float)positionNbt.get(1).getValue() - offset[1]);
                position.setZ((float)positionNbt.get(2).getValue() - offset[2]);
            }

            if(rotationNbt != null) {
                position.setYaw(rotationNbt.get(0).getValue());
                position.setPitch(rotationNbt.get(1).getValue());
            }

            Entity entity = EntityUtils.getEntity(entityType, position);
            if(entity == null)
                continue;

            String customName = entityTag.getString("CustomName");
            if(customName != null)
                entity.setCustomName(ChatParser.toColoredText(customName));

            Integer customNameVisible = entityTag.getAsInt("CustomNameVisible");
            if(customNameVisible != null)
                entity.setCustomNameVisible(customNameVisible == 1);

            Integer noGravity = entityTag.getAsInt("NoGravity");
            if(noGravity != null)
                entity.setNoGravity(noGravity == 1);

            Integer glowing = entityTag.getAsInt("Glowing");
            if(glowing != null)
                entity.setGlowing(glowing == 1);

            Integer silent = entityTag.getAsInt("Silent");
            if(silent != null)
                entity.setSilent(silent == 1);

            Integer invisible = entityTag.getAsInt("Invisible");
            if(invisible != null)
                entity.setInvisible(invisible == 1);

            // Living Entity
            if(entity instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity) entity;

                Integer invulnerable = entityTag.getAsInt("Invulnerable");
                if(invulnerable != null)
                    livingEntity.setInvulnerable(invulnerable == 1);

                Float health = entityTag.getFloat("Health");
                if(health != null)
                    livingEntity.setHealth(health);
            }

            // Entity creature
            if(entity instanceof EntityCreature) {
                EntityCreature entityCreature = (EntityCreature) entity;

                // Armor
                NBTList<NBTCompound> nbtArmorItems = ((NBTCompound) nbtEntity).getList("ArmorItems");

                // Boots
                entityCreature.setBoots(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(0)));

                // Leggings
                entityCreature.setLeggings(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(1)));

                // Chestplate
                entityCreature.setChestplate(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(2)));

                // Helmet
                entityCreature.setHelmet(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(3)));


                // Hands
                NBTList<NBTCompound> nbtHandItems = ((NBTCompound) nbtEntity).getList("HandItems");

                // Main Hand
                entityCreature.setItemInMainHand(ItemStackUtils.getItemStackFromNBT(nbtHandItems.get(0)));

                // Off Hand Hand
                entityCreature.setItemInOffHand(ItemStackUtils.getItemStackFromNBT(nbtHandItems.get(1)));
            }

            // Armor stand
            if(entity instanceof EntityArmorStand) {
                EntityArmorStand entityArmorStand = (EntityArmorStand) entity;

                // Armor
                NBTList<NBTCompound> nbtArmorItems = ((NBTCompound) nbtEntity).getList("ArmorItems");

                // Boots
                entityArmorStand.setBoots(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(0)));

                // Leggings
                entityArmorStand.setLeggings(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(1)));

                // Chestplate
                entityArmorStand.setChestplate(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(2)));

                // Helmet
                entityArmorStand.setHelmet(ItemStackUtils.getItemStackFromNBT(nbtArmorItems.get(3)));


                // Hands
                NBTList<NBTCompound> nbtHandItems = ((NBTCompound) nbtEntity).getList("HandItems");

                // Main Hand
                entityArmorStand.setItemInMainHand(ItemStackUtils.getItemStackFromNBT(nbtHandItems.get(0)));

                // Off Hand Hand
                entityArmorStand.setItemInOffHand(ItemStackUtils.getItemStackFromNBT(nbtHandItems.get(1)));


                // Pose
                NBTCompound pose = (NBTCompound) entityTag.get("Pose");
                if(pose != null) {
                    // Head
                    NBTList<NBTFloat> nbtHead = pose.getList("Head");
                    if (nbtHead != null)
                        if(nbtHead.getLength() == 3)
                            entityArmorStand.setHeadRotation(new Vector(nbtHead.get(0).getValue(), nbtHead.get(1).getValue(), nbtHead.get(2).getValue()));

                    // Body
                    NBTList<NBTFloat> nbtBody = pose.getList("Body");
                    if (nbtBody != null)
                        if(nbtBody.getLength() == 3)
                            entityArmorStand.setBodyRotation(new Vector(nbtBody.get(0).getValue(), nbtBody.get(1).getValue(), nbtBody.get(2).getValue()));

                    // Left Arm
                    NBTList<NBTFloat> nbtLeftArm = pose.getList("LeftArm");
                    if (nbtLeftArm != null)
                        if(nbtLeftArm.getLength() == 3)
                            entityArmorStand.setLeftLegRotation(new Vector(nbtLeftArm.get(0).getValue(), nbtLeftArm.get(1).getValue(), nbtLeftArm.get(2).getValue()));

                    // Right Arm
                    NBTList<NBTFloat> nbtRightArm = pose.getList("RightArm");
                    if (nbtRightArm != null)
                        if(nbtRightArm.getLength() == 3)
                            entityArmorStand.setLeftLegRotation(new Vector(nbtRightArm.get(0).getValue(), nbtRightArm.get(1).getValue(), nbtRightArm.get(2).getValue()));

                    // Left Leg
                    NBTList<NBTFloat> nbtLeftLeg = pose.getList("LeftLeg");
                    if (nbtLeftLeg != null)
                        if(nbtLeftLeg.getLength() == 3)
                            entityArmorStand.setLeftLegRotation(new Vector(nbtLeftLeg.get(0).getValue(), nbtLeftLeg.get(1).getValue(), nbtLeftLeg.get(2).getValue()));

                    // Right Leg
                    NBTList<NBTFloat> nbtRightLeg = pose.getList("RightLeg");
                    if (nbtRightLeg != null)
                        if(nbtRightLeg.getLength() == 3)
                            entityArmorStand.setLeftLegRotation(new Vector(nbtRightLeg.get(0).getValue(), nbtRightLeg.get(1).getValue(), nbtRightLeg.get(2).getValue()));

                }

                Integer noBasePlate = entityTag.getAsInt("NoBasePlate");
                if(noBasePlate != null)
                    entityArmorStand.setNoBasePlate(noBasePlate == 1);

                Integer small = entityTag.getAsInt("Small");
                if(small != null)
                    entityArmorStand.setSmall(small == 1);

                Integer showArms = entityTag.getAsInt("ShowArms");
                if(showArms != null)
                    entityArmorStand.setHasArms(showArms == 1);

                Integer marker = entityTag.getAsInt("Marker");
                if(marker != null)
                    entityArmorStand.setMarker(marker == 1);
            }

            // TODO: Item Frame

            entities.add(entity);
        }

        return ErrorMessage.None;
    }

    public ErrorMessage write(@NotNull Region region) {
        NBTCompound nbtTag = new NBTCompound();

        // Set Version
        nbtTag.setInt("Version", this.version);

        // TODO: Write Data entities

        ErrorMessage errorMessage = writeSizes(nbtTag, region);
        if(errorMessage != ErrorMessage.None)
            return errorMessage;

        errorMessage = writeBlockPalette(nbtTag, region);
        if(errorMessage != ErrorMessage.None)
            return errorMessage;

        // TODO: Write Block entities

        // TODO: Write entities

        // TODO: Write Biome Palette

        // TODO: Write BiomeData

        try(NBTWriter writer = new NBTWriter(this.schematicFile, true)) {
            writer.writeNamed("Schematic", nbtTag);
        } catch (IOException ex) {
            return ErrorMessage.BadWrite;
        }

        return ErrorMessage.None;
    }

    private ErrorMessage writeSizes(NBTCompound nbtTag, Region region) {
        // Set Width
        nbtTag.setShort("Width", (short)region.getSizeX());

        // Set Height
        nbtTag.setShort("Height", (short)region.getSizeY());

        // Set Length
        nbtTag.setShort("Length", (short)region.getSizeZ());

        // Set offset
        BlockPosition lower = region.getLower();
        nbtTag.setIntArray("Offset", new int[] {
                lower.getX(),
                lower.getY(),
                lower.getZ()
        });

        return ErrorMessage.None;
    }

    private ErrorMessage writeBlockPalette(NBTCompound nbtTag, Region region) {
        // Generate Palette
        int paletteMax = 0;
        Map<String, Integer> palette = new HashMap<>();

        ByteArrayOutputStream buffer = new ByteArrayOutputStream(region.getSizeX() * region.getSizeY() * region.getSizeZ());

        Iterator<Region.RegionBlock> regionIterator = region.iterator();
        while(regionIterator.hasNext()) {
            Region.RegionBlock regionBlock = regionIterator.next();
            short stateId = this.instance.getBlockStateId(regionBlock.getPosition());
            Block block = Block.fromStateId(stateId);
            String name = block.getName();

            int blockId;
            if (palette.containsKey(name)) {
                blockId = palette.get(name);
            } else {
                blockId = paletteMax;
                palette.put(name, blockId);
                paletteMax++;
            }

            while ((blockId & -128) != 0) {
                buffer.write(blockId & 127 | 128);
                blockId >>>= 7;
            }
            buffer.write(blockId);
        }

        // Set PaletteMax
        nbtTag.setInt("PaletteMax", paletteMax);

        // Palette items to NBTTag
        NBTCompound paletteItems = new NBTCompound();
        palette.forEach((key, value) -> paletteItems.setInt(key, value));

        // Set Palette
        nbtTag.set("Palette", paletteItems);

        // Set Block Data
        nbtTag.setByteArray("BlockData", buffer.toByteArray());

        return ErrorMessage.None;
    }


    public ErrorMessage build(@NotNull Position position, Runnable blocksCompleted) {
        if(!this.isLoaded)
            return ErrorMessage.NotLoaded;

        if(this.instance == null)
            return ErrorMessage.Instance;

        if(this.regionBlocks == null || this.regionBlocks.size() == 0)
            return ErrorMessage.NoBlocks;

        this.blockBatch = this.instance.createBlockBatch();

        for (Region.RegionBlock regionBlock : this.regionBlocks) {
            BlockPosition blockPosition = regionBlock.getPosition();
            short stateId = regionBlock.getStateId();

            this.blockBatch.setBlockStateId(blockPosition.getX() + (int)position.getX(), blockPosition.getY() + (int)position.getY(), blockPosition.getZ() + (int)position.getZ(), stateId);
        }

        this.blockBatch.flush(blocksCompleted);

        for(Entity entity : entities) {
            Position entityPosition = entity.getPosition();
            entityPosition.setX(entityPosition.getX() + position.getX());
            entityPosition.setY(entityPosition.getY() + position.getY());
            entityPosition.setZ(entityPosition.getZ() + position.getZ());

            entity.setInstance(this.instance);
        }

        return ErrorMessage.None;
    }

    private Block getBlock(String input) {
        String fullName = input.split("\\[")[0];
        String name = fullName.split(":")[1];

        return Block.valueOf(name.toUpperCase());
    }

    private short getStateId(String input) {
        Block block = getBlock(input);
        String states = input.replaceAll(block.getName(), "");

        if(states.startsWith("[")) {
            String[] stateArray = states.substring(1, states.length() - 1).split(",");
            return block.withProperties(stateArray);
        } else return block.getBlockId();
    }

}
