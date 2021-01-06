package dev.sejtam.mineschem.schematic;

import kotlin.Pair;

import dev.sejtam.mineschem.utils.Region;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;

import org.jetbrains.annotations.NotNull;

import org.jglrxavpok.hephaistos.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class SpongeSchematic implements ISchematc {

    private File schematicFile;
    private Instance instance;

    // Sizes
    private Short width;
    private Short height;
    private Short length;
    private int[] offset;

    // Blocks
    private Integer maxPalette;
    private Map<String, Integer> palette = new HashMap<>();
    private byte[] blocksData;
    private List<Region.RegionBlock> blocks = new ArrayList<>();

    private boolean isLoaded = false;

    public SpongeSchematic(@NotNull String schematicName, @NotNull Instance instance) {
        this(new File("schematics/" + schematicName + ".schem"), instance);
    }
    public SpongeSchematic(@NotNull File schematicFile, @NotNull Instance instance) {
        this.schematicFile = schematicFile;
        this.instance = instance;
    }

    public ErrorMessages read() {
        if(isLoaded)
            return ErrorMessages.None;

        // Check is file exists
        if(!schematicFile.exists())
            return ErrorMessages.NoSuchFile;

        // Check is file
        if(!schematicFile.isFile())
            return ErrorMessages.NoSuchFile;

        try (NBTReader reader = new NBTReader(schematicFile, true)) {
            // Get Main NBT Name
            Pair<String, NBT> pair = reader.readNamed();
            // Get Main NBT
            NBTCompound nbtTag = (NBTCompound) pair.getSecond();
            // Is SpongeSchematic
            if(!pair.getFirst().equals("Schematic"))
                return ErrorMessages.NBTName;

            // TODO: Version checker

            // TODO: Check Data Version

            ErrorMessages errorMessages = readSizes(nbtTag);
            if(errorMessages != ErrorMessages.None)
                return errorMessages;

            errorMessages = readBlockPalette(nbtTag);
            if(errorMessages != ErrorMessages.None)
                return errorMessages;

            errorMessages = readBlocks();
            if(errorMessages != ErrorMessages.None)
                return errorMessages;

            // TODO: Block entities

            // TODO: Read entities

            // TODO: Read Biome Palette

            // TODO: Read BiomeData

        } catch (IOException | NBTException ex) {
            return ErrorMessages.BadRead;
        }

        isLoaded = true;
        return ErrorMessages.None;
    }

    private ErrorMessages readSizes(NBTCompound nbtTag) {
        // Get Width
        width = nbtTag.getShort("Width");
        if(width == null)
            return ErrorMessages.NBTWidth;

        // Get Height
        height = nbtTag.getShort("Height");
        if(height == null)
            return ErrorMessages.NBTHeight;

        // Get Length
        length = nbtTag.getShort("Length");
        if(length == null)
            return ErrorMessages.NBTLength;

        // Get offset
        offset = nbtTag.getIntArray("Offset");
        if(offset == null || offset.length != 3)
            offset = new int[] {0, 0, 0};

        return ErrorMessages.None;
    }

    private ErrorMessages readBlockPalette(NBTCompound nbtTag) {
        // Get Max Palette
        maxPalette = nbtTag.getInt("PaletteMax");
        if(maxPalette == null)
            return ErrorMessages.NBTMaxPalette;

        // Get Palette
        NBTCompound nbtPalette = (NBTCompound) nbtTag.get("Palette");
        if(nbtPalette == null)
            return ErrorMessages.NBTPalette;

        // Is Palette same size
        List<String> keys = nbtPalette.getKeys();
        if(keys.size() != maxPalette)
            return ErrorMessages.PaletteNotEqualsMaxPalette;

        // Create map from nbtPalette
        for(String key : keys) {
            Integer value = nbtPalette.getInt(key);
            if(value == null)
                return ErrorMessages.PaletteGetInt;

            palette.put(key, value);
        }

        // Sort Palette map by values
        palette = palette.entrySet().stream()
                .sorted(Comparator.comparing(Map.Entry::getValue))
                .collect(LinkedHashMap::new,(map, entry) -> map.put(entry.getKey(), entry.getValue()), LinkedHashMap::putAll);

        // Get block data
        blocksData = nbtTag.getByteArray("BlockData");
        if(blocksData == null || blocksData.length == 0)
            return ErrorMessages.NBTBlockData;

        return ErrorMessages.None;
    }

    // https://github.com/EngineHub/WorldEdit/blob/303f5a76b2df70d63480f2126c9ef4b228eb3c59/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/SpongeSchematicReader.java#L261-L297
    private ErrorMessages readBlocks() {
        int index = 0;
        int i = 0;
        int value;
        int varintLength;
        List<String> paletteKeys = new ArrayList<>(palette.keySet());

        while (i < blocksData.length) {
            value = 0;
            varintLength = 0;

            while (true) {
                value |= (blocksData[i] & 127) << (varintLength++ * 7);
                if (varintLength > 5) {
                    return ErrorMessages.VarIntSize;
                }
                if ((blocksData[i] & 128) != 128) {
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

            blocks.add(new Region.RegionBlock(
                    new BlockPosition(x, y, z),
                    stateId
            ));

            index++;
        }

        return ErrorMessages.None;
    }

    public ErrorMessages write() {
        return ErrorMessages.None;
    }


    public ErrorMessages build(@NotNull Position position) {
        if(!isLoaded)
            return ErrorMessages.NotLoaded;

        if(instance == null)
            return ErrorMessages.Instance;

        if(blocks == null || blocks.size() == 0)
            return ErrorMessages.NoBlocks;

        for (Region.RegionBlock regionBlock : blocks) {
            BlockPosition blockPosition = regionBlock.getPosition();
            short stateId = regionBlock.getStateId();

            instance.setBlockStateId(blockPosition.getX(), blockPosition.getY(), blockPosition.getZ(), stateId);
        }

        return ErrorMessages.None;
    }

    private short getStateId(String input) {
        String fullName = input.split("\\[")[0];
        String name = fullName.split(":")[1];
        Block block = Block.valueOf(name.toUpperCase());
        String states = input.replaceAll(block.getName(), "");

        if(states.startsWith("[")) {
            String[] stateArray = states.substring(1, states.length() - 1).split(",");
            return block.withProperties(stateArray);
        } else return block.getBlockId();
    }

}
