package dev.sejtam.mineschem.core.schematic;

import dev.sejtam.mineschem.core.utils.Region;

import net.minestom.server.utils.Position;

import org.jetbrains.annotations.NotNull;

public interface ISchematic {

    ErrorMessage read();
    ErrorMessage write(@NotNull Region region);
    ErrorMessage build(@NotNull Position position, Runnable blocksCompleted);

    enum ErrorMessage {
        NoSuchFile,
        NotLoaded,
        NBTName,
        NBTWidth,
        NBTHeight,
        NBTLength,
        NBTMaxPalette,
        NBTPalette,
        PaletteNotEqualsMaxPalette,
        PaletteGetInt,
        NBTBlockData,
        NBTEntities,
        BadRead,
        BadWrite,
        VarIntSize,
        NoBlocks,
        BadMaterials,
        Instance,
        BlockBatch,
        None
    }

}
