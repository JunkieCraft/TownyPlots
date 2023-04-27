package me.junkiecraft.TownyPlots.utils;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatUtils {
    private JavaPlugin plugin;

    public ChatUtils(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public String setColor(String msg) {
        return ChatColor.translateAlternateColorCodes('&', msg);
    }

    public String getMessage(String path) {
        return setColor(this.plugin.getConfig().getString("Messages." + path));
    }
}

