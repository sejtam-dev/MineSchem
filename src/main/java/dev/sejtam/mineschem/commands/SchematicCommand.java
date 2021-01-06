package dev.sejtam.mineschem.commands;

import dev.sejtam.mineschem.schematic.ISchematc;
import dev.sejtam.mineschem.schematic.SpongeSchematic;

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

            sender.sendMessage(ChatColor.RED + "SpongeSchematic building...");
            SpongeSchematic spongeSchematic = new SpongeSchematic(arguments.getWord("fileName"), player.getInstance());

            spongeSchematic.read();
            ISchematc.ErrorMessages error = spongeSchematic.build(player.getPosition());
            if(error == SpongeSchematic.ErrorMessages.None)
                sender.sendMessage(ChatColor.CYAN + "SpongeSchematic build!");
            else
                sender.sendMessage(ChatColor.RED + error.toString() + " Error!");


        }, type, fileName);
    }

    private void usage(CommandSender sender, Arguments arguments) {
        sender.sendMessage(ChatColor.CYAN + "--- " + ChatColor.BOLD + ChatColor.GOLD + "Schematics" + ChatColor.RESET + ChatColor.CYAN + " ---");
        sender.sendMessage(ChatColor.GOLD + "- " + ChatColor.CYAN + "/schematic build <fileName>");
    }

}
