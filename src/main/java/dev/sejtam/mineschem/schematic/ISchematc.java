package dev.sejtam.mineschem.schematic;

import net.minestom.server.utils.Position;
import org.jetbrains.annotations.NotNull;

public interface ISchematc {

    ErrorMessages read();
    ErrorMessages write();
    ErrorMessages build(@NotNull Position position);

    enum ErrorMessages {
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
        BadRead,
        VarIntSize,
        NoBlocks,
        Instance,
        None
    }

}
