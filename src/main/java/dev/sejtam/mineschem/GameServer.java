package dev.sejtam.mineschem;

import dev.sejtam.mineschem.commands.SchematicCommand;
import dev.sejtam.mineschem.world.AnvilChunkLoader;
import dev.sejtam.mineschem.world.VoidChunkLoader;
import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.extras.optifine.OptifineSupport;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.storage.StorageManager;
import net.minestom.server.storage.systems.FileStorageSystem;

public class GameServer {

    public static InstanceContainer overworld;

    public static void main(String[] args) {
        //Minestom Init
        MinecraftServer minecraftServer = MinecraftServer.init();

        CommandManager commandManager = MinecraftServer.getCommandManager();
        commandManager.register(new SchematicCommand());

        //Optifine
        OptifineSupport.enable();

        //Storage init
        StorageManager storageManager = MinecraftServer.getStorageManager();
        storageManager.defineDefaultStorageSystem(FileStorageSystem::new);

        //Overworld init
        overworld = MinecraftServer.getInstanceManager().createInstanceContainer();
        overworld.enableAutoChunkLoad(true);
        overworld.setChunkGenerator(new VoidChunkLoader());
        overworld.setChunkLoader(new AnvilChunkLoader(storageManager.getLocation("region")));

        //Player init
        PlayerInit.init();

        //Mojang auth init
        MojangAuth.init();

        //Start server
        minecraftServer.start("localhost", 25565);
    }

}
