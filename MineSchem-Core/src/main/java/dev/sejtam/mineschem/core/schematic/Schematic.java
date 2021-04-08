package dev.sejtam.mineschem.core.schematic;

import dev.sejtam.mineschem.core.utils.Region;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;

import org.jetbrains.annotations.NotNull;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.File;
import java.io.IOException;

public class Schematic implements ISchematic {

    public static final String MAIN_FOLDER = "MineSchem/schematics/";

    private File schematicFile;
    private Instance instance;

    private ISchematic schematic;

    public Schematic(@NotNull String schematicName, @NotNull Instance instance) {
        this(new File(MAIN_FOLDER + schematicName + ".schem"), instance);
    }
    public Schematic(@NotNull File schematicFile, @NotNull Instance instance) {
        if(schematicFile.exists())
            this.schematicFile = schematicFile;
        else
            this.schematicFile = new File(schematicFile.getPath() + "atic");
        this.instance = instance;
    }

    public ErrorMessage read() {
        // Check is file exists
        if(!this.schematicFile.exists())
            return ErrorMessage.NoSuchFile;

        // Check is file
        if(!this.schematicFile.isFile())
            return ErrorMessage.NoSuchFile;

        try (NBTReader reader = new NBTReader(this.schematicFile, true)) {
            // Get Main NBT
            NBTCompound nbtTag = (NBTCompound) reader.readNamed().component2();

            // If contains Version
            if(nbtTag.getInt("Version") == null)
                this.schematic = new MCEditSchematic(this.schematicFile, this.instance);
            else
                this.schematic = new SpongeSchematic(this.schematicFile, this.instance);

        } catch (IOException | NBTException ex) {
            return ErrorMessage.BadRead;
        }

        return this.schematic.read();
    }

    public ErrorMessage write(@NotNull Region region) {
        if(this.schematic == null)
            this.schematic = new SpongeSchematic(this.schematicFile, this.instance);

        region.setInstance(this.instance);
        return this.schematic.write(region);
    }

    public ErrorMessage build(@NotNull Position position, Runnable blocksCompleted) {
        if(this.schematic == null)
            this.schematic = new SpongeSchematic(this.schematicFile, this.instance);

        return this.schematic.build(position, blocksCompleted);
    }
}
