package me.junkiecraft.TownyPlots.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.Arrays;
import java.util.List;
import org.bukkit.plugin.java.JavaPlugin;

import com.palmergames.bukkit.towny.exceptions.NotRegisteredException;

import me.junkiecraft.TownyPlots.Main;
import me.junkiecraft.TownyPlots.menus.MenuManager;

public class CommandManager implements CommandExecutor, TabCompleter {
    private JavaPlugin plugin;
    String PREFIX = Main.getChatUtils().getMessage("prefix");

    public CommandManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        int currentPage = 1;
        if (!(sender instanceof Player)) {
            if (args.length != 0) {
                if (args[0].equalsIgnoreCase("see")) {
                    sender.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("notPlayer"));
                }
                if (args[0].equalsIgnoreCase("help")) {
                    this.plugin.getConfig().getStringList("Messages.help")
                            .forEach(line -> sender.sendMessage(Main.getChatUtils().setColor(line)));
                    return true;
                }
                if (args[0].equalsIgnoreCase("reload")) {
                    plugin.reloadConfig();
                    sender.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("reload"));
                }
            } else {
                sender.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("commandError"));
            }

            return true;

        }

        Player player = (Player) sender;
        if ((sender instanceof Player)) {
            if (args.length != 0) {
                if (player.hasPermission("townyplots.use")) {

                    if (args[0].equalsIgnoreCase("see")) {
                        MenuManager menuManager = Main.getInstance().getMenuManager();
                        try {
                            menuManager.openParcelMenu(player, currentPage);
                        } catch (NotRegisteredException e) {
                            // e.printStackTrace();
                            player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("errorMenu"));
                        }
                    }
                    if (args[0].equalsIgnoreCase("help")) {
                        this.plugin.getConfig().getStringList("Messages.help")
                                .forEach(line -> sender.sendMessage(Main.getChatUtils().setColor(line)));
                        return true;
                    }

                } else {
                    player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("noPermission"));
                }
                if (player.hasPermission("townyplots.admin")) {
                    if (args[0].equalsIgnoreCase("reload")) {
                        plugin.reloadConfig();
                        player.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("reload"));
                    }
                }
            }else {
                sender.sendMessage(PREFIX + ": " + Main.getChatUtils().getMessage("commandError"));
            }

        }
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("townyplots")) {
            if (sender.hasPermission("townyplots.admin") || sender.isOp()) {
                List<String> tabComplete = Arrays.asList("help", "see", "reload");
                if (args.length == 1) {
                    return tabComplete;
                }
            } else {
                List<String> tabComplete = Arrays.asList("help", "see");
                if (args.length == 1) {
                    return tabComplete;
                }
            }
        }
        return null;
    }
}