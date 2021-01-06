package dev.sejtam.mineschem.schematic;

import kotlin.Pair;

import dev.sejtam.mineschem.utils.Region;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;

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

    private boolean isLoaded = false;

    public SpongeSchematic(@NotNull String schematicName, @NotNull Instance instance) {
        this(new File("schematics/" + schematicName + ".schem"), instance);
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

            errorMessage = readBlocks();
            if(errorMessage != ErrorMessage.None)
                return errorMessage;

            // TODO: Read Block entities

            // TODO: Read entities

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


    public ErrorMessage build(@NotNull Position position) {
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

        this.blockBatch.flush(() -> {});

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
