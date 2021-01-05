package dev.sejtam.mineschem.commands;

import dev.sejtam.mineschem.schematic.Schematic;

import net.minestom.server.chat.ChatColor;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.Arguments;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.entity.Player;

public class SchematicCommand extends Command {

    public SchematicCommand() {
        super("schematic");

        setCondition((sender, commandName) -> sender.isPlayer());
        setDefaultExecutor(this::usage);

        Argument<?> type = ArgumentType.Word("type").from("build");
        Argument<?> fileName = ArgumentType.Word("fileName");

        setArgumentCallback((player, argument, error) -> usage(player, null), type);

        addSyntax((sender, arguments) -> {
            Player player = (Player) sender;

            sender.sendMessage(ChatColor.RED + "Schematic building...");
            Schematic schematic = new Schematic(arguments.getWord("fileName"), player.getInstance());

            if(schematic.build(player.getPosition()) == Schematic.ErrorMessages.None)
                sender.sendMessage(ChatColor.CYAN + "Schematic build!");


        }, type, fileName);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage(ChatColor.CYAN + "--- " + ChatColor.BOLD + ChatColor.GOLD + "Schematics" + ChatColor.RESET + ChatColor.CYAN + " ---");
        sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.CYAN + "/schematic build <fileName>");
    }

}
