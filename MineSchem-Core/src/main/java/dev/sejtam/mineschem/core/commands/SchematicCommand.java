package dev.sejtam.mineschem.core.commands;

import dev.sejtam.mineschem.core.schematic.ISchematic;
import dev.sejtam.mineschem.core.schematic.Schematic;
import dev.sejtam.mineschem.core.utils.Region;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandContext;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;
import net.minestom.server.utils.BlockPosition;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SchematicCommand extends Command {

    public Map<UUID, BlockPosition> pos1 = new HashMap<>();
    public Map<UUID, BlockPosition> pos2 = new HashMap<>();

    public SchematicCommand() {
        super("schematic");

        setCondition((sender, commandName) -> sender.isPlayer());
        setDefaultExecutor(this::usage);

        var type = ArgumentType.Word("type").from("pos1", "pos2", "load", "save");
        var fileName = ArgumentType.String("fileName");

        setArgumentCallback((player, argument) -> usage(player, null), type);

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String arg = context.get(type);

            switch (arg) {
                case "load":
                        sender.sendMessage("Schematic loading...");

                        Schematic loadSchematic = new Schematic(context.get(fileName), player.getInstance());
                        ISchematic.ErrorMessage errorMessage = loadSchematic.read();
                        if(errorMessage != ISchematic.ErrorMessage.None)
                            sender.sendMessage(errorMessage.toString() + " - Error!");

                        errorMessage = loadSchematic.build(player.getPosition(), () -> sender.sendMessage("Schematic built!"));
                        if(errorMessage != ISchematic.ErrorMessage.None)
                            sender.sendMessage(errorMessage.toString() + " - Error!");

                    break;

                case "save":
                        sender.sendMessage("Schematic saving...");

                        UUID uuid = player.getUuid();
                        if(!this.pos1.containsKey(uuid)) {
                            sender.sendMessage("Pos1 not set!");
                            break;
                        }

                        if(!this.pos2.containsKey(uuid)) {
                            sender.sendMessage("Pos2 not set!");
                            break;
                        }

                        Schematic writeSchematic = new Schematic(context.get(fileName), player.getInstance());
                        Region region = new Region(this.pos1.get(uuid), this.pos2.get(uuid), player.getInstance());

                        ISchematic.ErrorMessage errorMessage1 = writeSchematic.write(region);
                        if(errorMessage1 == ISchematic.ErrorMessage.None)
                            sender.sendMessage("Schematic saved!");
                        else
                            sender.sendMessage(errorMessage1.toString() + " - Error!");
                    break;
            }
        }, type, fileName);

        addSyntax((sender, context) -> {
            Player player = (Player) sender;
            String arg = context.get(type);

            switch (arg) {
                case "pos1":
                        BlockPosition firstPosition = player.getTargetBlockPosition(10);
                        if (firstPosition == null) {
                            player.sendMessage("No block detected!");
                            break;
                        }

                        this.pos1.put(player.getUuid(), firstPosition);
                        player.sendMessage("Pos1 set! " + firstPosition.toString());
                    break;

                case "pos2":
                        BlockPosition secoundPosition = player.getTargetBlockPosition(10);
                        if (secoundPosition == null) {
                            player.sendMessage("No block detected!");
                            break;
                        }

                        this.pos2.put(player.getUuid(), secoundPosition);
                        player.sendMessage("Pos2 set! " + secoundPosition.toString());
                    break;
            }
        }, type);
    }

    private void usage(CommandSender sender, CommandContext context) {
        sender.sendMessage("--- Schematics ---");
        sender.sendMessage("- /schematic pos1");
        sender.sendMessage("- /schematic pos2");
        sender.sendMessage("- /schematic load <fileName>");
        sender.sendMessage("- /schematic save <fileName>");
    }

}
