package dev.sejtam.mineschem.schematic;

import kotlin.Pair;

import dev.sejtam.mineschem.utils.Region;

import net.minestom.server.instance.Instance;
import net.minestom.server.instance.batch.BlockBatch;
import net.minestom.server.utils.BlockPosition;
import net.minestom.server.utils.Position;

import org.jetbrains.annotations.NotNull;
import org.jglrxavpok.hephaistos.nbt.*;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class MCEditSchematic implements ISchematic {

    private File schematicFile;
    private Instance instance;
    private BlockBatch blockBatch;

    private Integer version = 2;

    // Sizes
    private Short width;
    private Short height;
    private Short length;

    // Blocks
    private String materials;
    private byte[] blockId;
    private byte[] blocksData;
    private byte[] addId;
    private short[] blocks;
    private List<Region.RegionBlock> regionBlocks = new ArrayList<>();

    private boolean isLoaded = false;

    public MCEditSchematic(@NotNull String schematicName, @NotNull Instance instance) {
        this(new File("schematics/" + schematicName + ".schem"), instance);
    }
    public MCEditSchematic(@NotNull File schematicFile, @NotNull Instance instance) {
        this.schematicFile = schematicFile;
        this.instance = instance;
    }

    // https://github.com/EngineHub/WorldEdit/blob/version/5.x/src/main/java/com/sk89q/worldedit/schematic/MCEditSchematicFormat.java
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

            if(!nbtTag.containsKey("Blocks"))
                return ErrorMessage.NoBlocks;

            ErrorMessage errorMessage = readSizes(nbtTag);
            if(errorMessage != ErrorMessage.None)
                return errorMessage;

            errorMessage = readBlocksData(nbtTag);
            if(errorMessage != ErrorMessage.None)
                return errorMessage;

            errorMessage = readBlocks();
            if(errorMessage != ErrorMessage.None)
                return errorMessage;

            // TODO: Read tile entities
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

        return ErrorMessage.None;
    }


    private ErrorMessage readBlocksData(NBTCompound nbtTag) {
        // Check materials
        this.materials = nbtTag.getString("Materials");
        if(!this.materials.equals("Alpha"))
            return ErrorMessage.BadMaterials;

        this.blockId = nbtTag.getByteArray("Blocks");
        if(this.blockId == null || this.blockId.length == 0)
            return ErrorMessage.NoBlocks;

        this.blocksData = nbtTag.getByteArray("Data");
        if(this.blocksData == null || this.blocksData.length == 0)
            return ErrorMessage.NBTBlockData;

        this.addId = new byte[0];
        if(nbtTag.containsKey("AddBlocks"))
            this.addId = nbtTag.getByteArray("AddBlocks");

        for (int index = 0; index < this.blockId.length; index++) {
            if ((index >> 1) >= this.addId.length) {
                this.blocks[index] = (short) (this.blockId[index] & 0xFF);
            } else {
                if ((index & 1) == 0) {
                    this.blocks[index] = (short) (((this.addId[index >> 1] & 0x0F) << 8) + (this.blockId[index] & 0xFF));
                } else {
                    this.blocks[index] = (short) (((this.addId[index >> 1] & 0xF0) << 4) + (this.blockId[index] & 0xFF));
                }
            }
        }

        return ErrorMessage.None;
    }

    public ErrorMessage readBlocks() {
        for (int x = 0; x < this.width; ++x) {
            for (int y = 0; y < this.height; ++y) {
                for (int z = 0; z < this.length; ++z) {
                    int index = y * this.width * this.length + z * this.width + x;

                    // TODO: Test it. Idk it is working. Idk how to convert BlockId and BlockData into the stateId
                    short stateId = (short)(this.blocks[index] << 8 | this.blocksData[index]);

                    this.regionBlocks.add(new Region.RegionBlock(
                            new BlockPosition(x, y, z),
                            stateId
                    ));
                }
            }
        }

        return ErrorMessage.None;
    }

    // TODO: Write
    public ErrorMessage write(@NotNull Region region) {
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
}
