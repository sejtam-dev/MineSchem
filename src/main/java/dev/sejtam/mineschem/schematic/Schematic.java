package dev.sejtam.mineschem.schematic;

import kotlin.Pair;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.utils.Position;

import org.jetbrains.annotations.NotNull;

import org.jglrxavpok.hephaistos.nbt.*;

import java.io.Console;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class Schematic {

    private File schematicFile;
    private Instance instance;

    private Short width;
    private Short height;
    private Short length;
    private int[] offset;

    private Integer maxPalette;
    private Map<String, Integer> palette = new HashMap<>();
    private byte[] blocksData;

    private boolean isLoaded = false;

    public Schematic(@NotNull String schematicName, @NotNull Instance instance) {
        this(new File("schematics/" + schematicName + ".schem"), instance);
    }
    public Schematic(@NotNull File schematicFile, @NotNull Instance instance) {
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
            // Is Schematic
            if(!pair.getFirst().equals("Schematic"))
                return ErrorMessages.NBTName;

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

        } catch (IOException | NBTException ex) {
            return ErrorMessages.BadRead;
        }

        isLoaded = true;
        return ErrorMessages.None;
    }

    public ErrorMessages build(@NotNull Position position) {
        if(!isLoaded) {
            ErrorMessages error = read();
            if(error != ErrorMessages.None)
                return error;
        }

        if(instance == null)
            return ErrorMessages.Instance;

        // https://github.com/EngineHub/WorldEdit/blob/303f5a76b2df70d63480f2126c9ef4b228eb3c59/worldedit-core/src/main/java/com/sk89q/worldedit/extent/clipboard/io/SpongeSchematicReader.java#L261-L297
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
                    + (int) position.getX();
            int y = index / (width * length)
                    //- offset[1]
                    + (int)position.getY();
            int z = (index % (width * length)) / width
                    //- offset[2]
                    + (int) position.getZ();

            String blockInput = paletteKeys.get(value);
            Block block = getBlockFromString(blockInput);

            if(blockInput.contains("["))
                instance.setBlockStateId(x, y, z, getStateId(block, blockInput));
            else
                instance.setBlock(x, y, z, block);

            index++;
        }
        return ErrorMessages.None;
    }

    public short getStateId(Block block, String input) {
        String states = input.replaceAll(block.getName(), "");

        if(states.startsWith("[")) {
            String[] stateArray = states.substring(1, states.length() - 1).split(",");
            return block.withProperties(stateArray);
        } else return 0;
    }

    public Block getBlockFromString(String input) {
        String fullName = input.split("\\[")[0];
        String name = fullName.replaceAll("minecraft:", "");

        return Block.valueOf(name.toUpperCase());
    }

    public enum ErrorMessages {
        NoSuchFile,
        NBTName,
        NBTWidth,
        NBTHeight,
        NBTLength,
        NBTMaxPalette,
        NBTPalette,
        PaletteNotEqualsMaxPalette,
        PaletteGetInt,
        NBTBlockData,
        BadRead,
        VarIntSize,
        Instance,
        None
    }

}
