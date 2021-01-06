package dev.sejtam.mineschem.schematic;

import dev.sejtam.mineschem.utils.Region;

import net.minestom.server.instance.Instance;
import net.minestom.server.utils.Position;

import org.jetbrains.annotations.NotNull;

import org.jglrxavpok.hephaistos.nbt.NBTCompound;
import org.jglrxavpok.hephaistos.nbt.NBTException;
import org.jglrxavpok.hephaistos.nbt.NBTReader;

import java.io.File;
import java.io.IOException;

public class Schematic implements ISchematic {

    private File schematicFile;
    private Instance instance;

    private ISchematic schematic;

    public Schematic(@NotNull String schematicName, @NotNull Instance instance) {
        this(new File("schematics/" + schematicName + ".schem"), instance);
    }
    public Schematic(@NotNull File schematicFile, @NotNull Instance instance) {
        this.schematicFile = schematicFile;
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

        return this.schematic.write(region);
    }

    public ErrorMessage build(@NotNull Position position) {
        if(this.schematic == null)
            this.schematic = new SpongeSchematic(this.schematicFile, this.instance);

        return this.schematic.build(position);
    }
}
