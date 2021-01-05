package dev.sejtam.mineschem;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.event.player.PlayerSpawnEvent;
import net.minestom.server.network.ConnectionManager;
import net.minestom.server.utils.Position;

public class PlayerInit {


    public static void init() {
        ConnectionManager connectionManager = MinecraftServer.getConnectionManager();
        connectionManager.addPlayerInitialization(player -> {

            player.addEventCallback(PlayerLoginEvent.class, event -> {
                event.setSpawningInstance(GameServer.overworld);

                player.setRespawnPoint(new Position(0, 100, (float) 0, 180, 0));
            });

            player.addEventCallback(PlayerSpawnEvent.class, event -> event.getPlayer().setGameMode(GameMode.CREATIVE));
        });

    }
}
