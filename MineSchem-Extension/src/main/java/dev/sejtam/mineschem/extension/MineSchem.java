package dev.sejtam.mineschem.extension;

import dev.sejtam.mineschem.core.commands.SchematicCommand;

import net.minestom.server.MinecraftServer;
import net.minestom.server.command.CommandManager;
import net.minestom.server.extensions.Extension;

public class MineSchem extends Extension {

    @Override
    public void initialize() {
        CommandManager commandManager = MinecraftServer.getCommandManager();

        if(commandManager.getCommand("schematic") == null)
            MinecraftServer.getCommandManager().register(new SchematicCommand());

        System.out.println("[MineSchem] Loaded!");
    }

    @Override
    public void terminate() {
        MinecraftServer.getCommandManager().unregister(new SchematicCommand());

        System.out.println("[MineSchem] UnLoaded!");
    }

}
