package dev.sejtam.mineschem.world;

import net.minestom.server.MinecraftServer;
import net.minestom.server.instance.*;
import net.minestom.server.instance.batch.ChunkBatch;
import net.minestom.server.world.biomes.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class VoidChunkLoader implements ChunkGenerator {

    @Override
    public void generateChunkData(@NotNull ChunkBatch batch, int chunkX, int chunkZ) {

    }

    @Override
    public void fillBiomes(@NotNull Biome[] biomes, int chunkX, int chunkZ) {
        Arrays.fill(biomes, MinecraftServer.getBiomeManager().getById(0));
    }

    @Override
    public @Nullable List<ChunkPopulator> getPopulators() {
        return null;
    }
}