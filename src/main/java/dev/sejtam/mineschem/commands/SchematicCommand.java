package dev.sejtam.mineschem.commands;

import dev.sejtam.mineschem.schematic.ISchematc;
import dev.sejtam.mineschem.schematic.SpongeSchematic;

import dev.sejtam.mineschem.utils.Region;
import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
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

        Argument<?> type = ArgumentType.Word("type").from("pos1", "pos2", "load", "save");
        Argument<?> fileName = ArgumentType.Word("fileName");

        setArgumentCallback((player, argument, error) -> usage(player, null), type);

        addSyntax((sender, arguments) -> {
            Player player = (Player) sender;
            String arg = arguments.getWord("type");

            switch (arg) {
                case "load":
                        sender.sendMessage(ChatColor.RED + "Schematic loading...");

                        SpongeSchematic loadSchematic = new SpongeSchematic(arguments.getWord("fileName"), player.getInstance());
                        loadSchematic.read();

                        ISchematc.ErrorMessage errorMessage = loadSchematic.build(player.getPosition());
                        if(errorMessage == ISchematc.ErrorMessage.None)
                            sender.sendMessage(ChatColor.CYAN + "Schematic loaded!");
                        else
                            sender.sendMessage(ChatColor.RED + errorMessage.toString() + " - Error!");
                    break;

                case "save":
                        sender.sendMessage(ChatColor.RED + "Schematic saving...");

                        UUID uuid = player.getUuid();
                        if(!this.pos1.containsKey(uuid)) {
                            sender.sendMessage(ChatColor.RED + "Pos1 not set!");
                            break;
                        }

                        if(!this.pos2.containsKey(uuid)) {
                            sender.sendMessage(ChatColor.RED + "Pos2 not set!");
                            break;
                        }

                        SpongeSchematic writeSchematic = new SpongeSchematic(arguments.getWord("fileName"), player.getInstance());
                        Region region = new Region(pos1.get(uuid), pos2.get(uuid));

                        ISchematc.ErrorMessage errorMessage1 = writeSchematic.write(region);
                        if(errorMessage1 == ISchematc.ErrorMessage.None)
                            sender.sendMessage(ChatColor.CYAN + "Schematic saved!");
                        else
                            sender.sendMessage(ChatColor.RED + errorMessage1.toString() + " - Error!");
                    break;
            }
        }, type, fileName);

        addSyntax((sender, arguments) -> {
            Player player = (Player) sender;
            String arg = arguments.getWord("type");

            switch (arg) {
                case "pos1":
                        BlockPosition firstPosition = player.getTargetBlockPosition(10);
                        if (firstPosition == null) {
                            player.sendMessage(ChatColor.CYAN + "No block detected!");
                            break;
                        }

                        this.pos1.put(player.getUuid(), firstPosition);
                        player.sendMessage(ChatColor.CYAN + "Pos1 set! " + firstPosition.toString());
                    break;

                case "pos2":
                        BlockPosition secoundPosition = player.getTargetBlockPosition(10);
                        if (secoundPosition == null) {
                            player.sendMessage(ChatColor.CYAN + "No block detected!");
                            break;
                        }

                        this.pos2.put(player.getUuid(), secoundPosition);
                        player.sendMessage(ChatColor.CYAN + "Pos2 set! " + secoundPosition.toString());
                    break;
            }
        }, type);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage(ChatColor.CYAN + "--- " + ChatColor.BOLD + ChatColor.GOLD + "Schematics" + ChatColor.RESET + ChatColor.CYAN + " ---");
        sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.CYAN + "/schematic pos1");
        sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.CYAN + "/schematic pos2");
        sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.CYAN + "/schematic load <fileName>");
        sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.CYAN + "/schematic save <fileName>");
    }

}
